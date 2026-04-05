package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.List;

import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import sd2526.trab.api.User;
import sd2526.trab.api.grpc.GrpcUsersGrpc;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'postUser'");
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchUsers'");
    }
}

