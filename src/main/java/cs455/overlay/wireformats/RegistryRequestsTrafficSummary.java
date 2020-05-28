package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryRequestsTrafficSummary implements Event{
	
	//Message type class that can be converted into byte[] and back to object when a node receives it, then appropriate functions are called on the node side
	
	final int type = 11;
	
	public RegistryRequestsTrafficSummary() {}
	
	public RegistryRequestsTrafficSummary(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		din.readInt();
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
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

}
