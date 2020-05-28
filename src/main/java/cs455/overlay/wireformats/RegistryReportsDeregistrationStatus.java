package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryReportsDeregistrationStatus implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 5;
	int status;
	String info;
	
	public RegistryReportsDeregistrationStatus(int inStatus, String inInfo) {
		this.status = inStatus;
		this.info = inInfo;
	}
	
	public RegistryReportsDeregistrationStatus(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		status = din.readInt();
		int infoByteLength = din.readInt();
		byte[] infoInBytes = new byte[infoByteLength];
		din.readFully(infoInBytes);
		info = new String(infoInBytes);
		baInputStream.close();
		din.close();
	}
	
	public int getDeregistrationStatus() {
		return this.status;
	}
	
	public String getDeregistrationInfo() {
		return this.info;
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
		dout.writeInt(status);
		byte[] infoBytes = info.getBytes();
		int infoBytesLength = infoBytes.length;
		dout.writeInt(infoBytesLength);
		dout.write(infoBytes);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
}
