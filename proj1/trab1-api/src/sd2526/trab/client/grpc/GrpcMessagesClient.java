package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.List;

import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import sd2526.trab.api.Message;
import sd2526.trab.api.grpc.GrpcMessagesGrpc;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;

public class GrpcMessagesClient implements Messages {

    static {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
    }

    private final GrpcMessagesGrpc.GrpcMessagesBlockingStub stub;

    public GrpcMessagesClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).enableRetry().usePlaintext().build();
        this.stub = GrpcMessagesGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'postMessage'");
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInboxMessage'");
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllInboxMessages'");
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeInboxMessage'");
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteMessage'");
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchInbox'");
    }
}

