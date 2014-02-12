package org.slc.sli.ldap.inmemory.utils;

import junit.framework.Assert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.slc.sli.ldap.inmemory.LdapServerImpl;
import org.slc.sli.ldap.inmemory.domain.Server;


/**
 * Created by tfritz on 1/2/14.
 */
public class ConfigurationLoaderTest {
    @Test
    public void loadConfiguration() {
        System.out.println(">>>ConfigurationLoaderTest.loadConfiguration()");
        try {
            String configFile = LdapServerImpl.CONFIG_FILE;
            String schemaFile = LdapServerImpl.CONFIG_SCHEMA_FILE;
            System.out.println("   configFile: " + configFile);
            System.out.println("   schemaFile: " + schemaFile);
            Server config = ConfigurationLoader.load(configFile, schemaFile);
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            Assert.assertTrue(false);  //fail test
        }
        System.out.println("<<<ConfigurationLoaderTest.loadConfiguration()");
    }
}
