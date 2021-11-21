package de.hrw.dsalab.distsys.chat.utils.decoder.config;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.utils.exceptions.GeneralException;

import java.io.File;


/**
 * Represents an abstract definition for a new config decoder
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 1.0
 */
public interface IConfigDecoder {

    /**
     * Exports the {@link Configuration configuration} to a specific path.<br>
     * <b>NOTE</b> This method should always call {@link IConfigDecoder#exportConfiguration(Configuration, File)}
     * @param configuration {@link Configuration} object to export
     * @param path Path where the configuration should be exported to
     * @throws GeneralException Thrown iff an error occurred during exportation
     */
    void exportConfiguration(Configuration configuration, String path) throws GeneralException;

    /**
     * Exports the {@link Configuration configuration} to a specific file
     * @param configuration {@link Configuration} object to export
     * @param path Path where the configuration should be exported to
     * @throws GeneralException Thrown iff an error occurred during exportation
     */
    void exportConfiguration(Configuration configuration, File path) throws GeneralException;

    /**
     * Imports a {@link Configuration configuration } object from specified path
     * <b>NOTE</b> This method should always call {@link IConfigDecoder#importConfiguration(File)}
     * @param path Path to import from
     * @return new instance of {@link Configuration}
     * @throws GeneralException Thrown iff an error occurred during the import process
     */
    Configuration importConfiguration(String path) throws GeneralException;

    /**
     * Imports a {@link Configuration configuration } object from specified path
     * @param path Path to import from
     * @return new instance of {@link Configuration}
     * @throws GeneralException Thrown iff an error occurred during the import process
     */
    Configuration importConfiguration(File path) throws GeneralException;

    /**
     * Gets the configuration as a string
     * @param configuration – {@link Configuration} object to export
     * @return String representation of the {@link Configuration configuration}
     * @throws GeneralException Thrown iff an error occurred during the process
     */
    String getAsString(Configuration configuration) throws GeneralException;

    /**
     * This should return the extension of the underlying file.<br>
     * E.g. json for a JSON decoder and xml for an XML decoder
     * @return file extension
     */
    String extension();
}
