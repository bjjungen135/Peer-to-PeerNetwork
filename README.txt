To run the program you will issue these command after untar
gradle build
java -cp build/libs/'name of direcotry'.jar cs455.overlay.node.Registry 'portnumber'
java -cp build/libs/'name of direcotry'.jar cs455.overlay.node.MessagingNode 'ip-address' 'portnumber'

All classes are in the folder
src/main/java/cs455/overlay/node
src/main/java/cs455/overlay/routing
src/main/java/cs455/overlay/transport
src/main/java/cs455/overlay/util
src/main/java/cs455/overlay/wireformats

Class Descriptions -

MeassagingNode.java - Messaging node program that starts up the messaging node then will register with the overlay then listen for commands from the overlay to proceed

Node.java - A class to store the information for each messaging node that includes the IP, port number, and the node ID

Registry.java - This is the registry program that will start up the server socket and listen for incoming connections and then start to issue commands to the messaging nodes when the appropriate command is issues

RoutingEntry.java – This class will create a routing entry for the messaging node ID that is passed into the constructor

RoutingTable.java – This will sort and make the routing table for all the nodes that are registered in the registry

TCPConnection.java – This will get a TCP connection and store all the data needed for that. Such as the TCP sender and receiver class that is made in the TCP connection and that allows the nodes to communicate to each other

TCPConnectionsCache.java – This class is used to store active TCP connections for each node, so a node will have a TCP connection cache to keep track of the connections that it has and references this in order to look for nodes to send information to

TCPServerThread.java – This is the server thread that is started on the registry that is to actively listen for incoming connections and handle those for the registry appropriately. Then this will also start up on the messaging nodes for other messaging nodes to connect to those nodes

InteractiveCommandParser.java – This class is a class that runs as a thread, and it is listening for commands that are inputted from system.in and will call the appropriate functions when specific commands are typed in the input

StatisticsCollectorAndDesplay.java – This is what collects all the statistics for the registry to output after all nodes are done sending messages. It will print it in a readable format

Event.java – This is an interface class that allows other classes to implement this class and then use those functions that are stated in the interface

EventFactory.java – This is a singleton instance with a private constructor, and this will allow us to take an event and create another class out of the event that is passed into the event factory. This is used multiple times in a lot of different classes with the same function, so it made sense to make it a singleton instance

All classes below are message classes that all get converted into byte arrays and back into objects when they are received by a node. The message details are described below.

NodeReportsOverlaySetupStatus.java - This message tells the registry that the node has setup it’s routing table from the node manifest

OverlayNodeReportsTaskFInished.java – The messaging node will report when it has sent all the messages sent to request and then sends the done to the registry

OverlayNodeReportsTrafficSummary.java – The messaging node will send the trackers that it was keeping back to the registry for the registry to use

OverlayNodeSendData.java – The messaging node will send data out, then when it receives this message, if it is the destination then add that to counter info, if not then consult routing table and then relay the data to another node

OverlayNodeSendsDeregistration.java – A messaging node will send a deregistration request to the registry

OverlayNodeSendsRegistration.java – A messaging node will send a registration request to the registry

RegistryReportsDeregistrationStatus.java – The registry will send a message back to the messaging node with information about the deregistration status, i.e. was it successful or not

RegistryReportsRegistrationStatus.java – The registry will send a message back to the messaging node with information about the registration status, i.e. was it successful or not

RegistryRequestsTaskInititate.java – The registry will send a task initiation to all nodes in the registry with a specified number of messages to send

RegistryRequestsTrafficSummary.java – The registry will ask for traffic information to display and check that all messages were sent correctly

RegistrySendsNodeManifest.java – The registry will send the messaging node the list of nodes that the node will need to establish a connection with
