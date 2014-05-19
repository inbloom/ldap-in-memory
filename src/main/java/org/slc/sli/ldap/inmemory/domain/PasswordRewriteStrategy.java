package org.slc.sli.ldap.inmemory.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By: paullawler
 */
public class PasswordRewriteStrategy {

    static Map<String, String> users;

    private static PasswordRewriteStrategy instance = null;

    private PasswordRewriteStrategy() {
        users = new HashMap<String, String>();
        users.put("developer-email@slidev.org", "test1234");
    }

    public static synchronized PasswordRewriteStrategy getInstance() {
        if (instance == null) {
            instance = new PasswordRewriteStrategy();
        }
        return instance;
    }

    public String generatePassword(String uid) {
        String newPwd = users.get(uid);
        if (newPwd == null) {
            return uid + "1234";
        }
        return newPwd;
    }

}
