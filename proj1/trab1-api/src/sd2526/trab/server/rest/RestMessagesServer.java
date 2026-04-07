package sd2526.trab.server.rest;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import sd2526.trab.client.MessageClient;
import sd2526.trab.network.ServiceAnnouncer;

public class RestMessagesServer {

    private static final Logger log = Logger.getLogger(RestMessagesServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8081;

    public static void main(String[] args) {
        launchServer(PORT);
    }

    public static void launchServer(int port) {
        launchServer(port, Optional.empty());
    }

    public static void launchServer(int port, long period) {
        launchServer(port, Optional.of(period));
    }

    private static void launchServer(int port, Optional<Long> period) {
        try {
            var serverURI = RestServerUtils.computeServerUri(port);
            announceService(serverURI, period);
            RestServerUtils.launchResource(serverURI, MessageResource.class);
            log.info(String.format("%s Server ready @ %s\n",  MessageClient.SERVICE, serverURI));
        } catch( Exception e) {
            log.severe(e.getMessage());
        }
    }

    private static void announceService(String serverURI, Optional<Long> period) throws IOException {
        if (period.isPresent())
            new ServiceAnnouncer(MessageClient.SERVICE, serverURI, period.get());
        else
            new ServiceAnnouncer(MessageClient.SERVICE, serverURI);
    }
}

