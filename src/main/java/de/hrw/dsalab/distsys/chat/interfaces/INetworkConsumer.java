package de.hrw.dsalab.distsys.chat.interfaces;

import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;

/**
 * This interface enables the functionality to be notified by {@link AbstractNetwork}
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 0.4
 */
public interface INetworkConsumer {
    /**
     * This method is called when the {@link AbstractNetwork} receives data.
     * @param message {@link Message}
     */
    void networkDataReceived(Message message);
}
