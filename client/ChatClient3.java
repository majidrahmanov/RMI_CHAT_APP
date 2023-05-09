package client;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.JOptionPane;
import server.ChatServerInterface;

public class ChatClient3 extends UnicastRemoteObject implements ChatClient3IF {
	
	// The version number of the serialized object
	private static final long serialVersionUID = 5497389032578181L;
	
	// Instance variables
	ClientRMIGUI chatGUI; // The client GUI
	private String hostName = "localhost"; // The host name
	private String serviceName = "GroupChatService"; // The service name
	private String clientServiceName; // The client service name
	private String name; // The client's name
	protected ChatServerInterface serverIF; // The server interface
	protected boolean connectionProblem = false; // A flag to indicate if there was a connection problem
	
	// Constructor
	public ChatClient3(ClientRMIGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
	}
	
	// Start the client
	public void startClient() throws RemoteException {		
		String[] details = {name, hostName, clientServiceName}; // The client details	
		try {
			// Bind the client's RMI object to the client's service name
			Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
			
			// Look up the server's RMI object
			serverIF = (ChatServerInterface)Naming.lookup("rmi://" + hostName + "/" + serviceName);	
		} catch (ConnectException  e) {
			// Handle connection exception
			JOptionPane.showMessageDialog(
					chatGUI.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
			// Log the exception
		} catch (NotBoundException | MalformedURLException me) {
			connectionProblem = true;
			me.printStackTrace();
			// Log the exception
		}
		if (!connectionProblem) {
			// Register the client with the server
			registerWithServer(details);
		}	
		System.out.println("Client Listen RMI Server is running...\n");
	}
	
	// Register the client with the server
	public void registerWithServer(String[] details) {		
		try {
			// Pass the client's identity to the server
			serverIF.passIDentity(this.ref); // This line may be redundant
			
			// Register the client's listener with the server
			serverIF.registerListener(details);			
		} catch (Exception e) {
			e.printStackTrace();
			// Log the exception
		}
	}
	
	// Receive a message from the server
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println(message);
		chatGUI.textArea.append(message);
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}
	
	// Update the user list
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {
		// If there is only one user, disable the private message button
		if (currentUsers.length < 2) {
			chatGUI.privateMsgButton.setEnabled(false);
		}
		
		// Remove the client panel from the user panel
		chatGUI.userPanel.remove(chatGUI.clientPanel);
		
		// Set the client panel with the current user list
		chatGUI.setClientPanel(currentUsers);
		
		// Repaint and revalidate the client panel
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}
}













