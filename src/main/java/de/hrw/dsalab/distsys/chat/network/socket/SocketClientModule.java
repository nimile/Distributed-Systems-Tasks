package de.hrw.dsalab.distsys.chat.network.socket;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Message;

import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used by {@link SocketNetwork network} as a client module to handle traffic to a {@link SocketServerModule server} on a machine
 * @author Nils Milewski
 * @version 1.2
 * @since 1.2
 */
class SocketClientModule {
    public static final Logger logger = Logger.getLogger(SocketClientModule.class);

    /**
     * States that the client is connected to the targeted {@link SocketServerModule}
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * The underlying {@link SocketNetwork}
     */
    private final SocketNetwork network;

    /**
     * Ip address of the {@link SocketServerModule}
     */
    private InetAddress ip;

    /**
     * Port where the {@link SocketServerModule} listens
     */
    private int port;

    /**
     * {@link Socket} to communicate with {@link SocketServerModule}
     */
    private Socket socket;

    SocketClientModule(SocketNetwork network){
        this.network = network;
    }

    /**
     * Initializes the module
     * @throws UnknownHostException Thrown if the ip address is invalid
     */
    void init() throws UnknownHostException {
        var connection = Configuration.getConfiguration().getConnection();
        ip = InetAddress.getByName(connection.getIp());
        port = connection.getServerPort();
    }

    /**
     * Sends a connect sequence to the {@link SocketServerModule}
     */
    void connect() {
        try {
            if(connected.get()){
                throw new IOException("Not connected to server");
            }
            init();
            socket = new Socket(ip, port);
            Thread listener = new Thread(this::listen, "Client thread");
            listener.setDaemon(true);
            listener.start();
            connected.set(true);

            logger.info("Sending connect sequence " + Arrays.toString(GeneralUtils.CONNECT_SEQUENCE));
            send(GeneralUtils.CONNECT_SEQUENCE);
            network.connectionEstablished(ip.getCanonicalHostName());
        } catch (IOException ex) {
            logger.critical(ex.getMessage(), ex);
            network.connectionLost();
        }
    }

    /**
     * Sends a disconnect sequence to the {@link SocketServerModule}
     */
    void disconnect(){
        if(!connected.get()) {
            return;
        }
        try {
            send(GeneralUtils.DISCONNECT_SEQUENCE);
        } catch (IOException ex){
            logger.info("Disconnect error occurred");
        }
        connected.set(false);
        network.connectionLost();
    }

    /**
     * Sends a byte array to the targeted {@link SocketServerModule}
     * @param data Data which should be sent
     * @throws IOException Thrown if the data cannot be sent
     */
    void send(byte[] data) throws IOException {
        if (!connected.get()) {
            throw new IOException("Client not connected");
        }
        socket.getOutputStream().write(data);
    }

    /**
     * Listener thread
     */
    @SuppressWarnings("StatementWithEmptyBody")
    void listen(){
        try{
            InputStream is = socket.getInputStream();
            while(connected.get()){
                while(is.available() <= 0 && connected.get());
                int length = is.available();
                if(length > 0){
                    byte[] data = is.readNBytes(length);
                    Message msg = network.getDecoder().decode(data);
                    network.messageReceived(msg);
                }
            }
            socket.close();
        }catch (Exception ex){
            logger.critical(ex.getMessage(), ex);
            disconnect();
        }
    }
}
