package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.RegistryRequestsTrafficSummary;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

public class Registry implements Node{
	HashMap<Integer, TCPConnection> registry;
	HashMap<Integer, TCPConnection> registryConnectionToNodes;
	TCPConnectionsCache cache;
	Random random;
	int[] nodeIdsAssign;
	EventFactory factory;
	HashMap<Integer, RoutingEntry> overlay;
	int[] nodeIds;
	int manifestStatusCounter = 0;
	boolean printRegistryReady = false;
	int finishedCounter = 0;
	StatisticsCollectorAndDisplay collector = null;
	boolean timeToPrint = false;
	
	public static void main(String[] args) {
		//Initializing registry
		Registry registry = new Registry();
		//Starting server thread
		TCPServerThread server = new TCPServerThread(args[0], registry);
		new Thread(server).start();
		//Starting command listener for input from console
		new Thread(new InteractiveCommandParser(true, registry)).start();
	}
	
	//Constructor
	public Registry() {
		this.registry = new HashMap<>();
		this.registryConnectionToNodes = new HashMap<>();
		this.overlay = new HashMap<>();
		this.cache = new TCPConnectionsCache();
		this.factory = EventFactory.getInstance();
		this.random = new Random();
	}

	//Lists the messaging nodes in order that are registered
	public void commandListMessagingNodes() {
		Integer[] ids = new Integer[registry.size()];
		int pointer = 0;
		for(Integer id : registry.keySet()) {
			ids[pointer] = id;
			pointer++;
		}
		Arrays.sort(ids);
		for(Integer id : ids) {
			System.out.println(registry.get(id).getTCPIp() + " " + registry.get(id).getTCPPort() + " " + id);
		}
	}

	//Send the node manifest to the registered nodes by computing them here first in the routing table and routing entry objects
	public void sendNodeManifest(int number) throws IOException{
		if(number == 0) number = 3;
		overlay = new HashMap<>();
		RoutingTable routingTable = new RoutingTable(registry);
		nodeIds = routingTable.getRoutingTable();
		for(int id : nodeIds) {
			RoutingEntry route = new RoutingEntry(nodeIds, id, number, registry);
			overlay.put(id, route);
		}
		for(Integer id : registry.keySet()) {
			RegistrySendsNodeManifest manifest = new RegistrySendsNodeManifest(number, overlay.get(id), nodeIds);
			registryConnectionToNodes.get(id).getTCPSender().sendData(manifest.getBytes());
		}
	}

	//List the routing tables for debugging
	public void listNodeRoutingTables() {
		if(overlay.isEmpty()) {
			System.out.println("Please issue command \"setup-overlay\" then try again.");
		}
		else {
			for(int id : nodeIds) {
				System.out.println("Routing table for node ID " + id);
				int[] routingIds = overlay.get(id).getRoutingIds();
				TCPConnection[] connections = overlay.get(id).getRoutingConnections();
				for(int i = 0; i < routingIds.length; i++) {
					String hostName = connections[i].getTCPSocket().getInetAddress().getHostName().split("\\.")[0];
					System.out.println("IP = " + hostName + " Port Number = " + connections[i].getTCPSocket().getPort() + " ID = " + routingIds[i]);
				}
			}
		}
	}

	//Issues command for the messaging nodes to start sending messages to each other nodes
	public void startMessagingNodes(int number) throws IOException{
		if(manifestStatusCounter != registry.size()) {
			System.out.println("Not all nodes are done setting up connections, please wait and try again when all nodes are setup");
			return;
		}
		if(collector == null)
			collector = new StatisticsCollectorAndDisplay(registry.size(), nodeIds);
		RegistryRequestsTaskInitiate taskInitiate = new RegistryRequestsTaskInitiate(number);
		for(Integer key : registry.keySet()) {
			registryConnectionToNodes.get(key).getTCPSender().sendData(taskInitiate.getBytes());
		}
	}
	
	//Method that handles the task finished method. It will increment counter and then check that it needs to print, if so then
	//sleep the thread for 12 seconds to allow relaying messages to reach nodes then send a traffic request
	private void registryReceivedTaskFinished(OverlayNodeReportsTaskFinished finished) throws IOException, InterruptedException{
		synchronized(this) {
			finishedCounter++;
		}
		if(finishedCounter == registry.size()) {
			RegistryRequestsTrafficSummary trafficRequest = new RegistryRequestsTrafficSummary();
			Thread.sleep(12000);
			for(Integer key : registry.keySet())
				registryConnectionToNodes.get(key).getTCPSender().sendData(trafficRequest.getBytes());
			timeToPrint = true;
		}
	}
	
