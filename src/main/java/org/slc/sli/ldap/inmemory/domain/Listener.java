package org.slc.sli.ldap.inmemory.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by tfritz on 1/7/14.
 */
@XmlRootElement(name="listener")
@XmlType(propOrder = {"name","port","address"})
public class Listener {
    private String name;
    private String port;
    private String address;

    public String getName() {
        return name;
    }

    @XmlElement(name="name", required = true)
    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    @XmlElement(name="port", required = true)
    public void setPort(String port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    @XmlElement(name="address", required = true)
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
