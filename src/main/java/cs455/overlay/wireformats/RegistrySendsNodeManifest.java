package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.transport.TCPConnection;

public class RegistrySendsNodeManifest implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
		
	final int type = 6;
	int routingTableSize;
	RoutingEntry regEntry;
	int numberOfAllNodes;
	int[] allNodeIds;
	int[] routingIds;
	String[] routingIps;
	int[] routingPorts;
	TCPConnection[] connections;
	
	public RegistrySendsNodeManifest(int inTableSize, RoutingEntry regEntry, int[] allNodeIds) {
		this.routingTableSize = inTableSize;
		this.regEntry = regEntry;
		this.routingIds = this.regEntry.getRoutingIds();
		this.connections = this.regEntry.getRoutingConnections();
		this.allNodeIds = allNodeIds;
		this.numberOfAllNodes = this.allNodeIds.length;
	}
	
	public RegistrySendsNodeManifest(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		routingTableSize = din.readInt();
		routingIds = new int[routingTableSize];
		routingIps = new String[routingTableSize];
		routingPorts = new int[routingTableSize];
		for(int i = 0; i < routingTableSize; i++) {
			routingIds[i] = din.readInt();
			int ipByteLength = din.readInt();
			byte[] ipInBytes = new byte[ipByteLength];
			din.readFully(ipInBytes);
			routingIps[i] = new String(ipInBytes);
			routingPorts[i] = din.readInt();
		}
		numberOfAllNodes = din.readInt();
		allNodeIds = new int[numberOfAllNodes];
		for(int i = 0; i < numberOfAllNodes; i++)
			allNodeIds[i] = din.readInt();
		baInputStream.close();
		din.close();
	}
	
	public int[] getManifestAllIdList() {
		return this.allNodeIds;
	}
	
	public int getManifestRoutingTableSize() {
		return this.routingTableSize;
	}
	
	public int[] getManifestRoutingIds() {
		return this.routingIds;
	}
	
	public String[] getManifestRoutingIps() {
		return this.routingIps;
	}
	
	public int getManifestRoutingSize() {
		return this.routingTableSize;
	}
	
	@Override
	public int getType() {
		return type;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		dout.writeInt(routingTableSize);
		for(int i = 0; i < routingTableSize; i++) {
			String ip;
			if(connections[i].getTCPSocket() != null) {
				ip = connections[i].getTCPSocket().getInetAddress().getHostName().split("\\.")[0];
			}
			else {
				ip = connections[i].getTCPIp();
			}
			dout.writeInt(routingIds[i]);
			byte[] ipBytes = ip.getBytes();
			int ipBytesLength = ipBytes.length;
			dout.writeInt(ipBytesLength);
			dout.write(ipBytes);
			if(connections[i].getTCPSocket() != null)
				dout.writeInt(connections[i].getTCPSocket().getPort());
			else
				dout.writeInt(connections[i].getTCPPort());
		}
		dout.writeInt(numberOfAllNodes);
		for(int i = 0; i < allNodeIds.length; i++) {
			dout.writeInt(allNodeIds[i]);
		}
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

	public int[] getManifestRoutingPorts() {
		return this.routingPorts;
	}
}
