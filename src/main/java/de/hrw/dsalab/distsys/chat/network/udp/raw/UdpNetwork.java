package de.hrw.dsalab.distsys.chat.network.udp.raw;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Connection;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.utils.exceptions.ClientException;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;

import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements an {@link AbstractNetwork} using {@link DatagramSocket UDP}
 * @author Nils Milewski
 * @version 1.3
 * @since 2.5
 */
public class UdpNetwork extends AbstractNetwork {

    /**
     * Default buffer size for {@link DatagramPacket}
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Describes the server module
     */
    private UdpServerModule serverModule;

    /**
     * Describes the client module
     */
    private UdpClientModule clientModule;

    /**
     * Describes the network configuration
     */
    private Connection connection;

    /**
     * Determines if the network is active or not
      */
    private final AtomicBoolean alive = new AtomicBoolean();

    public UdpNetwork(){
        super();
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
        try {
            Message message = Message.builder().user(user).chatMessage(new String(data)).build();
            clientModule.send(message);
        } catch (ClientException ex) {
            logger.warn(ex.getMessage(), ex);
            exceptionOccurred(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() throws NetworkInitializeException {
        super.init();
        connection = Configuration.getConfiguration().getConnection();
        try {
            if(connection.isServer()) {
                serverModule = new UdpServerModule(this, connection);
            }
            clientModule = new UdpClientModule(this, connection);
        } catch (SocketException | UnknownHostException ex) {
            logger.critical("Cannot start server module", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws NetworkInitializeException {
        if (alive.get()) {
            return;
        }
        try {
            init();
            alive.set(true);
            if (connection.isServer() && null != serverModule) {
                serverModule.start();
            }
            clientModule.start();
            clientModule.connect();
        } catch (ClientException ex) {
            if (connection.isServer() && null != serverModule) {
                serverModule.stop();
            }
            clientModule.stop();
            isAlive().set(false);
            logger.critical(ex.getMessage(), ex);
            exceptionOccurred(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if(!alive.get()){
            return;
        }
        alive.set(false);
        clientModule.stop();
        if(connection.isServer() && null != serverModule) {
            serverModule.stop();
        }
    }

    /**
     * Access the state of the network.<br>
     * If returned false the network is inactive.
     * @return True iff the network is active
     */
    public AtomicBoolean isAlive() {
        return alive;
    }
}