	//When a registration comes in, generate random id, then register the node and send the registration status back to the node
	private void registryReceivedRegistrationRequest(OverlayNodeSendsRegistration reg, TCPConnection connection) throws IOException, InterruptedException{
		int id = random.nextInt(128);
		while(registry.containsKey(id))
			id = random.nextInt(128);
		if(registry.size() < 128) {
			synchronized(registry){
				registryConnectionToNodes.put(id, connection);
				registry.put(id, new TCPConnection(reg.getRegistrationIP(), reg.getRegistrationPort(), this));
				String info = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + registry.size() + ")";
				RegistryReportsRegistrationStatus regStatus = new RegistryReportsRegistrationStatus(id, info);
				registryConnectionToNodes.get(id).getTCPSender().sendData(regStatus.getBytes());
			}
		}
		else {
			String info = "Registration request unsuccessful. The registry is full";
			RegistryReportsRegistrationStatus regStatus = new RegistryReportsRegistrationStatus(-1, info);
			connection.getTCPSender().sendData(regStatus.getBytes());
		}
	}

	//When the node receives data, it goes here and based on event type, the proper method is called
	@Override
	public void onEvent(Event event, TCPConnection connection) {
		switch(event.getType()) {
			case 2: 
				try {
					registryReceivedRegistrationRequest((OverlayNodeSendsRegistration) event, connection);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				break;
			case 4:
				try {
					registryReceivedDeregistrationRequest((OverlayNodeSendsDeregistration) event, connection);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 7:
				NodeReportsOverlaySetupStatus setupStatus = (NodeReportsOverlaySetupStatus) event;
				registryReceivedSetupStatus(setupStatus);
				break;
			case 10:
				OverlayNodeReportsTaskFinished finished = (OverlayNodeReportsTaskFinished) event;
				try {
				registryReceivedTaskFinished(finished);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				break;
			case 12:
				OverlayNodeReportsTrafficSummary trafficSummary = (OverlayNodeReportsTrafficSummary) event;
				registryReceivedTrafficSummary(trafficSummary);
				break;
			default : break;
		}
	}

	//Received when the messaging node is done setting up it's routing table, then it will increment counter so that you can start tasks
	private void registryReceivedSetupStatus(NodeReportsOverlaySetupStatus setupStatus) {
		if(setupStatus.getSetupStatusId() != -1) {
			synchronized(this) {
				manifestStatusCounter++;
			}
		}
		else {
			System.out.println("Error");
		}
		if(manifestStatusCounter == registry.size()) {
			System.out.println("Registry ready to initiate tasks");
		}
	}

	//Received traffic summary from a node and puts it into a StatisitcsCollectorAndDislay and if the size is right then print the traffic summary
	private synchronized void registryReceivedTrafficSummary(OverlayNodeReportsTrafficSummary trafficSummary) {
		collector.addTraffic(trafficSummary.getTrafficSummaryId(), trafficSummary);
		if(collector.getCollectorSize() == registry.size() && finishedCounter == registry.size()) {
			if(timeToPrint) {
				collector.printTrafficSummary();
				finishedCounter = 0;
			}
		}
			
	}

	//Deregesiter a node from the registry
	private void registryReceivedDeregistrationRequest(OverlayNodeSendsDeregistration dereg, TCPConnection connection) throws IOException {
		int deregID = dereg.getDeregistrationNodeID();
		if(registry.containsKey(deregID)) {
			String information = "Deregistration request successful.";
			RegistryReportsDeregistrationStatus deregStatus = new RegistryReportsDeregistrationStatus(deregID, information);
			registry.get(deregID).getTCPSender().sendData(deregStatus.getBytes());
			registry.remove(deregID);
		}
		else {
			String information = "Deregistration ID does not match any in registry";
			RegistryReportsDeregistrationStatus deregStatus = new RegistryReportsDeregistrationStatus(deregID, information);
			registry.get(deregID).getTCPSender().sendData(deregStatus.getBytes());
		}
	}
}


