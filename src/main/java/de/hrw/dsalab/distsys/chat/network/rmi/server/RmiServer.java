package de.hrw.dsalab.distsys.chat.network.rmi.server;

import de.hrw.dsalab.distsys.chat.enumerations.CommandSequence;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.network.rmi.client.IRmiClient;
import de.hrw.dsalab.distsys.chat.utils.GeneralUtils;
import de.hrw.dsalab.distsys.chat.utils.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RmiServer extends UnicastRemoteObject implements IRmiServer{
    private static final Logger logger = Logger.getLogger(RmiServer.class);

    private final List<IRmiClient> clients = new ArrayList<>();

    public RmiServer() throws RemoteException{
        super();
    }

    private void notifyClients(byte[] data){
        logger.info("Data received, broadcast to clients");
        for (IRmiClient client : clients) {
            try {
                client.dataReceivedFromServer(data);
            }catch (RemoteException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void subscribe(IRmiClient subscriber) throws RemoteException {
        logger.info("Client connected");
        clients.add(subscriber);
    }

    @Override
    public void unsubscribe(IRmiClient subscriber) throws RemoteException {
        logger.info("Client disconnected");
        clients.remove(subscriber);
    }

    @Override
    public void sendMessage(byte[] data) throws RemoteException {
        if(GeneralUtils.checkCommand(data) == CommandSequence.REGULAR) {
            notifyClients(data);
        }
    }
}
