package sd2526.trab.server.grpc;

import java.io.IOException;
import java.net.UnknownHostException;

import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerCredentials;
import io.grpc.Status;
import static io.grpc.Status.ALREADY_EXISTS;
import static io.grpc.Status.INTERNAL;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.NOT_FOUND;
import static io.grpc.Status.PERMISSION_DENIED;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import sd2526.trab.api.java.Result;
import sd2526.trab.server.ServerUtils;

public class GrpcServerUtils {
    
    private static final String COMM_PROTOCOL = "grpc";

    static String getServerUri(int port) throws UnknownHostException {
        return ServerUtils.getServerURI(COMM_PROTOCOL, port, ServerUtils.InterfaceType.GRPC);
    }

    static void launchServer(int port, BindableService stub) throws InterruptedException, IOException {
        ServerCredentials cred = InsecureServerCredentials.create();
        Server server = Grpc.newServerBuilderForPort(port, cred).addService(stub).build();
        server.start().awaitTermination();
    }

    static <T, V> void unwrapResult(StreamObserver<T> obs, Result<V> res, Runnable r) {
        if (!res.isOK()) {
            obs.onError(errorCodeToStatus(res.error()));
        } else {
            r.run();
            obs.onCompleted();
        }
    }

    static StatusException errorCodeToStatus(Result.ErrorCode err) {
        Status s = switch (err) {
            case BAD_REQUEST -> INVALID_ARGUMENT;
            case NOT_FOUND -> NOT_FOUND;
            case FORBIDDEN -> PERMISSION_DENIED;
            case CONFLICT -> ALREADY_EXISTS;
            default -> INTERNAL;
        };
        return s.asException();
    }

}
