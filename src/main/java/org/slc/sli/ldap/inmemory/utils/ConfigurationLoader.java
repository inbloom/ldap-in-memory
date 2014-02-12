package org.slc.sli.ldap.inmemory.utils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slc.sli.ldap.inmemory.domain.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Created by tfritz on 1/2/14.
 *
 * <url>http://www.code-thrill.com/2012/05/configuration-that-rocks-with-apache.html</url>
 *
 */
public class ConfigurationLoader {
    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationLoader.class);

    public static Server load(final String configFile, final String configSchemaFile) throws Exception {
        LOG.debug(">>>ConfigurationLoader.loadConfiguration()");

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL schemaUrl = ConfigurationLoader.class.getClassLoader().getResource(configSchemaFile);

        if (schemaUrl == null) {
            throw new FileNotFoundException("The config schema file " + configSchemaFile + " was not found on the classpath.  This file should exist within the WAR.");
        }

        Schema schema = sf.newSchema(new File(schemaUrl.toURI()));
        JAXBContext context = JAXBContext.newInstance(Server.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(schema);
        URL configUrl = ConfigurationLoader.class.getClassLoader().getResource(configFile);

        if (configUrl == null) {
            throw new FileNotFoundException("The config file " + configFile + " was not found on the classpath.");
        }

        File file = new File(configUrl.toURI());

        Server server = (Server) unmarshaller.unmarshal(file);

        if (LOG.isDebugEnabled()) {
            LOG.debug(ToStringBuilder.reflectionToString(server, ToStringStyle.MULTI_LINE_STYLE));
        }

        LOG.debug("<<<ConfigurationLoader.loadConfiguration()");
        return server;
    }
}
