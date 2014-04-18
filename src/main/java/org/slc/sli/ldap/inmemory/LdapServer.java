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

    private static volatile LdapServerImpl instance = null;

    public static LdapServerImpl getInstance() {
        if (instance == null) {
            synchronized(LdapServer.class) {
                if (instance == null)
                    instance = new LdapServerImpl();
            }
        }
        return instance;
    }
}
