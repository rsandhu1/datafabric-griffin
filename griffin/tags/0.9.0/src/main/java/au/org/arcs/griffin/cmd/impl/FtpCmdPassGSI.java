/*
 * FtpCmdPassGSI.java
 * 
 * Implementation of local file system storage interface.
 * 
 * Created: 2010-01-04 Shunde Zhang <shunde.zhang@arcs.org.au>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 */

package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>PASSWORD (PASS)</b>
 * <p>
 * Pass for GSI, it does nothing.
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdPassGSI extends AbstractFtpCmd {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        if (getCtx().getGSSIdentity() != null) {
            msgOut(MSG230);
            return;
        } else {
            msgOut(MSG530, new String[] {"Send USER first"});
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the user's password";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExtension() {
        return false;
    }
}
