package au.org.arcs.griffin.cmd.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.FtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b> Privacy Protected Command </b>
 * 
 * The argument field of ENC is a Telnet string consisting of a base 64 encoded
 * "private" message produced by a security mechanism specific message integrity
 * and confidentiality procedure.
 * 
 * The server will decode and/or verify the encoded message.
 * 
 * @author Shunde Zhang
 */
public class FtpCmdEnc extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdEnc.class);

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String prot = getArguments().trim();
        if (prot == null || prot.length() <= 0) {
            msgOut(MSG500_ENC);
            return;
        }
        if (getCtx().getServiceContext() == null
                || !getCtx().getServiceContext().isEstablished()) {
            msgOut(MSG503_ENC);
            return;
        }
        byte[] data = Base64.decodeBase64(prot.getBytes());
        MessageProp prop = new MessageProp(0, false);
        try {
            data = getCtx().getServiceContext().unwrap(data, 0, data.length,
                                                       prop);
        } catch (GSSException e) {
            msgOut(MSG500_DECRYPT, new String[] {e.getMessage()});
            log.error("secure_command: got GSSException: " + e.getMessage(), e);
            return;
        }

        // At least one C-based client sends a zero byte at the end
        // of a secured command. Truncate trailing zeros.
        // Search from the right end of the string for a non-null character.
        int i;
        for (i = data.length; i > 0 && data[i - 1] == 0; i--) {
            // do nothing, just decrement i
        }
        String msg = new String(data, 0, i);
        msg = msg.trim();
        log.info("decrypted msg: " + msg);

        if (msg.equalsIgnoreCase("CCC")) {
            getCtx().setReplyType("clear");
            msgOut(MSG200);
        } else {
            getCtx().setReplyType("enc");
            // ftpcommand(msg);
            String token = getParser().findCommandToken(msg);
            if (token == null) {
                msgOut(MSG500_CMD, new String[] {msg});
                return;
            }
            FtpCmd cmd = getParser().createCommandByToken(token);
            if (cmd == null) {
                msgOut(MSG500_CMD, new String[] {msg});
                return;
            }
            String args = msg.substring(token.length()).trim();
            cmd.setArguments(args);
            cmd.setCtx(getCtx());
            cmd.execute();
            // msgOut(MSG500);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "GSI command";
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
