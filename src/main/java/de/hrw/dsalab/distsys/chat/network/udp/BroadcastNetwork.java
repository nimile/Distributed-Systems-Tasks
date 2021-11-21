package de.hrw.dsalab.distsys.chat.network.udp;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Connection;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkException;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements an {@link AbstractNetwork} using {@link DatagramSocket Broadcast}
 * @author Nils Milewski
 * @version 1.2
 * @since 2.1
 */
public class BroadcastNetwork extends AbstractNetwork {
    /**
     * Define the size for a UDP packet
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * Defines the destination port of a packet
     */
    protected int port;

    /**
     * Defines the destination address of a packet
     */
    protected InetAddress ip;

    /**
     * This is the {@link DatagramSocket} which is used to interact with the physical network
     */
    protected DatagramSocket serverSocket = null;

    /**
     * Buffer for outgoing data
     */
    private final byte[] buffer = new byte[BUFFER_SIZE];

    /**
     * States that the module is active
     */
    protected final AtomicBoolean alive = new AtomicBoolean(false);

    /**
     * This method waits for incoming packets.<br>
     * If a packet is received it will construct a new message and calls the {@link BroadcastNetwork#messageReceived}
     */
    private void listen(){
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (alive.get()) {
                serverSocket.receive(packet);
                if(packet.getLength() > 0) {
                    byte[] received = Arrays.copyOf(packet.getData(), packet.getLength());
                    Message msg = getDecoder().decode(received);
                    messageReceived(msg);
                }
            }
        }catch (Exception ex){
            logger.warn("An exception occurred while listening", ex);
            exceptionOccurred(ex);
        }
    }

    /**
     * This method is inherited by {@link de.hrw.dsalab.distsys.chat.interfaces.NetworkListener} and calls the {@link AbstractNetwork#notifyNetworkConsumer notifyNetworkConsumer} method
     * @param msg Used {@link Message message}
     */
    @Override
    public void messageReceived(Message msg) {
        super.notifyNetworkConsumer(msg);
    }

    /**
     * This method constructs a new {@link Message message} based on {@link User user} and {@link Byte data}.<br>
     * The newly created message is send over the provided {@link DatagramSocket socket}
     * @param socket {@link DatagramSocket socket} where the message should be used to sent
     * @param user {@link User user} who is sending
     * @param data {@link Byte data} array
     * @throws IOException Thrown iff the data cannot be sent
     */
    protected void internalSend(DatagramSocket socket, User user, byte[] data) throws IOException{
        if(!alive.get()){
            throw new NetworkException();
        }
        Message message = Message.builder().user(user).chatMessage(new String(data)).build();
        byte[] dataOut = super.getDecoder().encode(message);
        socket.send(new DatagramPacket(dataOut, dataOut.length, ip, port));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNetworkData(User user, byte[] data) {
        try(DatagramSocket socket = new DatagramSocket()) {
            internalSend(socket, user, data);
        } catch (Exception ex) {
            logger.warn("An Exception occurred during sending. ", ex);
            connectionLost();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() throws NetworkInitializeException {
        super.init();
        Connection connection = Configuration.getConfiguration().getConnection();
        try {
            ip = InetAddress.getByName(connection.getIp());
            port = connection.getServerPort();
        } catch (UnknownHostException ex) {
            logger.critical("A critical exception occurred during initialization", ex);
            throw new NetworkInitializeException(ex);
        }
    }

    /**
     * Initializes the {@link BroadcastNetwork#serverSocket}
     * @throws IOException Thrown iff the {@link BroadcastNetwork#serverSocket} cannot be created
     */
    protected void initSocket() throws IOException {
        serverSocket = new DatagramSocket(port);
        serverSocket.setReuseAddress(true);
    }

    /**
     * Initializes the listener thread
     */
    protected void initListener(){
        Thread thread = new Thread(this::listen);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws NetworkInitializeException {
        if(alive.get()){
            return;
        }
        try {
            init();
            initSocket();
            initListener();
            alive.set(true);
            connectionEstablished(ip.getCanonicalHostName());
        } catch (Exception ex) {
            exceptionOccurred(ex);
            alive.set(false);
            throw new NetworkInitializeException(ex);
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
        connectionLost();
    }
}
