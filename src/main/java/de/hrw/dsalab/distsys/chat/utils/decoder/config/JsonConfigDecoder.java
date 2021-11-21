package de.hrw.dsalab.distsys.chat.utils.decoder.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.exceptions.GeneralException;

import java.io.*;

/**
 * Represents a concrete implementation of the {@link IConfigDecoder}
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 1.0
 */
public class JsonConfigDecoder implements IConfigDecoder{
    private static final Logger logger = Logger.getLogger(JsonConfigDecoder.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportConfiguration(Configuration configuration, String path) throws GeneralException {
        exportConfiguration(configuration, new File(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportConfiguration(Configuration configuration, File path) throws GeneralException {
        logger.config("Export configuration to " + path.getAbsolutePath());
        String json = getAsString(configuration);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path))){
            bw.write(json);
            bw.flush();
        } catch (IOException ex) {
            logger.critical("An error occurred during exportation", ex);
            throw new GeneralException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration importConfiguration(String path) throws GeneralException {
        return importConfiguration(new File(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration importConfiguration(File path) throws GeneralException {
        logger.config("Import configuration to " + path.getAbsolutePath());

        Configuration config;
        try(BufferedReader bw = new BufferedReader(new FileReader(path))){
            Gson gson = new Gson();
            config = gson.fromJson(bw, Configuration.class);
        } catch (IOException ex) {
            logger.critical("An error occurred during exportation", ex);
            throw new GeneralException(ex);
        }
        return config;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(Configuration configuration) throws GeneralException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extension() {
        return ".json";
    }
}
