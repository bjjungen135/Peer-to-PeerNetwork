package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NodeReportsOverlaySetupStatus implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 7;
	int id;
	String info;
	
	public NodeReportsOverlaySetupStatus(int inId, String inInfo) {
		this.id = inId;
		this.info = inInfo;
	}
	
	public NodeReportsOverlaySetupStatus(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
		this.id = din.readInt();
		int infoLength = din.readInt();
		byte[] infoInBytes = new byte[infoLength];
		din.readFully(infoInBytes);
		this.info = new String(infoInBytes);
		baInputStream.close();
		din.close();
	}
	
	public int getSetupStatusId() {
		return this.id;
	}
	
	public String getSetupStatusInfo() {
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
		dout.writeInt(id);
		byte[] infoBytes = info.getBytes();
		int length = infoBytes.length;
		dout.writeInt(length);
		dout.write(infoBytes);
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}
}