package org.slc.sli.ldap.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by tfritz on 12/26/13.
 */

public class LdapInMemoryContextListener implements ServletContextListener {
    private final static Logger LOG = LoggerFactory.getLogger(LdapInMemoryContextListener.class);

    ServletContext context;

    public void contextInitialized(ServletContextEvent event) {
        try {
            LdapServer.getInstance().start();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        LdapServer.getInstance().stop();
    }

}
