package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;
import cs455.overlay.wireformats.OverlayNodeSendsData;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.RegistryRequestsTrafficSummary;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

public class MessagingNode implements Node{
	
	HashMap<Integer, TCPConnection> connectionCache;
	EventFactory factory;
	int assignedNodeId;
	String ip;
	int portNum;
	TCPConnection connectionRegistry;
	int[] allNodeIds;
	int[] routingIds;
	String[] routingIps;
	int[] routingPorts;
	int routingTableSize;
	int sendTracker;
	int receiveTracker;
	int relayTracker;
	long sendSummation;
	long receiveSummation;
	Random random;
	int max;
	int min;
	boolean startedSending;
	Thread server;
	Thread commandParser;
	TCPServerThread serverThread;
	int currentIndex;
	Socket socketToServer;
	boolean conaintsAllNodeIds;
	OverlayNodeSendsData dataToSend;
	
	//Contructor
	public MessagingNode(String ip, int portnum) throws UnknownHostException, IOException {
		connectionCache = new HashMap<>();
		random = new Random();
		sendTracker = 0;
		receiveTracker = 0;
		relayTracker = 0;
		sendSummation = 0;
		receiveSummation = 0;
		max = Integer.MAX_VALUE;
		min = Integer.MIN_VALUE;
		startedSending = false;
		currentIndex = -1;
		socketToServer = new Socket(ip, portnum);
	}

