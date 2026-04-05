package sd2526.trab.server.rest;

import java.net.URI;
import java.net.UnknownHostException;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import sd2526.trab.api.java.Result;
import sd2526.trab.server.ServerUtils;

public class RestServerUtils {

    public static final String COMM_PROTOCOL = "http";

    static String computeServerUri(int port) throws UnknownHostException {
        return ServerUtils.getServerURI(COMM_PROTOCOL, port, ServerUtils.InterfaceType.REST);
    }

    static <T> void launchResource(String uri, Class<T> resourceClass) {
        ResourceConfig config = new ResourceConfig();
        config.register(resourceClass);
        JdkHttpServerFactory.createHttpServer( URI.create(uri), config);
    }

    static <T> T wrapResult(Result<T> res) {
        if (res.isOK())
            return res.value();
        throw statusCodeToException(res.error());
    }

    static WebApplicationException statusCodeToException(Result.ErrorCode err) {
        Response.Status status = switch (err) {
            case CONFLICT -> CONFLICT;
            case NOT_FOUND -> NOT_FOUND;
            case BAD_REQUEST -> BAD_REQUEST;
            case FORBIDDEN -> FORBIDDEN;
            default -> INTERNAL_SERVER_ERROR;
        };
        return new WebApplicationException(status);
    }

}
