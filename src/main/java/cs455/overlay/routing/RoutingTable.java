package cs455.overlay.routing;

import java.util.Arrays;
import java.util.HashMap;

import cs455.overlay.transport.TCPConnection;

public class RoutingTable {
	
	int[] nodeIdArray;

	//Hold data for all the routing entries
	public RoutingTable(HashMap<Integer, TCPConnection> registry) {
		int pointer = 0;
		nodeIdArray = new int[registry.size()];
		for(Integer nodeId : registry.keySet()) {
			nodeIdArray[pointer] = nodeId;
			pointer++;
		}
		Arrays.sort(nodeIdArray);
	}

	public int[] getRoutingTable() {
		return nodeIdArray;
	}
}
