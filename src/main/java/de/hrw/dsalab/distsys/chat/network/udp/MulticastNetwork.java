package de.hrw.dsalab.distsys.chat.network.udp;

import de.hrw.dsalab.distsys.chat.data.User;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;

import java.io.IOException;
import java.net.*;

/**
 * This class implements an {@link AbstractNetwork} using {@link BroadcastNetwork Multicast}
 * @author Nils Milewski
 * @version 1.2
 * @since 2.2
 */
public class MulticastNetwork extends BroadcastNetwork {

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNetworkData(User user, byte[] data) {
        try(MulticastSocket socket = new MulticastSocket()) {
            super.internalSend(socket, user, data);
        } catch (Exception ex) {
            logger.warn("An Exception occurred during sending. ", ex);
            connectionLost();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSocket() throws IOException {
        serverSocket = new MulticastSocket(super.port);
        serverSocket.setReuseAddress(true);
        ((MulticastSocket)serverSocket).joinGroup(super.ip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        try {
            ((MulticastSocket)serverSocket).leaveGroup(ip);
            super.stop();
        } catch (IOException ex) {
            exceptionOccurred(ex);
        }
    }
}
