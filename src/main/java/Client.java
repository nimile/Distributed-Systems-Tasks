import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;

/**
 * This is the entry point for the client application.<br>
 * The client and server code shares a common entry therefore it utilizes core elements from the {@link Main} class
 * @version 3.1.2
 * @since 0.2
 */
public class Client {


    public static void main(String[] args) {
        Main.runDebug(Main.getFilePathDebug("./data/%s/client/config.xml", "socket", "udp", "rmi", "quit"));
    }

    public static void shutdown(AbstractNetwork network){
        network.stop();
        Configuration.getConfiguration().export();
    }
}
