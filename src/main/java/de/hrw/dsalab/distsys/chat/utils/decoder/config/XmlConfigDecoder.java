package de.hrw.dsalab.distsys.chat.utils.decoder.config;

import de.hrw.dsalab.distsys.chat.data.Configuration;
import de.hrw.dsalab.distsys.chat.utils.Logger;
import de.hrw.dsalab.distsys.chat.utils.exceptions.ConfigurationException;
import de.hrw.dsalab.distsys.chat.utils.exceptions.GeneralException;
import de.hrw.dsalab.distsys.chat.utils.exceptions.NotImplementedException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Represents a concrete implementation of the {@link IConfigDecoder}
 *
 * @author Nils Milewski
 * @version 1.0
 * @since 1.0
 */
public class XmlConfigDecoder implements IConfigDecoder {
    private static final Logger logger = Logger.getLogger(XmlConfigDecoder.class);
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
        try {
            String data = getAsString(configuration);
            FileUtils.write(path, data, Charset.defaultCharset());
        } catch (Exception ex) {
            throw new GeneralException(ex);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(Configuration configuration) throws GeneralException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Configuration.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<!DOCTYPE configuration SYSTEM \"config.dtd\">");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            StringWriter writer = new StringWriter();
            marshaller.marshal(configuration, writer);
            return writer.toString();
        } catch (Exception ex) {
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
        try {
            validate(path);
            JAXBContext xmlContext = JAXBContext.newInstance(Configuration.class);
            XMLInputFactory xmlFactory = XMLInputFactory.newFactory();
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

            XMLStreamReader xsr = xmlFactory.createXMLStreamReader(new StreamSource(path));
            Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
            return (Configuration) unmarshaller.unmarshal(xsr);
        } catch (ConfigurationException | JAXBException | XMLStreamException ex) {
            throw new GeneralException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extension() {
        return ".xml";
    }


    private void validate(File file) throws ConfigurationException {
        try {
            logger.info("Validate " + file.getAbsolutePath());
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(new XmlErrorHandler());
            builder.parse(file);
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }


    private static class XmlErrorHandler implements ErrorHandler{

        void rethrow(Exception ex) throws ConfigurationException {
            throw new ConfigurationException(ex);
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            logger.warn(exception.getMessage(), exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw new SAXException(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw new SAXException(exception);
        }
    }
}
