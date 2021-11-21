package de.hrw.dsalab.distsys.chat.network.udp.raw;

import de.hrw.dsalab.distsys.chat.data.Connection;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.exceptions.ClientException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

import static de.hrw.dsalab.distsys.chat.network.udp.raw.UdpNetwork.BUFFER_SIZE;

/**
 * This class is used by {@link UdpNetwork network} as a client module to handle traffic to a {@link UdpServerModule server} on a machine
 * @author Nils Milewski
 * @version 1.3
 * @since 2.5
 */
class UdpClientModule {
    private static final Logger logger = Logger.getLogger(UdpClientModule.class);

    /**
     * Ip address of the {@link UdpServerModule}
     */
    private final InetAddress serverAddress;

    /**
     * Port where the {@link UdpServerModule} listens
     */
    private final int port;

    /**
     * The underlying {@link UdpNetwork}
     */
    private final UdpNetwork network;

    /**
     * Used {@link DatagramSocket} to communicate with an {@link UdpServerModule}
     */
    private final DatagramSocket socket;

    /**
     * Buffer for {@link DatagramPacket}
     */
    private final byte[] buffer;

    /**
     * Construct a new client module based on {@link UdpNetwork} with a given {@link Connection}
     * @param network Underlying {@link UdpNetwork network}
     * @param connection Used {@link Connection connection}
     * @throws SocketException Can be thrown if the socket cannot be created
     * @throws UnknownHostException Is thrown if the configured address is unknown
     */
    UdpClientModule(UdpNetwork network, Connection connection) throws SocketException, UnknownHostException {
        this.network = network;
        this.socket = new DatagramSocket();
        this.buffer = new byte[BUFFER_SIZE];
        this.serverAddress = InetAddress.getByName(connection.getIp());
        this.port = connection.getServerPort();
    }

    /**
     * Starts a client module listener thread
     */
    public void start(){
        Thread thread = new Thread(this::run, this.getClass().getSimpleName());
        thread.setDaemon(true);
        thread.start();
        logger.info("Started module");
    }

    /**
     * Stops the client module
     */
    public void stop(){}

    /**
     * Sends a message to the remote {@link UdpServerModule server module}
     * @param message Message which should be sent
     * @throws ClientException Thrown if the network is not active or if the message cannot be delivered
     */
    public void send(Message message) throws ClientException {
        byte[] data = network.getDecoder().encode(message);
        internalSend(data);
    }

    /**
     * Sends a byte array to the targeted {@link UdpServerModule}
     * @param data Data which should be sent
     * @throws ClientException Thrown if send failed or the network is not active
     */
    private void internalSend(byte[] data) throws ClientException {
        try {
            if (network.isAlive().get()) {
                socket.send(new DatagramPacket(data, data.length, serverAddress, port));
            } else {
                throw new ClientException("Network is offline");
            }
        } catch (Exception ex) {
            throw new ClientException(ex);
        }
    }

    /**
     * Listener thread
     */
    public void run() {
        try {
            while (network.isAlive().get()) {
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                socket.receive(received);
                byte[] data = Arrays.copyOf(received.getData(), received.getLength());
                network.messageReceived(network.getDecoder().decode(data));
            }
        } catch (IOException ex) {
            network.exceptionOccurred(ex);
            logger.critical(ex.getMessage(), ex);
        }
    }

    /**
     * Sends a connect sequence to the {@link UdpServerModule}
     * @throws ClientException Thrown by {@link UdpClientModule#internalSend internalSend}
     */
    public void connect() throws ClientException {
        internalSend(GeneralUtils.CONNECT_SEQUENCE);
        network.connectionEstablished(serverAddress.getCanonicalHostName());
    }

    /**
     * Sends a disconnect sequence to the {@link UdpServerModule}
     * @throws ClientException Thrown by {@link UdpClientModule#internalSend internalSend}
     */
    public void disconnect() throws ClientException {
        internalSend(GeneralUtils.DISCONNECT_SEQUENCE);
        network.connectionLost();
    }
}