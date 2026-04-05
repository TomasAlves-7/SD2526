package sd2526.trab.client.grpc;

import java.util.function.Supplier;

import io.grpc.StatusRuntimeException;
import sd2526.trab.api.java.Result;
import static sd2526.trab.api.java.Result.ErrorCode.BAD_REQUEST;
import static sd2526.trab.api.java.Result.ErrorCode.CONFLICT;
import static sd2526.trab.api.java.Result.ErrorCode.FORBIDDEN;
import static sd2526.trab.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static sd2526.trab.api.java.Result.ErrorCode.NOT_FOUND;
import static sd2526.trab.api.java.Result.ErrorCode.TIMEOUT;

public class GrpcClientUtils {

    //static final long READ_TIMEOUT = 50000;

    static <T> Result<T> wrapRequest(Supplier<Result<T>> f) {
        try {
            return f.get();
        } catch (StatusRuntimeException sre) {
            return Result.error(getErrorCodeFrom(sre));
        }
    }

    static Result.ErrorCode getErrorCodeFrom(StatusRuntimeException status) {
        var code = status.getStatus().getCode();
        return switch (code) {
            case ALREADY_EXISTS -> CONFLICT;
            case PERMISSION_DENIED -> FORBIDDEN;
            case NOT_FOUND -> NOT_FOUND;
            case INVALID_ARGUMENT -> BAD_REQUEST;
            case DEADLINE_EXCEEDED -> TIMEOUT;
            default -> INTERNAL_ERROR;
        };
    }

}
