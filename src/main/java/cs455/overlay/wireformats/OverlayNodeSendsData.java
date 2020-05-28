package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsData implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 9;
	int destinationId;
	int sourceId;
	int payload;
	int traceSize;
	int[] trace;
	
	public OverlayNodeSendsData(int destination, int source, int inPayload, int size, int[] inTrace) {
		this.destinationId = destination;
		this.sourceId = source;
		this.payload = inPayload;
		this.traceSize = size;
		this.trace = inTrace;
	}
	
	public OverlayNodeSendsData(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		this.destinationId = din.readInt();
		this.sourceId = din.readInt();
		this.payload = din.readInt();
		this.traceSize = din.readInt();
		this.trace = new int[traceSize];
		for(int i = 0; i < trace.length; i++) {
			trace[i] = din.readInt();
		}
		baInputStream.close();
		din.close();
	}
	
	public int getDataDestination() {
		return this.destinationId;
	}
	
	public int getDataPayload() {
		return this.payload;
	}
	
	public int getDataSource() {
		return this.sourceId;
	}
	
	public int[] getDataTrace() {
		return this.trace;
	}
	
	public int getDataTraceSize() {
		return this.traceSize;
	}
	
	public void setDataDestintaiton(int newDestination) {
		this.destinationId = newDestination;
	}
	
	public void setDataPayload(int newPayload) {
		this.payload = newPayload;
	}
	
	public void setDataTrace(int[] newDataTrace) {
		this.trace = newDataTrace;
	}
	
	public void setDataTraceSize(int newDataTraceSize) {
		this.traceSize = newDataTraceSize;
	}
	
	public void addRelayNode(int nodeId) {
		this.traceSize++;
		int[] temp = new int[this.traceSize];
		for(int i = 0; i < this.trace.length; i++)
			temp[i] = this.trace[i];
		temp[this.traceSize - 1] = nodeId;
		this.trace = temp;
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
		dout.writeInt(destinationId);
		dout.writeInt(sourceId);
		dout.writeInt(payload);
		dout.writeInt(traceSize);
		for(int i = 0; i < traceSize; i++) {
			dout.writeInt(trace[i]);
		}
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

}