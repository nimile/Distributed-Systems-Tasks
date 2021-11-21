package de.hrw.dsalab.distsys.chat.interfaces;

import de.hrw.dsalab.distsys.chat.data.Message;

/**
 * This interface was provided as inside the base project
 * @version 1.0
 * @since 0.1
 */
public interface NetworkListener {

	/**
	 * Should be invoked when a message is received from the network
	 * @param msg Received message
	 */
	void messageReceived(Message msg);
}
