package cs455.overlay.routing;

import java.util.HashMap;

import cs455.overlay.transport.TCPConnection;

public class RoutingEntry {
	
	int[] routing;
	TCPConnection[] routingConnection;
	int nodeId;
	int routingSize = 3;
	HashMap<Integer, TCPConnection> registry;
	
	//Constructor that create a routing table for a node based on the nodeId passed in
	public RoutingEntry(int[] routingTable, int inNodeId, int inRoutingSize, HashMap<Integer, TCPConnection> registry) {
		this.nodeId = inNodeId;
		this.registry = registry;
		if(inRoutingSize != 0) {
			this.routingSize = inRoutingSize;
		}
		this.routing = new int[this.routingSize];
		this.routingConnection = new TCPConnection[this.routingSize];
		createRoutingEntry(routingTable);
	}
	 
	//Helper to create the routing table
	public void createRoutingEntry(int[] routingTable) {
		for(int i = 0; i < routingTable.length; i++) {
			if(routingTable[i] == nodeId) {
				for(int j = 0; j < routingSize; j++) {
					int hop = (int) Math.pow(2, j);
					routing[j] = routingTable[(((i + hop) % routingTable.length))];
					routingConnection[j] = registry.get(routingTable[((((i + hop)) % routingTable.length))]);
				}
			}
		}
	}
	
	//Getter for the routingIds
	public int[] getRoutingIds() {
		return this.routing;
	}
	
	//Getter for the connections it should establish
	public TCPConnection[] getRoutingConnections() {
		return this.routingConnection;
	}

}
