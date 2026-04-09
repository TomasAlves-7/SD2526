package sd2526.trab.client;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.function.Function;
import java.util.logging.Logger;

import sd2526.trab.network.Discovery;
import sd2526.trab.server.ServerUtils;

public class ClientLauncher {

    private static final Logger log = Logger.getLogger(ClientLauncher.class.getName());

    private final Discovery listener = Discovery.getInstance();

    <T> T launch(String service, Function<URI, T> restLauncher, Function<URI, T> grpcLauncher) {
        String domain;
        try{
            String hostname = InetAddress.getLocalHost().getHostName();
            domain = hostname.substring(hostname.indexOf('.') + 1);
        } catch (UnknownHostException e){
            domain = "ourorg"; //fallback
        }
        var serverUri = listener.knownUrisOf(service, domain, 1)[0];

        var uriComponents = serverUri.getPath().split("/");
        var commType = uriComponents[uriComponents.length - 1];
        if (commType.equals(ServerUtils.InterfaceType.REST.getType())) {
            return restLauncher.apply(serverUri);
        } else if (commType.equals(ServerUtils.InterfaceType.GRPC.getType())) {
            return grpcLauncher.apply(serverUri);
        } else {
            log.severe("Received unknown communication type %s".formatted(commType));
            throw new RuntimeException();
        }

    }

}

