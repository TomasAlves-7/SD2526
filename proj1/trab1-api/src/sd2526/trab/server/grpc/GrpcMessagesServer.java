package sd2526.trab.server.grpc;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import sd2526.trab.client.MessageClient;
import sd2526.trab.network.ServiceAnnouncer;

public class GrpcMessagesServer {
    
    private static final Logger log = Logger.getLogger(GrpcUserServer.class.getName());

    public static final int PORT = 9000;

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
            var serverURI = GrpcServerUtils.getServerUri(port);
            announceService(period, serverURI);
            var stub = new GrpcMessagesStub(serverURI);
            log.info(String.format("Users gRPC Server ready @ %s\n", serverURI));
            GrpcServerUtils.launchServer(port, stub);
        } catch (Exception e) {
            log.severe("Unable to launch gRPC server at port %d".formatted(port));
            throw new RuntimeException(e);
        }
    }

    private static void announceService(Optional<Long> period, String serverURI) throws IOException {
        if (period.isPresent())
            new ServiceAnnouncer(MessageClient.SERVICE, serverURI, period.get());
        else
            new ServiceAnnouncer(MessageClient.SERVICE, serverURI);
    }
}
