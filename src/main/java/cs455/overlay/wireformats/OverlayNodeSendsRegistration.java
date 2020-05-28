package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsRegistration implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 2;
	String ip;
	int portNumber;
	
	public OverlayNodeSendsRegistration(String inIP, int inPort) {
		this.ip = inIP;
		this.portNumber = inPort;
	}
	
	public OverlayNodeSendsRegistration(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		int ipByteLength = din.readInt();
		byte[] ipInBytes = new byte[ipByteLength];
		din.readFully(ipInBytes);
		ip = new String(ipInBytes);
		portNumber = din.readInt();
		baInputStream.close();
		din.close();
	}
	
	public String getRegistrationIP() {
		return this.ip;
	}
	
	public int getRegistrationPort() {
		return this.portNumber;
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
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
}
