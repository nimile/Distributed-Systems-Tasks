package de.hrw.dsalab.distsys.chat.network.dummy;

import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.decoder.message.JsonMessageDecoder;
import de.hrw.dsalab.distsys.chat.data.Message;
import de.hrw.dsalab.distsys.chat.data.User;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DummyTraffic {

    public static final Logger logger = Logger.getLogger(DummyTraffic.class);
    private final AbstractNetwork network;
    public DummyTraffic(AbstractNetwork network){
        this.network = network;
    }

    public void send(Message message){
        int delay = Math.abs(new Random(System.currentTimeMillis()).nextInt() % 5000);
        logger.info("Send message with delay " + delay);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                network.messageReceived(message);
            }
        }, delay);
    }

    Timer jane = null;
    Timer john = null;

    public void doTrafficStuff(){
        jane = simulateClient("Jane", 5000, 1000);
        john = simulateClient("John", 5250, 2500);
    }

    public void stop(){
        if(null != jane){
            jane.cancel();
            jane.purge();
            jane = null;
        }

        if(null != john){
            john.cancel();
            john.purge();
            john = null;
        }
    }

    private Timer simulateClient(String name, int delay, int period){
        Timer timer = new Timer();
        AtomicInteger cnt = new AtomicInteger(0);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                User user = User.build(name);
                Message message = Message.builder().user(user).chatMessage("INBOUND DATA No. " + cnt.getAndIncrement()).build();
                byte[] dataOut = network.getDecoder().encode(message);
                Message msg = new JsonMessageDecoder().decode(dataOut);
                network.messageReceived(msg);
            }
        }, delay, period);
        return timer;
    }
}
