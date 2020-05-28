package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsDeregistration implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 4;
	String ip;
	int  portNumber;
	int nodeID;
	
	public OverlayNodeSendsDeregistration(String inIP, int inPort, int inID) {
		this.ip = inIP;
		this.portNumber = inPort;
		this.nodeID = inID;
	}
	
	public OverlayNodeSendsDeregistration(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		int ipByteLength = din.readInt();
		byte[] ipBytes = new byte[ipByteLength];
		din.readFully(ipBytes);
		ip = new String(ipBytes);
		portNumber = din.readInt();
		nodeID = din.readInt();
		baInputStream.close();
		din.close();
	}
	
	public String getDeregistrationIP() {
		return this.ip;
	}
	
	public int getDeregistrationPort() {
		return this.portNumber;
	}
	
	public int getDeregistrationNodeID() {
		return this.nodeID;
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
		byte[]  ipBytes = ip.getBytes();
		int length = ipBytes.length;
		dout.writeInt(length);
		dout.write(ipBytes);
		dout.writeInt(portNumber);
		dout.writeInt(nodeID);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
	
	

}
