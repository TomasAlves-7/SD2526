package sd2526.trab.server.grpc;


import com.google.protobuf.Empty;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import sd2526.trab.api.grpc.GrpcMessagesGrpc;
import sd2526.trab.api.grpc.Messages.DeleteMessageArgs;
import sd2526.trab.api.grpc.Messages.GetAllInboxMessagesArgs;
import sd2526.trab.api.grpc.Messages.GetAllInboxMessagesResult;
import sd2526.trab.api.grpc.Messages.GetInboxMessageArgs;
import sd2526.trab.api.grpc.Messages.GrpcMessage;
import sd2526.trab.api.grpc.Messages.PostMessageArgs;
import sd2526.trab.api.grpc.Messages.PostMessageResult;
import sd2526.trab.api.grpc.Messages.RemoveInboxMessageArgs;
import sd2526.trab.api.grpc.Messages.SearchInboxArgs;
import sd2526.trab.api.grpc.Messages.SearchInboxResult;
import sd2526.trab.client.UsersClient;
import sd2526.trab.impl.JavaMessages;
import sd2526.trab.network.Adapter;
import static sd2526.trab.server.grpc.GrpcServerUtils.unwrapResult;
import jakarta.ws.rs.core.UriBuilder;

public class GrpcMessagesStub implements GrpcMessagesGrpc.AsyncService, BindableService {
    
    private final String baseUri;

    private final JavaMessages messages;

    public GrpcMessagesStub(String baseUri) {
        this.baseUri = baseUri;
        messages = new JavaMessages();
        messages.setUsers(UsersClient.getInstance());
    }

    @Override
    public ServerServiceDefinition bindService() {
        return GrpcMessagesGrpc.bindService(this);
    }

    @Override
    public void postMessage(PostMessageArgs request, StreamObserver<PostMessageResult> responseObserver) {
        var message = Adapter.GrpcMessages_to_Message(request.getMessage());
        var result = messages.postMessage(request.getPwd(), message);
        unwrapResult(responseObserver, result, () -> {
            var relativeUri = result.value();
            var uri = UriBuilder.fromUri(baseUri).path("/messages/").path(relativeUri).build().toASCIIString();
            responseObserver.onNext(PostMessageResult.newBuilder().setMid(uri).build());
        });
    }

    @Override
    public void getInboxMessage(GetInboxMessageArgs request, StreamObserver<GrpcMessage> responseObserver) {
        var result = messages.getInboxMessage(request.getName(), request.getMid(), request.getPwd());
        unwrapResult(responseObserver, result, () -> {
            var message = result.value();
            var grpcMessage = Adapter.Messages_to_GrpcMessage(message);
            responseObserver.onNext(grpcMessage);
        });
    }

    @Override
    public void getAllInboxMessages(GetAllInboxMessagesArgs request, StreamObserver<GetAllInboxMessagesResult> responseObserver) {
        var result = messages.getAllInboxMessages(request.getName(), request.getPwd());
        unwrapResult(responseObserver, result, () -> {
            var mids = result.value();
            responseObserver.onNext(GetAllInboxMessagesResult.newBuilder().addAllMids(mids).build());
        });
    }
    
    @Override
    public void removeInboxMessage(RemoveInboxMessageArgs request, StreamObserver<Empty> responseObserver) {
        var result = messages.removeInboxMessage(request.getName(), request.getMid(), request.getPwd());
        unwrapResult(responseObserver, result, () -> {
            responseObserver.onNext(Empty.getDefaultInstance());
        });
    }

    @Override
    public void deleteMessage(DeleteMessageArgs request, StreamObserver<Empty> responseObserver) {
        var result = messages.deleteMessage(request.getName(), request.getMid(),request.getPwd());
        unwrapResult(responseObserver, result, () -> {
            responseObserver.onNext(Empty.getDefaultInstance());
        });
    }

    @Override
    public void searchInbox(SearchInboxArgs request, StreamObserver<SearchInboxResult> responseObserver) {
        var result = messages.searchInbox(request.getName(), request.getPwd(), request.getQuery());
        unwrapResult(responseObserver, result, () -> {
            var mids = result.value();
            responseObserver.onNext(SearchInboxResult.newBuilder().addAllMids(mids).build());
        });
    }
}
