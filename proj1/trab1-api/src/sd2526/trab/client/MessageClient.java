package sd2526.trab.client;

import java.util.List;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.client.grpc.GrpcMessagesClient;
import sd2526.trab.client.rest.RestMessagesClient;

public class MessageClient implements Messages {

    public static final String SERVICE = "Message";

    private final ClientLauncher launcher = new ClientLauncher();

    private volatile Messages inner;

    private static MessageClient singleton;

    public static MessageClient getInstance() {
        if (singleton == null) {
            synchronized (MessageClient.class) {
                if (singleton == null)
                    singleton = new MessageClient();
            }
        }
        return singleton;
    }

    private MessageClient() {}

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        if (inner == null)
            materializeChannel();
        return inner.postMessage(pwd, msg);
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        if (inner == null)
            materializeChannel();
        return inner.getInboxMessage(name, mid, pwd);
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        if (inner == null)
            materializeChannel();
        return inner.getAllInboxMessages(name, pwd);
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        if (inner == null)
            materializeChannel();
        return inner.removeInboxMessage(name, mid, pwd);
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        if (inner == null)
            materializeChannel();
        return inner.deleteMessage(name, mid, pwd);
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        if (inner == null)
            materializeChannel();
        return inner.searchInbox(name, pwd, query);
    }

    private void materializeChannel () {
        synchronized (this) {
            if (inner == null)
                inner = launcher.launch(SERVICE, RestMessagesClient::new, GrpcMessagesClient::new);
        }
    }
    
}
