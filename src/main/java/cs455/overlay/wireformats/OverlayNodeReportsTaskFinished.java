package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeReportsTaskFinished implements Event{

	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 10;
	String ip;
	int port;
	int nodeId;

	public OverlayNodeReportsTaskFinished(String ip, int portNum, int nodeId) {
		this.ip = ip;
		this.port = portNum;
		this.nodeId = nodeId;
	}
	
	public OverlayNodeReportsTaskFinished(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		int ipByteLength = din.readInt();
		byte[] ipInBytes = new byte[ipByteLength];
		din.readFully(ipInBytes);
		ip = new String(ipInBytes);
		port = din.readInt();
		nodeId = din.readInt();
		baInputStream.close();
		din.close();
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
		byte[] ipBytes = ip.getBytes();
		int ipBytesLength = ipBytes.length;
		dout.writeInt(ipBytesLength);
		dout.write(ipBytes);
		dout.writeInt(port);
		dout.writeInt(nodeId);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
}