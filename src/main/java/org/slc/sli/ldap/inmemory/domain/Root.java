package org.slc.sli.ldap.inmemory.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Created by tfritz on 1/7/14.
 */
@XmlRootElement(name="root")
@XmlType(propOrder = {"objectDn","objectClasses"})
public class Root {
    private String objectDn;
    private List<String> objectClasses;


    public String getObjectDn() {
        return objectDn;
    }

    @XmlElement(name="objectDn", required = true)
    public void setObjectDn(String objectDn) {
        this.objectDn = objectDn;
    }

    public java.util.List<String> getObjectClasses() {
        return objectClasses;
    }

    @XmlElementWrapper(name="objectClasses", required = true)
    @XmlElement(name="objectClass", required = true)
    public void setObjectClasses(List<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
