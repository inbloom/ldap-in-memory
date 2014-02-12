package org.slc.sli.ldap.inmemory;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Harness to run the LDAP sever locally, within the IDE.  To shut down the LDAP server, terminate the class/thread.
 * Created by tfritz on 1/9/14.
 */
public class LdapServerSingletonHarness {
    public static void main(String[] args) {
        System.out.println(">>>LdapServerSingletonHarness.main()");
        try {
            LdapServer.getInstance().start();
            System.out.println("   startLdapServer entry count: " + LdapServer.getInstance().getInMemoryDirectoryServer().countEntries());
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
        System.out.println("<<<LdapServerSingletonHarness.main()");
    }
}
