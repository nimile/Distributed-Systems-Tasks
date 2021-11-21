package de.hrw.dsalab.distsys.chat;

import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.interfaces.INetworkConsumer;
import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;


import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;

/**
 * This is the UI of the chat<br>
 * A basic code was provided but soon refactored and features were added
 * @version 3.1.2
 * @since 0.1
 */
public class Chat extends JFrame implements INetworkConsumer {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Used as an abstraction for the future {@link de.hrw.dsalab.distsys.chat.enumerations.NetworkTypes network} implementation
	 */
	private final transient AbstractNetwork network;

	/**
	 * The base used a simple string as user, but it was refactored in a bit more {@link User complex structure}
	 */
	private final transient User user;

	/**
	 * Used for QOL improvements, it will store the previous sent text
	 */
	private String lastText = "";

	public Chat(AbstractNetwork network) {
		this.network = network;
		JPanel mainPanel;
		
		setTitle("Chat Tool v0.1");
		setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);

		// Checks if the use is configured inside the configuration file
		User tmpUser = Configuration.getConfiguration().getUser();
		// If no user exists create a new one
		if(null ==  tmpUser || tmpUser.getUid() == null){
			String nick = JOptionPane.showInputDialog(this, "Enter your nickname please:", "Enter nickname", JOptionPane.QUESTION_MESSAGE);
			Configuration.getConfiguration().setUser(User.build(nick));
		}
		user = Configuration.getConfiguration().getUser();
		// Title is set to the user object with the name of the network
		setTitle(getTitle() + " " + user.getNick() + "(" + user.getUid() + ")" + network.getClass().getSimpleName());

		mainPanel = setupChatView();
		getContentPane().add(mainPanel);
		getContentPane().getParent().invalidate();
		getContentPane().validate();

		// Subscribe to the network consumer list
		network.subscribe(this);
	}
	private final JTextArea textArea = new JTextArea();
	private final JTextField textField = new JTextField();

	private JPanel setupChatView() {
		JPanel panel = new JPanel();
		JPanel southPanel = new JPanel();
		JButton sendButton = new JButton("Send");

		// Added to clear the chat
		JButton clearButton = new JButton("clear");

		textField.setColumns(55);

		// Added to utilize to switch between the last and current text
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN){
					String tmp = textField.getText();
					textField.setText(lastText);
					lastText = tmp;
				}
				super.keyReleased(e);
			}
		});
		// Refactored into new method
		sendButton.addActionListener(this::performSending);

		// Enables sending via enter key
		textField.addActionListener(this::performSending);

		textArea.setBackground(Color.LIGHT_GRAY);
		textArea.setEditable(false);

		clearButton.addActionListener(event -> textArea.setText(""));

		// Added to enable scrolling inside the textarea
		JScrollPane scroll = new JScrollPane (textArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		southPanel.setLayout(new FlowLayout());
		southPanel.add(textField);

		southPanel.add(sendButton);
		southPanel.add(clearButton);
		
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}


	/**
	 * This method handles the sending of a message<br>
	 * A message won't be sent if the text is empty<br>
	 * If to connect or disconnect is entered the network will do the equivalent methods
	 * @param event ...
	 */
	private void performSending(ActionEvent event){
		if (textField.getText().isEmpty() || textField.getText().isBlank()) {
			return;
		}
		lastText = textField.getText();
		if(textField.getText().equalsIgnoreCase("connect")){
			try {
				network.start();
			} catch (NetworkInitializeException e) {
				network.exceptionOccurred(e);
			}
		}else if(textField.getText().equalsIgnoreCase("disconnect")){
			network.stop();
		}else {
			network.sendNetworkData(user, textField.getText().getBytes(StandardCharsets.UTF_8));
		}
		textField.setText("");
	}

	/**
	 * Receives data from the {@link AbstractNetwork notifyNetworkConsumer} object
	 * @param message {@link Message received message}
	 */
	@Override
	public void networkDataReceived(Message message) {
		textArea.append(message.getMessage() + System.lineSeparator());
	}
}
