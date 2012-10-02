package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.ftp.vanilla.Command;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.DataChannel;
import au.org.arcs.griffin.cmd.RemoteGridFTPControlChannelProvider;
import au.org.arcs.griffin.exception.FtpCmdException;

public class FtpCmdStorConductor extends AbstractFtpCmd {
    private static Log log = LogFactory.getLog(FtpCmdStorConductor.class);
    public void execute() throws FtpCmdException {
        long fileOffset = getAndResetFileOffset();
        int maxThread = getCtx().getParallelMax();
        if (maxThread < 1) {
            maxThread = 1;
        }
        try {

            RemoteGridFTPControlChannelProvider provider=(RemoteGridFTPControlChannelProvider)getCtx().getDataChannelProvider();
            provider.setOffset(fileOffset);
            provider.setMaxThread(maxThread);
            provider.setDirection(DataChannel.DIRECTION_PUT);
            provider.prepare();
            
            provider.sendCmd(new Command("STOR",getArguments()), this);

            Thread thread = new Thread(provider);
            thread.start();
            try {
                if (thread.isAlive()) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                log.warn("interrupted exception, this is logged and ignored");
                e.printStackTrace();
            }
//            provider.closeProvider();
            if (provider.getServerException()!=null) throw provider.getServerException();
            log.info("transfer is complete");


        } catch (RuntimeException e) {
        	e.printStackTrace();
//            log.error(e.toString());
            msgOut(MSG501);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            msgOut(MSG425);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            msgOut(MSG501);
		}
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

	public String getHelp() {
		// TODO Auto-generated method stub
		return "store file to the nearest location";
	}

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
