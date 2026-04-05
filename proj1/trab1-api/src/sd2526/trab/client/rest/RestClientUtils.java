package sd2526.trab.client.rest;

import java.util.function.Supplier;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import jakarta.ws.rs.core.Response;
import sd2526.trab.api.java.Result;
import static sd2526.trab.api.java.Result.ErrorCode.BAD_REQUEST;
import static sd2526.trab.api.java.Result.ErrorCode.CONFLICT;
import static sd2526.trab.api.java.Result.ErrorCode.FORBIDDEN;
import static sd2526.trab.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static sd2526.trab.api.java.Result.ErrorCode.NOT_FOUND;
import static sd2526.trab.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static sd2526.trab.api.java.Result.ErrorCode.OK;

public class RestClientUtils {

    private static final Logger log = Logger.getLogger(RestClientUtils.class.getName());

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    static final int READ_TIMEOUT = 5000;
    static final int CONNECT_TIMEOUT = 5000;

    static Client computeClient() {
        var config = new ClientConfig();
        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        return ClientBuilder.newClient(config);
    }

    static <V> Result<V> runRepeatableRequest(Supplier<Result<V>> mappingFunction) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return mappingFunction.get();
            } catch(ProcessingException e) {
                log.info(e.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ignored) {}
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return Result.error(Result.ErrorCode.TIMEOUT);
    }

    static <T> Result<T> getRequest(WebTarget target, Class<T> outputClass) {
        return runRepeatableRequest(() -> {
            var invocation = target.request().accept(APPLICATION_JSON);
            try (var response = invocation.get()) {
                return processResponseWithBody(outputClass, response);
            }
        });
    }

    static <T> Result<T> deleteRequest(WebTarget target, Class<T> outputClass) {
        return runRepeatableRequest(() -> {
            var invocation = target.request().accept(APPLICATION_JSON);
            try (var response = invocation.delete()) {
                return processResponseWithBody(outputClass, response);
            }
        });
    }

    static Result<Void> deleteNoContentRequest(WebTarget target) {
        return runRepeatableRequest(() -> {
            var invocation = target.request();
            try (var response = invocation.delete()) {
                int status = response.getStatus();
                if (status != Response.Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                return Result.ok();
            }
        });
    }

    static <T,G> Result<G> postRequest(WebTarget target, T entity, Class<G> outputClass) {
        return runRepeatableRequest(() -> {
            var invocation = target.request().accept(APPLICATION_JSON);
            var body = Entity.json(entity);
            try (var response = invocation.post(body)) {
                return processResponseWithBody(outputClass, response);
            }
        });
    }

    static <T,G> Result<G> putRequest(WebTarget target, T entity, Class<G> outputClass) {
        return runRepeatableRequest(() -> {
            var invocation = target.request().accept(APPLICATION_JSON);
            var body = Entity.json(entity);
            try (var response = invocation.put(body)) {
                return processResponseWithBody(outputClass, response);
            }
        });
    }

    static <T> Result<T> processResponseWithBody(Class<T> outputClass, Response response) {
        int status = response.getStatus();
        if (status != Response.Status.OK.getStatusCode() || !response.hasEntity())
            return Result.error(getErrorCodeFrom(status));
        return Result.ok(response.readEntity(outputClass));
    }

    static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> OK;
            case 409 -> CONFLICT;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 400 -> BAD_REQUEST;
            case 501 -> NOT_IMPLEMENTED;
            default -> INTERNAL_ERROR;
        };
    }
}

