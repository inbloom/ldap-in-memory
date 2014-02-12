package org.slc.sli.ldap.inmemory.domain;

/**
 * Encapsulates configuration for the in-memory LDAP server, which is loaded by JAXB and validated against an XSD.
 * Created by tfritz on 1/7/14.
 */


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name="server")
@XmlType(propOrder={"bindDn","password","schema","root","entries","listeners","ldifs"})
public class Server {
    private String bindDn;
    private String password;
    private Schema schema;
    private Root root;
    private List<Entry> entries;
    private List<Listener> listeners;
    private List<Ldif> ldifs;

    public String getBindDn() {
        return bindDn;
    }

    @XmlElement(name="bindDn", required = true)
    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public String getPassword() {
        return password;
    }

    @XmlElement(name="password", required = true)
    public void setPassword(String password) {
        this.password = password;
    }

    public Schema getSchema() {
        return schema;
    }

    @XmlElement(name="schema", required = true)
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Root getRoot() {
        return root;
    }

    @XmlElement
    public void setRoot(Root root) {
        this.root = root;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @XmlElementWrapper(name="entries", required = true)
    @XmlElement(name="entry", required = true)
    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    @XmlElementWrapper(name="listeners", required = true)
    @XmlElement(name="listener", required = true)
    public void setListeners(List<Listener> listeners) {
        this.listeners = listeners;
    }

    public List<Ldif> getLdifs() {
        return ldifs;
    }

    @XmlElementWrapper(name="ldifs", required = true)
    @XmlElement(name= "ldif", required = true)
    public void setLdifs(List<Ldif> ldifs) {
        this.ldifs = ldifs;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
