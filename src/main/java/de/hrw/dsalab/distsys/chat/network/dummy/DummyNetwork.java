package de.hrw.dsalab.distsys.chat.network.dummy;

import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;


/**
 * This class implements an {@link AbstractNetwork} using dummy data
 * @author Nils Milewski
 * @version 1.0
 * @since 0.1
 */
public class DummyNetwork extends AbstractNetwork {
    private final DummyTraffic dummyTraffic = new DummyTraffic(this);

    @Override
    public void messageReceived(Message msg) {
        super.notifyNetworkConsumer(msg);
    }
    private boolean online = false;
    @Override
    public void sendNetworkData(User user, byte[] data) {
        if(!online){
            Message message = Message.builder().user(user).chatMessage(new String(data)).build();
            notifyNetworkConsumer(message);
            return;
        }
        Message message = Message.builder().user(user).chatMessage(new String(data)).build();
        dummyTraffic.send(message);
    }

    @Override
    public void start() {
        if(online)return;
        online = true;
        logger.info("Starting " + this.getClass().getSimpleName());
        dummyTraffic.doTrafficStuff();

    }

    @Override
    public void stop() {
        if(!online)return;
        online = false;
        logger.info("Stopping " + this.getClass().getSimpleName());
        dummyTraffic.stop();
    }
}
