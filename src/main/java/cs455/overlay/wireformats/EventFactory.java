package cs455.overlay.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EventFactory {
	
	//Singleton instance class in order to create an event into a message type

	private static final EventFactory instance = new EventFactory();
	
	private EventFactory() {}
	
	public static EventFactory getInstance() {
		return instance;
	}
	
	//Method for reading message and then creating that object
	public Event createEvent(byte[] marshalledBytes) throws IOException {
		switch(ByteBuffer.wrap(marshalledBytes).getInt()) {
			case 2:
				return new OverlayNodeSendsRegistration(marshalledBytes);
			case 3:
				return new RegistryReportsRegistrationStatus(marshalledBytes);
			case 4:
				return new OverlayNodeSendsDeregistration(marshalledBytes);
			case 5:
				return new RegistryReportsDeregistrationStatus(marshalledBytes);
			case 6:
				return new RegistrySendsNodeManifest(marshalledBytes);
			case 7:
				return new NodeReportsOverlaySetupStatus(marshalledBytes);
			case 8:
				return new RegistryRequestsTaskInitiate(marshalledBytes);
			case 9:
				return new OverlayNodeSendsData(marshalledBytes);
			case 10:
				return new OverlayNodeReportsTaskFinished(marshalledBytes);
			case 11:
				return new RegistryRequestsTrafficSummary(marshalledBytes);
			case 12:
				return new OverlayNodeReportsTrafficSummary(marshalledBytes);
			default :
				System.err.println("Event could not be created");
				return null;
		}
	}
}
