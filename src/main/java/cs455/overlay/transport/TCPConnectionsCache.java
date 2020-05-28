package cs455.overlay.transport;

import java.util.HashMap;
import java.util.Random;

public class TCPConnectionsCache {
	
	//Class to store established TCPConnections
	
	HashMap<Integer, TCPConnection> connectionsCache;
	Integer connectionNumber;
	Random random;
	

	public TCPConnectionsCache() {
		this.connectionsCache = new HashMap<>();
	}

	synchronized public void print() {
		System.out.println(connectionsCache);
	}

	public void addTCPConnection(int id, TCPConnection connection) {
		connectionsCache.put(id, connection);
	}

	public TCPConnection getConnection(int id) {
		return connectionsCache.get(id);
	}

	public boolean doesCacheContain(int id) {
		return connectionsCache.containsKey(id);
	}
}
