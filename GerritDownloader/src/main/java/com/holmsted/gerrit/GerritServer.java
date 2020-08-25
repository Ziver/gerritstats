package com.holmsted.gerrit;

import javax.annotation.Nonnull;

/**
 * Represents a Gerrit server.
 */
public class GerritServer {

    private static final int GERRIT_SSH_DEFAULT_PORT = 29418;
    private static final int GERRIT_HTTP_DEFAULT_PORT = 80;

    private String serverAddress = null;
    private int serverPort = 0;

    private String privateKey = null;
    private String username = null;
    private String password = null;

    /**
     * Constructor for SSH information
     */
    public GerritServer(@Nonnull String serverAddress, int serverPort, @Nonnull String privateKey) {
        this.serverAddress = serverAddress;
        this.serverPort = (serverPort != 0 ? serverPort : GERRIT_SSH_DEFAULT_PORT);
        this.privateKey = privateKey;
    }

    /**
     * Constructor for HTTP information
     */
    public GerritServer(@Nonnull String serverAddress, int serverPort, String username, String password) {
        this(serverAddress, serverPort, null);
        this.username = username;
        this.password = password;
    }


    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }


    public String getPrivateKey() {
        return privateKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        return String.format("%s:%d", serverAddress, serverPort);
    }
}
