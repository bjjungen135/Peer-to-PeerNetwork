package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeReportsTrafficSummary implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 12;
	int nodeId;
	int sentPackets;
	int relayPackets;
	int receivedPackets;
	long sentSum;
	long receivedSum;

	public OverlayNodeReportsTrafficSummary(int nodeId, int sendPack, int relayPack, long sendSum,
			int receivePack, long receiveSum) {
		this.nodeId = nodeId;
		this.sentPackets = sendPack;
		this.relayPackets = relayPack;
		this.receivedPackets = receivePack;
		this.sentSum = sendSum;
		this.receivedSum = receiveSum;
	}

	public OverlayNodeReportsTrafficSummary(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		this.nodeId = din.readInt();
		this.sentPackets = din.readInt();
		this.relayPackets = din.readInt();
		this.sentSum = din.readLong();
		this.receivedPackets = din.readInt();
		this.receivedSum = din.readLong();
		baInputStream.close();
		din.close();;
	}
	
	public int getTrafficSummaryId() {
		return this.nodeId;
	}
	
	public int getTrafficPacketsSent() {
		return this.sentPackets;
	}
	
	public int getTrafficPacketsReceived() {
		return this.receivedPackets;
	}
	
	public int getTrafficPacketsRelayed() {
		return this.relayPackets;
	}
	
	public long getTrafficSentSum() {
		return this.sentSum;
	}
	
	public long getTrafficReceivedSum() {
		return this.receivedSum;
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
		dout.writeInt(nodeId);
		dout.writeInt(sentPackets);
		dout.writeInt(relayPackets);
		dout.writeLong(sentSum);
		dout.writeInt(receivedPackets);
		dout.writeLong(receivedSum);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

}
