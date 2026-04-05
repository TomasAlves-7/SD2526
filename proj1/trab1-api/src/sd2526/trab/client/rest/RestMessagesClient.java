package sd2526.trab.client.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.rest.RestMessages;

public class RestMessagesClient implements Messages {

    private static final Logger log = Logger.getLogger(RestMessagesClient.class.getName());

    private final WebTarget baseTarget;

    public RestMessagesClient(URI serverUri) {
        Client client = RestClientUtils.computeClient();
        this.baseTarget = client.target(serverUri).path(RestMessages.PATH);
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

