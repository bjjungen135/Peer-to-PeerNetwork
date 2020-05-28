package cs455.overlay.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;

public class InteractiveCommandParser implements Runnable{
	
	boolean isRegistry;
	MessagingNode node;
	Registry registry;
	
	//Class that listens to the system.in for commands and routes them accordingly
	public InteractiveCommandParser(boolean type, Registry registry) {
		this.isRegistry = type;
		this.registry = registry;
	}

	public InteractiveCommandParser(boolean type, MessagingNode node) {
		this.isRegistry = type;
		this.node = node;
	}

	@Override
	public void run(){
		BufferedReader reader;
		String input = "";
		while(!input.equals("exit")) {
			reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				input = reader.readLine();
				if(isRegistry) {
					registryCommands(input);
				}
				else {
					messagingNodeCommands(input);
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	public void registryCommands(String command) throws IOException{
		int number = 0;
		if(command.contains(" ")) {
			String[] temp = command.split(" " );
			number = Integer.parseInt(temp[1]);
			command = temp[0];
		}
		switch(command) {
		case "list-messaging-nodes" : registry.commandListMessagingNodes(); break;
		case "setup-overlay" : if(number == 0) number = 3; registry.sendNodeManifest(number); break;
		case "list-routing-tables" : registry.listNodeRoutingTables(); break;
		case "start" : registry.startMessagingNodes(number); break;
		default : break;
		}
	}
	
	public void messagingNodeCommands(String command) throws IOException{
		switch(command) {
		case "exit-overlay" : node.messagingNodeExitOverlay(node); break;
		case "print-counter-and-diagnostics" : node.printDiagnostics(); break;
		default : break;
		}
	}
}
