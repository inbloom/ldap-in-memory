package org.slc.sli.ldap.inmemory.loghandlers;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by tfritz on 1/13/14.
 */
public class AccessLogHandler extends Handler {
    private final static Logger LOG = LoggerFactory.getLogger(AccessLogHandler.class);

    @Override
    public void publish(LogRecord logRecord) {
       LOG.debug(ToStringBuilder.reflectionToString(logRecord, ToStringStyle.MULTI_LINE_STYLE));
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
