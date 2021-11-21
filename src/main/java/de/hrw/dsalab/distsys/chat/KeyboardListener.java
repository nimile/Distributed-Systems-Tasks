package de.hrw.dsalab.distsys.chat;

import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.interfaces.InputListener;

import javax.swing.*;

/**
 * This class was provided as inside the base project
 * @version 1.0
 * @since 0.1
 */
public class KeyboardListener implements InputListener {

	private final JTextArea textArea;
	private final String nick;
	
	public KeyboardListener(JTextArea textArea, String nick) {
		this.textArea = textArea;
		this.nick = nick;
	}
	
	@Override
	public void inputReceived(Message str) {
		textArea.append("<" + nick + "> " + str.getChatMessage() + System.getProperty("line.separator"));
	}

}
