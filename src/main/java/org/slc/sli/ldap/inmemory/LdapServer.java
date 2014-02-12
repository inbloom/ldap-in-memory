package org.slc.sli.ldap.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates an in-memory LDAP server, which is hydrated from an LDIF file (an LDAP export),
 * using the UnboundedId java SDK.
 *
 * NOTE:  THIS CLASS IS NOT INTENDED FOR PRODUCTION USE, IT IS ONLY INTENDED FOR DEVELOPMENT USES.
 *
 * Created by tfritz on 12/30/13.
 */
public class LdapServer {
    private final static Logger LOG = LoggerFactory.getLogger(LdapServer.class);

    /**
     * Hide the default Constructor.
     */
    private LdapServer() {
    }

    /**
     * Holder is loaded on first execution of getInstance(), or on first access to INSTANCE; not before. *
     */
    private static class SingletonHolder {
        public static final LdapServerImpl INSTANCE = new LdapServerImpl();
    }

    /**
     * Returns the singleton instance. *
     */
    public static LdapServerImpl getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public LdapServerImpl getLdapServer() {
        return SingletonHolder.INSTANCE;
    }

}
