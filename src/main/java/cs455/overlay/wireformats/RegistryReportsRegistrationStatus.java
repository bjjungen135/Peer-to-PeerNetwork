package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryReportsRegistrationStatus implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side

	final int type = 3;
	int status;
	String info;
	
	public RegistryReportsRegistrationStatus(int inStatus, String inInfo) {
		this.status = inStatus;
		this.info = inInfo;
	}
	
	public RegistryReportsRegistrationStatus(byte[] marshalledBytes) throws IOException{
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
	
	public int getStatusNum() {
		return this.status;
	}
	
	public String getStatusInfo() {
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
