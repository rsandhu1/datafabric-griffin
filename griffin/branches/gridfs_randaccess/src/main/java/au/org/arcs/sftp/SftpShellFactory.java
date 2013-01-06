package au.org.arcs.sftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Custom version of a SSH server shell.
 * 
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 * @author John Curtis
 */
public class SftpShellFactory implements Factory<Command> {

    private static Log log = LogFactory.getLog(SftpShellFactory.class);
    private SftpServerDetails server_details;

    public SftpShellFactory(SftpServerDetails details) {
        server_details = details;
    }

    public Command create() {
        return new EchoShell(server_details);
    }

    protected static class EchoShell implements Command, Runnable {

        private SftpServerDetails server_details;
        private InputStream in;
        private OutputStream out;
        @SuppressWarnings("unused")
        private OutputStream err;
        private ExitCallback callback;
        private Thread thread;

        public EchoShell(SftpServerDetails details) {
            server_details = details;
        }

        public void setInputStream(InputStream in) {
            this.in = in;
        }

        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        public void start(Environment env) throws IOException {
            thread = new Thread(this, server_details.getAppTitle()
                    + " Echo shell");
            thread.start();
        }

        public void destroy() {
            thread.interrupt();
        }

        public void run() {
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            try {
                for (;;) {
                    String s = r.readLine();
                    if (s == null) { return; }
                    out.write((s + "\n").getBytes());
                    out.flush();
                    if ("exit".equals(s)) { return; }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                callback.onExit(0);
            }
            /*
             * try { BufferedReader reader = new BufferedReader(new
             * InputStreamReader(in)); DataOutputStream dos = new
             * DataOutputStream(out);
             * dos.writeBytes(server_details.getOptions().getAppTitle() + "\n");
             * dos.writeBytes(server_details.getOptions().getAppVersion() +
             * "\n"); dos.flush();
             * 
             * for (;;) { int c = reader.read(); switch (c) { case -1: break;
             * case '\r': int c2 = reader.read(); if ((c2 != '\n') && (c2 !=
             * -1)) { dos.writeByte(c2); dos.flush(); break; } else {
             * dos.writeByte('\n'); dos.flush(); } default: dos.writeByte(c);
             * dos.flush(); break; } }
             * 
             * } catch (Exception e) { e.printStackTrace(); } finally {
             * callback.onExit(0); }
             */
        }
    }
}
