import de.hrw.dsalab.distsys.chat.Chat;
import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.enumerations.NetworkTypes;
import de.hrw.dsalab.distsys.chat.network.AbstractNetwork;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NetworkInitializeException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.io.File;

/**
 * This is the entry point for the application.<br>
 * @version 3.1.2
 * @since 0.2
 */
public class Main {
    public static File getFilePathDebug(String rootPath, String... options){
        String mode = (String)JOptionPane.showInputDialog(
                null,
                "Select your SERVER configuration",
                "Server Configuration chooser",
                JOptionPane.QUESTION_MESSAGE,
                null, options,
                null);
        String fp = String.format(rootPath, mode);
        File file  = new File(fp);
        if(file.exists()){
            return file;
        }
        return null;
    }

    public static void runDebug(File file){
        if(null == file){
            return;
        }
        Configuration.readConfiguration(file.getAbsolutePath());

        // Code below this line is actual main code for production usage
        //Configuration.importConfiguration();

        NetworkTypes networkTypes = Configuration.getConfiguration().getConnection().getNetworkTypes();
        AbstractNetwork network = AbstractNetwork.getNetwork(networkTypes);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(network)));

        try {
            new Chat(network);
            network.start();
        } catch (NetworkInitializeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "A fatal exception occurred please restart the application\n" + ExceptionUtils.getStackTrace(ex),
                    "Fatal exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        runDebug(getFilePathDebug("./data/%s/server/config.xml", "socket", "udp", "broadcast", "multicast", "rmi", "quit"));
    }

    public static void shutdown(AbstractNetwork network){
        network.stop();
        Configuration.getConfiguration().export();
    }
}
