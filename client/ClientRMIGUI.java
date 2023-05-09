package client;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;

public class ClientRMIGUI extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;	
	private JPanel textPanel, inputPanel;
	private JTextField textField;
	private String name, message;
	private Font meiryoFont = new Font("Meiryo", Font.PLAIN, 14);
	private Border blankBorder = BorderFactory.createEmptyBorder(10,10,20,10);//top,r,b,l
	private ChatClient3 chatClient;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    protected JTextArea textArea, userArea;
    protected JFrame frame;
    protected JButton privateMsgButton, startButton, sendButton;
    protected JPanel clientPanel, userPanel;
	public static void main(String args[]){
		try{
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
				if("Nimbus".equals(info.getName())){
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch(Exception e){
			}
		new ClientRMIGUI();
		}
	// main class
	public ClientRMIGUI(){
		frame = new JFrame("Client Chat Console");	
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    System.exit(0);  
		    }   
		});
		Container c = getContentPane();
		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(getInputPanel(), BorderLayout.CENTER);
		outerPanel.add(getTextPanel(), BorderLayout.NORTH);
		c.setLayout(new BorderLayout());
		c.add(outerPanel, BorderLayout.CENTER);
		c.add(getUsersPanel(), BorderLayout.EAST);
		frame.add(c);
		frame.pack();
		frame.setAlwaysOnTop(true);
		frame.setLocation(150, 150);
		textField.requestFocus();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	// panel for seeing messages
	public JPanel getTextPanel(){
		String welcome = "Welcome enter your name and press Start to begin\n";
		textArea = new JTextArea(welcome, 14, 34);
		textArea.setMargin(new Insets(12, 12, 12, 12));
		textArea.setFont(meiryoFont);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textPanel = new JPanel();
		textPanel.add(scrollPane);
		textPanel.setFont(new Font("Meiryo", Font.PLAIN, 14));
		return textPanel;
	}
	// Add new message
	public JPanel getInputPanel(){
		inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		inputPanel.setBorder(blankBorder);	
		textField = new JTextField();
		textField.setFont(meiryoFont);
		inputPanel.add(textField);
		return inputPanel;
	}
	// List of users
	public JPanel getUsersPanel(){
		userPanel = new JPanel(new BorderLayout());
		String  userStr = " Current Users      ";
		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);	
		userLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
		String[] noClientsYet = {"No other users"};
		setClientPanel(noClientsYet);
		clientPanel.setFont(meiryoFont);
		userPanel.add(makeButtonPanel(), BorderLayout.SOUTH);		
		userPanel.setBorder(blankBorder);
		return userPanel;		
	}
    public void setClientPanel(String[] currClients) {  	
    	clientPanel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<String>();
        for(String s : currClients){
        	listModel.addElement(s);
        }
        if(currClients.length > 1){
        	privateMsgButton.setEnabled(true);
        }
        list = new JList<String>(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(8);
        list.setFont(meiryoFont);
        JScrollPane listScrollPane = new JScrollPane(list);
        clientPanel.add(listScrollPane, BorderLayout.CENTER);
        userPanel.add(clientPanel, BorderLayout.CENTER);
    }
	public JPanel makeButtonPanel() {		
		sendButton = new JButton("Send ");
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
        privateMsgButton = new JButton("Send PM");
        privateMsgButton.addActionListener(this);
        privateMsgButton.setEnabled(false);
		startButton = new JButton("Start ");
		startButton.addActionListener(this);
		JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
		buttonPanel.add(privateMsgButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(startButton);
		buttonPanel.add(sendButton);
		return buttonPanel;
	}
	@Override
	public void actionPerformed(ActionEvent e){
		try {
			// If the "Start" button is clicked
			if(e.getSource() == startButton){
				// Get the user's name from the text field
				name = textField.getText();
				// If the name is not empty
				if(name.length() != 0){
					// Set the title of the window to include the user's name
					frame.setTitle(name + "'s console ");
					// Clear the text field
					textField.setText("");
					// Add a message to the text area indicating that the user is connecting
					textArea.append("username : " + name + " connecting to chat...\n");
					// Connect to the chat server with the given name
					getConnected(name);
					// If the connection was successful
					if(!chatClient.connectionProblem){
						// Disable the "Start" button
						startButton.setEnabled(false);
						// Enable the "Send" button
						sendButton.setEnabled(true);
					}
				} else{
					// If the name is empty, display an error message
					JOptionPane.showMessageDialog(frame, "Enter your name to Start");
				}
			}
			// If the "Send" button is clicked
			if(e.getSource() == sendButton){
				// Get the message from the text field
				message = textField.getText();
				// Clear the text field
				textField.setText("");
				// Send the message to the chat server
				sendMessage(message);
				// Print a message to the console indicating that the message was sent
				System.out.println("Sending message : " + message);
			}
			// If the "Private Message" button is clicked
			if(e.getSource() == privateMsgButton){
				// Get the indices of the selected users from the list
				int[] privateList = list.getSelectedIndices();
				// For each selected user, print their index to the console
				for(int i=0; i<privateList.length; i++){
					System.out.println("selected index :" + privateList[i]);
				}
				// Get the message from the text field
				message = textField.getText();
				// Clear the text field
				textField.setText("");
				// Send the private message to the selected users
				sendPrivate(privateList);
			}
		} catch (RemoteException remoteExc) {			
			// If a remote exception occurs, print the stack trace
			remoteExc.printStackTrace();	
		}	
	}
	private void sendMessage(String chatMessage) throws RemoteException {
		// Update the chat history on the server with the given name and message
		chatClient.serverIF.updateChat(name, chatMessage);
	}
	private void sendPrivate(int[] privateList) throws RemoteException {
		// Construct the private message with the sender's name and message
		String cleanedUserName = userName.replaceAll("\\s+","_");
		cleanedUserName = userName.replaceAll("\\W+","_");
		try {		
			chatClient = new ChatClient3(this, cleanedUserName);
			chatClient.startClient();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}










