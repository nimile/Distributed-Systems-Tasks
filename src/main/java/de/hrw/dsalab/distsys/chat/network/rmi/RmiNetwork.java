package de.hrw.dsalab.distsys.chat.network.rmi;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.data.Connection;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;

import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.network.rmi.client.ClientModule;
import de.hrw.dsalab.distsys.chat.network.rmi.server.ServerModule;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;


import java.util.concurrent.atomic.AtomicBoolean;


public class RmiNetwork extends AbstractNetwork {
    private final Logger logger = Logger.getLogger(RmiNetwork.class);

    private final AtomicBoolean alive = new AtomicBoolean(false);
    private ClientModule clientModule;
    private ServerModule serverModule;
    private Connection connection;

    @Override
    public void init() throws NetworkInitializeException {
        try {
            logger.info("Read configuration");
            connection = Configuration.getConfiguration().getConnection();

            logger.info("Initialize client module");
            clientModule = new ClientModule(this);

            if(connection.isServer()) {
                logger.info("Initialize server module");
                serverModule = new ServerModule();
            }
        }catch (Exception ex){
            throw new NetworkInitializeException(ex);
        }
    }

    @Override
    public void messageReceived(Message msg) {
        super.notifyNetworkConsumer(msg);
    }

    @Override
    public void sendNetworkData(User user, byte[] data) {
        if(!alive.get()){
            return;
        }
        Message message = Message.builder().user(user).chatMessage(new String(data)).build();
        clientModule.sendMessage(message);
    }

    @Override
    public void start() throws NetworkInitializeException {
        if(alive.get()){
            return;
        }

        try {
            logger.info("Starting network");
            alive.set(true);
            init();
            if(connection.isServer()) {
                serverModule.start();
            }
            clientModule.start();

            logger.info("Network started");
        } catch (Exception ex) {
            alive.set(false);
            logger.critical("Cannot start network", ex);
            throw new NetworkInitializeException(ex);
        }
    }

    @Override
    public void stop() {
        if(!alive.get()){
            return;
        }

        logger.info("Stopping network");
        try {
            alive.set(false);
            if(connection.isServer()) {
                serverModule.stop();
            }
            clientModule.stop();

            logger.info("Network stopped");
        } catch (Exception ex) {
            logger.critical("Cannot stop network", ex);
            exceptionOccurred(ex);
        }
    }

    /**
     * Access activity the state of the network
     * @return True when the network is active
     */
    public AtomicBoolean getAlive() {
        return alive;
    }
}
