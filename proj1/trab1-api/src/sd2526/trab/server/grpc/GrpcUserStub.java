package sd2526.trab.server.grpc;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import sd2526.trab.api.grpc.GrpcUsersGrpc;
import sd2526.trab.api.grpc.Users.DeleteUserArgs;
import sd2526.trab.api.grpc.Users.DeleteUserResult;
import sd2526.trab.api.grpc.Users.GetUserArgs;
import sd2526.trab.api.grpc.Users.GetUserResult;
import sd2526.trab.api.grpc.Users.GrpcUser;
import sd2526.trab.api.grpc.Users.PostUserResult;
import sd2526.trab.api.grpc.Users.SearchUsersArgs;
import sd2526.trab.api.grpc.Users.UpdateUserArgs;
import sd2526.trab.api.grpc.Users.UpdateUserResult;
import sd2526.trab.client.MessageClient;
import sd2526.trab.impl.JavaUsers;
import sd2526.trab.network.Adapter;
import static sd2526.trab.server.grpc.GrpcServerUtils.unwrapResult;

public class GrpcUserStub implements GrpcUsersGrpc.AsyncService, BindableService {

    private final JavaUsers users = new JavaUsers();

    public GrpcUserStub() {
        var messageService = MessageClient.getInstance();
        users.setMessages(messageService);
    }

    @Override
    public ServerServiceDefinition bindService() {
        return GrpcUsersGrpc.bindService(this);
    }
    
    @Override
    public void postUser(GrpcUser request, StreamObserver<PostUserResult> responseObserver) {
        var user = Adapter.GrpcUser_to_User(request);
        var result = users.postUser(user);
        unwrapResult(responseObserver, result, () -> {
            var out = PostUserResult.newBuilder().setUserAddress(result.value()).build();
            responseObserver.onNext(out);
        });
    }

    @Override
    public void getUser(GetUserArgs request, StreamObserver<GetUserResult> responseObserver) {
        var result = users.getUser(request.getName(), request.getPwd());
        unwrapResult(responseObserver, result, () -> {
            var user = result.value();
            var grpcUser = Adapter.User_to_GrpcUser(user);
            responseObserver.onNext(GetUserResult.newBuilder().setUser(grpcUser).build());
        });
    }

    @Override
    public void updateUser(UpdateUserArgs request, StreamObserver<UpdateUserResult> responseObserver) {
        var updateInfo = Adapter.GrpcUser_to_User(request.getInfo());
        var result = users.updateUser(request.getName(), request.getPwd(), updateInfo);
        unwrapResult(responseObserver, result, () -> {
            var user = result.value();
            var grpcUser = Adapter.User_to_GrpcUser(user);
            responseObserver.onNext(UpdateUserResult.newBuilder().setUser(grpcUser).build());
        });
    }

    @Override
    public void deleteUser(DeleteUserArgs request, StreamObserver<DeleteUserResult> responseObserver) {
        var result = users.deleteUser(request.getName(), request.getPwd());
        unwrapResult(responseObserver, result, () -> {
            var user = result.value();
            var grpcUser = Adapter.User_to_GrpcUser(user);
            responseObserver.onNext(DeleteUserResult.newBuilder().setUser(grpcUser).build());
        });
    }

    @Override
    public void searchUsers(SearchUsersArgs request, StreamObserver<GrpcUser> responseObserver) {
        var result = users.searchUsers(request.getName(), request.getPwd(), request.getQuery());
        unwrapResult(responseObserver, result, () -> {
            var users = result.value();
            users.stream().map(Adapter::User_to_GrpcUser).forEach(responseObserver::onNext);
        });
    }
}
