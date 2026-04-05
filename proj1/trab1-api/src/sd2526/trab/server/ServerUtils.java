package sd2526.trab.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerUtils {
    
    private static final String SERVER_URI_FMT = "%s://%s:%s/%s";

    public enum InterfaceType {
        REST("rest"),
        GRPC("grpc");

        private final String type;

        InterfaceType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static String getServerURI(String protocol, int port, InterfaceType interfaceType) throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        return SERVER_URI_FMT.formatted(interfaceType.getType(), ip, port, interfaceType.getType());
    }
}
