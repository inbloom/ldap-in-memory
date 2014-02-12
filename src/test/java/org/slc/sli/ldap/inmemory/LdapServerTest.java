package org.slc.sli.ldap.inmemory;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedRequest;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedResult;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by tfritz on 1/2/14.
 */
public class LdapServerTest {

    private static int ROOT_ENTITY_COUNT = 683;
    private static int LEA_ADMIN_COUNT = 13;

    /**
     * Starts the internal LDAP server before running unit tests.  Uses resources found within the test tree.  Also uses the test classpath.
     */
    @BeforeClass
    public static void startLdapServer() {
        System.out.println(">>>LdapServerTest.startLdapServer()");
        try {
            LdapServer.getInstance().start();
            System.out.println("   startLdapServer entry count: " + LdapServer.getInstance().getInMemoryDirectoryServer().countEntries());
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            System.exit(1); //if exception occurs on startup exit the tests.
        }
        System.out.println("<<<LdapServerTest.startLdapServer()");
    }

    /**
     * This test ensures the LDAP server is started.
     */
    @Test
    public void testLdapServerStarted() {
        System.out.println(">>>LdapServerTest.testLdapServerStarted()");
        boolean isStarted = LdapServer.getInstance().isStarted();
        if (!isStarted) {
            System.exit(1); //exit if server cannot startup.
        }
        System.out.println("<<<LdapServerTest.testLdapServerStarted()");
    }

