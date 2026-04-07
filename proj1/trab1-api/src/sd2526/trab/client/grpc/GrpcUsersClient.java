package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import sd2526.trab.api.User;
import sd2526.trab.api.grpc.GrpcUsersGrpc;
import sd2526.trab.api.grpc.Users.DeleteUserArgs;
import sd2526.trab.api.grpc.Users.GetUserArgs;
import sd2526.trab.api.grpc.Users.GrpcUser;
import sd2526.trab.api.grpc.Users.SearchUsersArgs;
import sd2526.trab.api.grpc.Users.UpdateUserArgs;
import sd2526.trab.api.java.Result;
import static sd2526.trab.api.java.Result.ErrorCode.BAD_REQUEST;
import static sd2526.trab.api.java.Result.ErrorCode.FORBIDDEN;
import sd2526.trab.api.java.Users;
import static sd2526.trab.client.grpc.GrpcClientUtils.wrapRequest;
import sd2526.trab.network.Adapter;

public class GrpcUsersClient implements Users {

    static {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
    }

    private final GrpcUsersGrpc.GrpcUsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).enableRetry().usePlaintext().build();
        this.stub = GrpcUsersGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<String> postUser(User user) {
        return wrapRequest(() -> {
            var grpcUser = Adapter.User_to_GrpcUser(user);
            var result = stub.postUser(grpcUser);
            return Result.ok(result.getUserAddress());
        });
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        if (name == null) {
            return Result.error(BAD_REQUEST);
        }
        if (pwd == null) {
            return Result.error(FORBIDDEN);
        }
        return wrapRequest(() -> {
            var args = GetUserArgs.newBuilder().setName(name).setPwd(pwd).build();
            var grpcUser = stub.getUser(args).getUser();
            return Result.ok(Adapter.GrpcUser_to_User(grpcUser));
        });
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        if (name == null) {
            return Result.error(BAD_REQUEST);
        }
        if (pwd == null) {
            return Result.error(FORBIDDEN);
        }
        return wrapRequest(() -> {
            var grpcInfo = Adapter.User_to_GrpcUser(info);
            var args = UpdateUserArgs.newBuilder().setName(name).setPwd(pwd).setInfo(grpcInfo).build();
            var grpcUser = stub.updateUser(args).getUser();
            return Result.ok(Adapter.GrpcUser_to_User(grpcUser));
        });
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        if (name == null) {
            return Result.error(BAD_REQUEST);
        }
        if (pwd == null) {
            return Result.error(FORBIDDEN);
        }
        return wrapRequest(() -> {
            var args = DeleteUserArgs.newBuilder().setName(name).setPwd(pwd).build();
            var grpcUser = stub.deleteUser(args).getUser();
            return Result.ok(Adapter.GrpcUser_to_User(grpcUser));
        });
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        if (name == null) {
            return Result.error(BAD_REQUEST);
        }
        if (pwd == null) {
            return Result.error(FORBIDDEN);
        }
        query = query == null ? query : "";
        var args = SearchUsersArgs.newBuilder().setName(name).setPwd(pwd).setQuery(query).build();
        return wrapRequest(() -> {
            var userIterator = stub.searchUsers(args);
            List<GrpcUser> grpcUsers = new ArrayList<>();
            userIterator.forEachRemaining(grpcUsers::add);
            return Result.ok(grpcUsers.stream().map(Adapter::GrpcUser_to_User).toList());
        });
    }
}

