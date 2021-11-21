package de.hrw.dsalab.distsys.chat.network.socket;

import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;

import java.io.IOException;


/**
 * This class implements an {@link AbstractNetwork} using {@link java.net.Socket Sockets}
 * @author Nils Milewski
 * @version 1.1
 * @since 1.0
 */
public class SocketNetwork extends AbstractNetwork {
    /**
     * Describes the server module
     */
    private final SocketServerModule serverModule;

    /**
     * Describes the client module
     */
    private final SocketClientModule clientModule;

    public SocketNetwork(){
        super();
        serverModule = new SocketServerModule(this);
        clientModule = new SocketClientModule(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(Message msg) {
        super.notifyNetworkConsumer(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNetworkData(User user, byte[] data) {
        Message message = Message.builder().user(user).chatMessage(new String(data)).build();
        try {
            clientModule.send(super.getDecoder().encode(message));
        } catch (IOException ex) {
            logger.critical(ex.getMessage(), ex);
            connectionLost();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        serverModule.start();
        clientModule.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        clientModule.disconnect();
        serverModule.stop();
    }
}