    /**
     *  Tests ability to create connection locally to LDAPhe  server.
     */
    @Test
    public void createLocalLdapConnection() {
        System.out.println(">>>LdapServerTest.createLocalLdapConnection()");
        boolean result = Boolean.FALSE;
        LDAPConnection connection = null;
        try {
            if (LdapServer.getInstance().getInMemoryDirectoryServer() == null) {
                System.out.println("   InMemoryDirectoryServer is NULL...");
            }
            connection = LdapServer.getInstance().getInMemoryDirectoryServer().getConnection();
            result = Boolean.TRUE;
        } catch (LDAPException lse) {
            System.out.println("   message = " + lse.getMessage());
            System.out.println("   exceptionMessage = " + lse.getExceptionMessage());
            System.out.println("   diagnosticMessage = " + lse.getDiagnosticMessage());
            System.out.println("   resultCode = " + lse.getResultCode());
            System.out.println("   errorMessageFromServer = +" + lse.getDiagnosticMessage());
            System.err.println(ExceptionUtils.getStackTrace(lse));
            result = Boolean.FALSE;
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.close();
            }
        }
        Assert.assertTrue(result);
        System.out.println("<<<LdapServerTest.createLocalLdapConnection()");
    }

    /**
     * Tests connecting and searching for the root DSE.
     */
    @Test
    public void getRootDSE() {
        System.out.println(">>>LdapServerTest.getRootDSE()");
        boolean result = Boolean.FALSE;
        LDAPConnection connection = null;
        try {
            int entryCount = LdapServer.getInstance().getInMemoryDirectoryServer().countEntries();

            System.out.println("   getRootDSEentry count: " + entryCount);
            Assert.assertEquals(entryCount, LdapServerTest.ROOT_ENTITY_COUNT);

            /* Establish a secure connection using the socket factory. */
            connection = LdapServer.getInstance().getInMemoryDirectoryServer().getConnection();
            RootDSE rootDSE = connection.getRootDSE();
            String rootDseToString =  ToStringBuilder.reflectionToString(rootDSE, ToStringStyle.SHORT_PREFIX_STYLE);

            System.out.println("   rootDSE: " + rootDseToString);
            Assert.assertTrue(!StringUtils.isEmpty(rootDseToString));
            result = Boolean.TRUE;
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
            result = Boolean.FALSE;
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.close();
            }
        }
        Assert.assertTrue(result);
        System.out.println("<<<LdapServerTest.getRootDSE()");
    }

    /**
     * Tests connecting and searching for LEA Administrator userts.
     */
    @Test
    public void showEntries() {
        System.out.println("<<<LdapServerTest.showSections()");
        /* Construct a filter that can be used to find specific entries, and then create a search
        request to find all such users in the directory. */
        boolean result = Boolean.FALSE;
        LDAPConnection connection = null;
        Filter filter = Filter.createEqualityFilter("cn", "LEA Administrator");
        SearchRequest searchRequest = new SearchRequest("dc=slidev,dc=org", SearchScope.SUB, filter);
        SearchResult searchResult;

        try {
            connection = LdapServer.getInstance().getInMemoryDirectoryServer().getConnection();
            searchResult = connection.search(searchRequest);
            int resultCount = searchResult.getEntryCount();
            Assert.assertEquals(resultCount, LdapServerTest.LEA_ADMIN_COUNT);
            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                System.out.println("    entry: " + ToStringBuilder.reflectionToString(entry, ToStringStyle.DEFAULT_STYLE));
                //  Attribute(name=userPassword, values={'{MD5}LUOaIWq99K/a23tT6zJWDg=='})
            }
            result = Boolean.TRUE;
            System.out.println("   searchResult entryCount: " + resultCount);
        } catch (LDAPSearchException lse) {
            // The search failed for some reason.
            searchResult = lse.getSearchResult();
            ResultCode resultCode = lse.getResultCode();
            String errorMessageFromServer = lse.getDiagnosticMessage();
            System.err.println(ToStringBuilder.reflectionToString(searchResult, ToStringStyle.SIMPLE_STYLE));
            System.err.println(ToStringBuilder.reflectionToString(resultCode, ToStringStyle.SIMPLE_STYLE));
            System.err.println(errorMessageFromServer);
            System.err.println(ExceptionUtils.getStackTrace(lse));
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.close();
            }
        }
        Assert.assertTrue(result);
        System.out.println("<<<LdapServerTest.showSections()");
    }

    /**
     * Test ability to change each entity's password, who has the userPassword attribute.
     */
    @Test
    public void changePersonPasswords() {
        System.out.println("<<<LdapServerTest.changePersonPasswords()");
        LDAPConnection connection = null;
        Filter filter = Filter.createPresenceFilter("userPassword");
        SearchRequest searchRequest = new SearchRequest("dc=slidev,dc=org", SearchScope.SUB, filter);
        SearchResult searchResult;

        int personCount = 0;
        int personPasswordChangedCount = 0;

        try {
            connection = LdapServer.getInstance().getInMemoryDirectoryServer().getConnection();
            searchResult = connection.search(searchRequest);
            int resultCount = searchResult.getEntryCount();

            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                System.out.println("    entry: " + ToStringBuilder.reflectionToString(entry, ToStringStyle.DEFAULT_STYLE));
                //  Attribute(name=userPassword, values={'{MD5}LUOaIWq99K/a23tT6zJWDg=='})

                String dn = entry.getDN(); //ou=people,...
                String uid = entry.getAttributeValue("uid");
                String newPwd = uid + "1234";

                System.out.println("      uid: " + uid);
                //System.out.println("         dn:" + dn);
                int i = StringUtils.indexOf(dn, "ou=people,", 0);
                String suffix = StringUtils.substring(dn, i);
                //System.out.println("            suffix = " + suffix);

                String user = "uid=" + uid + "," + suffix;
                System.out.println("                pwd mod string: " + user);

                PasswordModifyExtendedRequest passwordModifyRequest =
                      new PasswordModifyExtendedRequest(
                            dn, // The user to update
                            entry.getAttributeValue("userPassword"), // The current password for the user.
                            newPwd); // The new password.  null = server will generate

                PasswordModifyExtendedResult passwordModifyResult;

                try {
                    personCount++;

                    passwordModifyResult = (PasswordModifyExtendedResult) connection.processExtendedOperation(passwordModifyRequest);
                    ResultCode resultCode = passwordModifyResult.getResultCode();

                    //System.out.println("                   resultCode = " + resultCode);
                    ///System.out.println("                   resultCode intValue = " + resultCode.intValue());

                    if (passwordModifyResult != null && resultCode == ResultCode.SUCCESS) { //success
                        personPasswordChangedCount++;
                    } else {
                        System.out.println("                   " + passwordModifyResult.getDiagnosticMessage());
                    }

                    /* This doesn't necessarily mean that the operation was successful, since
                     some kinds of extended operations return non-success results under
                     normal conditions.  */
                } catch (LDAPException le) {
                    System.out.println(ExceptionUtils.getStackTrace(le));
                    /* For an extended operation, this generally means that a problem was
                     encountered while trying to send the request or read the result.  */

                    passwordModifyResult = new PasswordModifyExtendedResult(new ExtendedResult(le.toLDAPResult()));
                }
            }

            System.out.println("   changePersonPasswords searchResult entryCount: " + resultCount);
        } catch (LDAPSearchException lse) {
            // The search failed for some reason.
            searchResult = lse.getSearchResult();
            ResultCode resultCode = lse.getResultCode();
            String errorMessageFromServer = lse.getDiagnosticMessage();
            System.err.println(ToStringBuilder.reflectionToString(searchResult, ToStringStyle.SIMPLE_STYLE));
            System.err.println(ToStringBuilder.reflectionToString(resultCode, ToStringStyle.SIMPLE_STYLE));
            System.err.println(errorMessageFromServer);
            System.err.println(ExceptionUtils.getStackTrace(lse));
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.close();
            }
        }


        System.out.println("  personCount = " + personCount);
        System.out.println("  personPasswordChangedCount = " + personPasswordChangedCount);
        Assert.assertEquals(personCount, personPasswordChangedCount);

        System.out.println("<<<LdapServerTest.changePersonPasswords()");
    }

    /**
     * Stops the internal LDAP server after tests have executed.
     */
    @AfterClass
    public static void stopLdapServer() {
        System.out.println(">>>LdapServerTest.stopLdapServer()");
        LdapServer.getInstance().stop();
        System.out.println("<<<LdapServerTest.stopLdapServer()");
    }

}
