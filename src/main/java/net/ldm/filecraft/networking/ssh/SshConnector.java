package net.ldm.filecraft.networking.ssh;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SshConnector {
    private final Session session;
    private final String host;

    public SshConnector(String username, String host) throws JSchException {
        this.host = host;
        this.session = new JSch().getSession(username, host);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
    }

    public void setPassword(String password) {
        session.setPassword(password);
    }

    public void connect() throws JSchException {
        session.connect();
    }

    public static record ShellOutput(String output, int exitCode) {}

    public ShellOutput execute(String command) throws JSchException, IOException, InterruptedException {
        StringBuilder out = new StringBuilder();
        int exitCode = -1;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] buffer = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int bytesRead = in.read(buffer, 0, 1024);
                if (bytesRead < 0) break;
                out.append(new String(buffer, 0, bytesRead));
            }

            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                exitCode = channel.getExitStatus();
                break;
            }

            //noinspection BusyWait
            Thread.sleep(100);
        }

        channel.disconnect();
        return new ShellOutput(out.toString(), exitCode);
    }

    public void disconnect() {
        session.disconnect();
    }

    public String getHost() {
        return host;
    }
}
