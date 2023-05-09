package server;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;
import client.ChatClient3IF;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {
	private Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;

	// Constructor
	public ChatServer() throws RemoteException {
		super();
		chatters = new Vector<Chatter>(10, 1);
	}

	// Main method
	public static void main(String[] args) {
		startRMIRegistry();	
		String hostName = "localhost";
		String serviceName = "GroupChatService";
		if(args.length == 2){
			hostName = args[0];
			serviceName = args[1];
		}
		try{
			ChatServerInterface hello = new ChatServer();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);
			System.out.println("Group Chat RMI Server is running...");
		} catch(Exception e){
			System.out.println("Server had problems starting");
		}	
	}

	// Method to start the RMI registry
	public static void startRMIRegistry() {
		try{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready");
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}

	// Method to return a simple greeting
	public String sayHello(String ClientName) throws RemoteException {
		System.out.println(ClientName + " sent a message");
		return "Hello " + ClientName + " from group chat server";
	}

	// Method to update the chat with a new message
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message =  name + " : " + nextPost + "\n";
		sendToAll(message);
	}

	// Method to pass the remote object's identity to the server
	String line = "--------------------------------------------------\n";
	@Override
	public void passIDentity(RemoteRef ref) throws RemoteException {	
		try{
			System.out.println(line + ref.toString());
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	// Method to register a new listener for the chat server
	@Override
	public void registerListener(String[] details) throws RemoteException {	
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " has joined the chat session");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'sRMI service : " + details[2]);
		registerChatter(details);
	}

	// Helper method to register a new chatter
	private void registerChatter(String[] details){		
		try{
			ChatClient3IF nextClient = ( ChatClient3IF )Naming.lookup("rmi://" + details[1] + "/" + details[2]);
			chatters.addElement(new Chatter(details[0], nextClient));
			nextClient.messageFromServer("[Server] : Hello " + details[0] + " you are now free to chat.\n");
			sendToAll("[Server] : " + details[0] + " has joined the group.\n");
			updateUserList();		
		} catch(RemoteException | MalformedURLException | NotBoundException e){
			e.printStackTrace();
		}
	}

	// Helper method to update the user list for all clients
	private void updateUserList() {
		String[] currentUsers = getUserList();	
		for(Chatter c : chatters){
			try {
				c.getClient().updateUserList(currentUsers);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}
	private String[] getUserList(){
		String[] allUsers = new String[chatters.size()];
		for(int i = 0; i < allUsers.length; i += 1){
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}
	public void sendToAll(String newMessage){	
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(newMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}
	// Doesn't work
	/* @Override
	public void sendDM(int[] privateGroup, String privateMessage) throws RemoteException{
		Chatter pc;
		for(int i : privateGroup){
			pc= chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	} */
}