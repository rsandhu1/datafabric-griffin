package au.org.arcs.griffin.server.impl;

import java.util.ResourceBundle;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.session.impl.FtpSessionContextImpl;

/**
 * This is the implementation class for GSIFTP.
 * @author Shunde Zhang
 */

public class GsiFtpServer extends DefaultFtpServer {
    private static Log log = LogFactory.getLog(GsiFtpServer.class);

    protected FtpSessionContext createFtpContext() {
        FtpSessionContext ctx = new FtpSessionContextImpl(getOptions(), getFileSystem(), ResourceBundle
            .getBundle(getResources()), this);
        ctx.setBufferSize(ctx.getOptions().getBufferSize());
        ctx.setNetworkStack(NETWORK_STACK_TCP);
        ctx.setDCAU(DCAU_SELF);
        return ctx;
    }

}