	public static void main(String[] args) throws IOException{
		//Start connection with server socket
		MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));
		node.connectionRegistry = new TCPConnection(node.socketToServer, node);
		//Start it's own server
		node.serverThread = new TCPServerThread("0", node);
		node.ip = node.serverThread.getServerIP();
		node.server = new Thread(node.serverThread);
		node.server.start();
		node.portNum = node.serverThread.getServerPort();
		//Create registration object
		OverlayNodeSendsRegistration registration = new OverlayNodeSendsRegistration(node.serverThread.getServerIP(), node.serverThread.getServerPort());
		//Send registration
		node.connectionRegistry.getTCPSender().sendData(registration.getBytes());
		node.commandParser = new Thread(new InteractiveCommandParser(false, node));
		node.commandParser.start();
		node.factory = EventFactory.getInstance();
	}

	//Send a deregistration request
	public void messagingNodeExitOverlay(MessagingNode messageNode) throws IOException{
		OverlayNodeSendsDeregistration dereg = new OverlayNodeSendsDeregistration(messageNode.ip, messageNode.portNum, messageNode.assignedNodeId);
		messageNode.connectionRegistry.getTCPSender().sendData(dereg.getBytes());
	}
	
	//Got routing table, so create it and establish connections to other nodes
	private void messagingNodeReceivesManifest(MessagingNode messageNode, RegistrySendsNodeManifest manifest) throws IOException{
		try {
			messageNode.routingIds = manifest.getManifestRoutingIds();
			messageNode.routingIps = manifest.getManifestRoutingIps();
			messageNode.routingPorts = manifest.getManifestRoutingPorts();
			messageNode.allNodeIds = manifest.getManifestAllIdList();
			messageNode.routingTableSize = manifest.getManifestRoutingTableSize();
			for(int i = 0; i < routingTableSize; i++) {
				TCPConnection connection = new TCPConnection(messageNode.routingIps[i], messageNode.routingPorts[i], messageNode);
				connection.setupTCPConnection();
				messageNode.connectionCache.put(messageNode.routingIds[i], connection);
			}
			String info = "Setup complete";
			NodeReportsOverlaySetupStatus setupStatus = new NodeReportsOverlaySetupStatus(messageNode.assignedNodeId, info);
			messageNode.connectionRegistry.getTCPSender().sendData(setupStatus.getBytes());
			}
			catch(IOException e) {
				int failed = -1;
				String info = "Setup failed. Please see following error : " + e;
				NodeReportsOverlaySetupStatus setupStatus = new NodeReportsOverlaySetupStatus(failed, info);
				messageNode.connectionRegistry.getTCPSender().sendData(setupStatus.getBytes());
			}
	}
	
	//Start sending tasks, and notify the registry when done
	private void messagingNodeStartsTask(MessagingNode messageNode, int taskNumber) throws IOException{
		synchronized(messageNode){
			messageNode.sendTracker = 0; messageNode.relayTracker = 0; messageNode.sendSummation = 0; messageNode.receiveTracker = 0; messageNode.receiveSummation = 0; messageNode.startedSending = false;
			messageNode.startedSending = true;
		}
		while(taskNumber > 0) {
			int randomPosition = messageNode.random.nextInt(messageNode.allNodeIds.length);
			while(messageNode.allNodeIds[randomPosition] == messageNode.assignedNodeId) {
				randomPosition = messageNode.random.nextInt(messageNode.allNodeIds.length);
			}
			int destination = messageNode.allNodeIds[randomPosition];
			int payload = (int) ((long) min + Math.random() * ((long) max - min + 1));
			messageNode.sendSummation += payload;
			messageNode.sendTracker++;
			if(messageNode.dataToSend == null) {
				messageNode.dataToSend = new OverlayNodeSendsData(destination, messageNode.assignedNodeId, payload, 0, new int[0]);
			}
			else {
				messageNode.dataToSend.setDataDestintaiton(destination);
				messageNode.dataToSend.setDataPayload(payload);
				messageNode.dataToSend.setDataTrace(new int[0]);
				messageNode.dataToSend.setDataTraceSize(0);
			}
			if(messageNode.connectionCache.containsKey(destination)) {
				messageNode.connectionCache.get(destination).getTCPSender().sendData(dataToSend.getBytes());
			}
			else {
				int relayId = getRealyId(messageNode, dataToSend);
				messageNode.connectionCache.get(relayId).getTCPSender().sendData(dataToSend.getBytes());
			}
			taskNumber--;
		}
		OverlayNodeReportsTaskFinished finished = new OverlayNodeReportsTaskFinished(ip, portNum, assignedNodeId);
		messageNode.connectionRegistry.getTCPSender().sendData(finished.getBytes());
	}
	
	//Received data, if it is destination stop and update counter, otherwise relay the message based on routing table
	private synchronized void messagingNodeReceivesData(MessagingNode messageNode, OverlayNodeSendsData data) throws IOException{
		if(data.getDataDestination() == messageNode.assignedNodeId) {
			messageNode.receiveTracker++;
			messageNode.receiveSummation += data.getDataPayload();
			return;
		}
		else {
			messageNode.relayTracker++;
			data.addRelayNode(assignedNodeId);
			if(messageNode.connectionCache.containsKey(data.getDataDestination())) {
				messageNode.connectionCache.get(data.getDataDestination()).getTCPSender().sendData(data.getBytes());
				return;
			}
		}
		int relayId = getRealyId(messageNode, data);
		messageNode.connectionCache.get(relayId).getTCPSender().sendData(data.getBytes());
	}
	
	//Send the traffic summary of this node
	private void messagingNodeSendsTrafficSummary(MessagingNode messageNode) throws IOException{
		OverlayNodeReportsTrafficSummary reportTraffic = new OverlayNodeReportsTrafficSummary(assignedNodeId, sendTracker, relayTracker, sendSummation, receiveTracker, receiveSummation);
		messageNode.connectionRegistry.getTCPSender().sendData(reportTraffic.getBytes());
	}
	
	//Helper to find the relay id that the node should relay data to wihtout overshooting desination
	private synchronized int getRealyId(MessagingNode messageNode, OverlayNodeSendsData data) {
		if(messageNode.currentIndex == -1) {
			for(int i = 0; i < messageNode.allNodeIds.length; i++) {
				if(messageNode.allNodeIds[i] == messageNode.assignedNodeId)
					messageNode.currentIndex = i;
			}
		}
		int currentRelayPosition = messageNode.currentIndex;
		for(int i = messageNode.currentIndex; i < messageNode.allNodeIds.length + messageNode.currentIndex; i++) {
			if(messageNode.allNodeIds[(i % messageNode.allNodeIds.length)] == messageNode.assignedNodeId)
				continue;
			else if(messageNode.connectionCache.containsKey(allNodeIds[(i % allNodeIds.length)])) {
				currentRelayPosition = (i % messageNode.allNodeIds.length);
			}
			else if(messageNode.allNodeIds[(i % messageNode.allNodeIds.length)] == data.getDataDestination()) {
				break;
			}
		}
		return messageNode.allNodeIds[currentRelayPosition];
	}

	//Prints counter for debug
	public void printDiagnostics() {
		System.out.printf("%-10s %-20s %-20s %-20s %-20s %-20s\n", "", "Packets Sent", "Packets Received", "Packets Relayed", "Sum Values Sent", "Sum Values Received");
		String temp = "Node " + this.assignedNodeId;
		System.out.printf("%-10s %-20d %-20d %-20d %-20d %-20d\n", temp, this.sendTracker, this.receiveTracker, this.relayTracker, this.sendSummation, this.receiveSummation);
	}

	//When node receives an event, pass it here and call appropriate function
	@Override
	public void onEvent(Event event, TCPConnection connection) {
		switch(event.getType()) {
		case 3 : 
			RegistryReportsRegistrationStatus regStatus = (RegistryReportsRegistrationStatus) event;
			if(regStatus.getStatusNum() != -1) {
				assignedNodeId = regStatus.getStatusNum();
				System.out.println("Reg Status: " + regStatus.getStatusNum() + " " + regStatus.getStatusInfo());
			}
			else
				System.out.println(regStatus.getStatusInfo());
			break;
		case 5:
			RegistryReportsDeregistrationStatus deregStatus = (RegistryReportsDeregistrationStatus) event;
			System.out.println("Dereg Status: " + deregStatus.getDeregistrationStatus() + " " + deregStatus.getDeregistrationInfo());
			break;
		case 6: 
			RegistrySendsNodeManifest manifest = (RegistrySendsNodeManifest) event;
			try {
				messagingNodeReceivesManifest(this, manifest);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 8:
			RegistryRequestsTaskInitiate taskInitiate = (RegistryRequestsTaskInitiate) event;
			try {
				messagingNodeStartsTask(this, taskInitiate.getTaskNumber());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 9:
			OverlayNodeSendsData dataReceived = (OverlayNodeSendsData) event;
			try {
				messagingNodeReceivesData(this, dataReceived);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 11:
			RegistryRequestsTrafficSummary traffic = (RegistryRequestsTrafficSummary) event;
			try {
				messagingNodeSendsTrafficSummary(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		default: break;
		}
	}
}
