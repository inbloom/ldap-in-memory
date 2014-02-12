package org.slc.sli.ldap.inmemory.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by tfritz on 1/7/14.
 */
@XmlRootElement(name= "ldif")
@XmlType(propOrder = {"name"})
public class Ldif {

    private String name;

    public String getName() {
        return name;
    }

    @XmlElement(name="name", required = true)
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
