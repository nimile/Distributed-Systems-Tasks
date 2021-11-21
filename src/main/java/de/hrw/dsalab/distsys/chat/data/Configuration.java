package de.hrw.dsalab.distsys.chat.data;

import com.google.gson.Gson;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.decoder.config.IConfigDecoder;
import de.hrw.dsalab.distsys.chat.utils.decoder.config.JsonConfigDecoder;
import de.hrw.dsalab.distsys.chat.utils.decoder.config.XmlConfigDecoder;
import lombok.Getter;
import lombok.Setter;


import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents the application configuration.
 *
 * @author Nils Milewski
 * @version 1.1
 * @since 1.0
 */
@Getter @Setter
@XmlRootElement
public class Configuration implements Serializable {
    public static final Logger logger = Logger.getLogger(Configuration.class);

    /**
     * This attribute specifies the decoder, there two supported either Json oder XML
     */
    private static final IConfigDecoder decoder = new XmlConfigDecoder();

    /**
     * Default file path is <i>$executable$/data/config.$ext$</i>. The extension is determined by the used {@link IConfigDecoder decoder}
     */
    public static final String PATH = "./data/config" + decoder.extension();

    private User user = null;
    private Connection connection = new Connection();

    /**
     * Represents the message format. {@link Message#getMessage List of possible wildcars}
     */
    private String chatMessageFormat = "$dir$ [$date$ $time$] [$nick$]: $message$";

    private Configuration(){
        logger.config("Following decoder is used " + decoder.getClass());
    }

    /**
     * Attribute for the singleton pattern
     */
    private static AtomicReference<Configuration> instance = new AtomicReference<>(null);


    @Deprecated(since = "3.0", forRemoval = true)
    public static void readConfiguration(String path) {
        try {
            logger.config("Read configuration");
            instance.set(decoder.importConfiguration(path));
        } catch (Exception ex) {
            logger.warn("Cannot read configuration file, using default configuration", ex);
            instance.set(new Configuration());
        }
        logger.config("Server configuration is " + instance);
    }

    /**
     * Access the configuration object
     * @return Application configuration
     */
    public static Configuration getConfiguration(){
        if(null == instance.get()){
            importConfiguration();
        }
        return instance.getAcquire();
    }

    /**
     * Imports the configuration from ./data/config.ext
     */
    public static void importConfiguration() {
        try {
            logger.config("Read configuration");
            instance.set(decoder.importConfiguration(PATH));
        } catch (Exception ex) {
            logger.warn("Cannot read configuration file, using default configuration", ex);
            instance.set(new Configuration());
        }
        logger.config("Server configuration is " + instance);
    }


    /**
     * Exports the configuration to ./data/config.ext
     */
    public void export(){
        try {
            logger.config("Export configuration to " + PATH);
            decoder.exportConfiguration(this, PATH);
        } catch (Exception ex) {
            logger.warn("Cannot export configuration file", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Utilizes the {@link Gson#toJson} method to create a json representation of the object
     * @return Json String representation
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
