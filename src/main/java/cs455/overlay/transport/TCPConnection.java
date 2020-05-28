package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPConnection {
	Socket socket;
	TCPSender tcpSender;
	TCPReceiver tcpReceiver;
	String ip;
	int portnum;
	Node node;
	
	
	//Connects the socket to the server socket and establishes a connection if it can be made	
	public TCPConnection(Socket socket, Node node) throws IOException {
		this.socket = socket;
		this.node = node;
		this.setTCPSender(new TCPSender(this.socket));
		this.setTCPReceiver(new TCPReceiver(this.socket, this.node, this));
		new Thread(this.getTCPReceiver()).start();
	}
	
	public TCPConnection(String ip, int portnum, Node node) throws UnknownHostException, IOException {
		this.ip = ip;
		this.portnum = portnum;
		this.node = node;
	}
	
	public void setupTCPConnection() throws UnknownHostException, IOException {
		this.socket = new Socket(ip, portnum);
		this.setTCPSender(new TCPSender(this.socket));
		this.setTCPReceiver(new TCPReceiver(this.socket, this.node, this));
		new Thread(this.getTCPReceiver()).start();
	}
	
	public Socket getTCPSocket() {
		return socket;
	}
	
	public String getTCPIp() {
		return ip;
	}
	
	public int getTCPPort() {
		return portnum;
	}
	
	//Getters and setters for the TCPSender and TCPReceiver classes
	public TCPSender getTCPSender() {
		return tcpSender;
	}

	public void setTCPSender(TCPSender tcpSender) {
		this.tcpSender = tcpSender;
	}
	
	public TCPReceiver getTCPReceiver() {
		return tcpReceiver;
	}

	public void setTCPReceiver(TCPReceiver tcpReceiver) {
		this.tcpReceiver = tcpReceiver;
	}

	//Class for Sending data over the socket connection
	public class TCPSender{
		private Socket socket;
		private DataOutputStream dout;
		
		public TCPSender(Socket socket) throws IOException{
			this.socket = socket;
			dout = new DataOutputStream(socket.getOutputStream());
		}
		
		public synchronized void sendData(byte[] dataToSend) throws IOException {
			int dataLength = dataToSend.length;
			dout.writeInt(dataLength);
			dout.write(dataToSend, 0, dataLength);
			dout.flush();
		}
	}
	
	//Class for Receiving data over a socket connection
	public class TCPReceiver implements Runnable{
		private Socket socket;
		private DataInputStream din;
		private Node node;
		private TCPConnection connection;
		private EventFactory factory;
		
		public TCPReceiver(Socket socket, Node node, TCPConnection connection) throws IOException{
			this.socket = socket;
			this.node = node;
			this.connection = connection;
			this.din = new DataInputStream(socket.getInputStream());
			this.factory = EventFactory.getInstance();
		}
		
		@Override
		public void run() {
			int dataLength;
			while(socket != null) {
				try {
					dataLength = din.readInt();
					byte[] data = new byte[dataLength];
					din.readFully(data, 0, dataLength);
					Event event;
					synchronized(factory) {
						event = factory.createEvent(data);
					}
					node.onEvent(event, connection);
				}
				catch(IOException e) {
					System.out.println(e.getMessage());
					break;
				}
			}
		}
	}
}
