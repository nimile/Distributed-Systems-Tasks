import de.hrw.dsalab.distsys.chat.utils.decoder.config.XmlConfigDecoder;
import de.hrw.dsalab.distsys.chat.utils.exceptions.GeneralException;
import org.junit.jupiter.api.Test;

import java.io.File;

public class xmlTest {
    @Test
    public void run(){
        XmlConfigDecoder decoder = new XmlConfigDecoder();
        try {
            var config = decoder.importConfiguration(new File("./data/config.xml"));
            System.out.println(decoder.getAsString(config));
            decoder.exportConfiguration(config, new File("./data/config2.xml"));
            System.out.println(config);
        } catch (GeneralException e) {
            e.printStackTrace();
        }
    }
}
