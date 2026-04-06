package sd2526.trab.server.rest;

import java.util.List;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import sd2526.trab.api.Message;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.client.UsersClient;
import sd2526.trab.impl.JavaMessages;
import static sd2526.trab.server.rest.RestServerUtils.statusCodeToException;
import static sd2526.trab.server.rest.RestServerUtils.wrapResult;

public class MessageResource implements RestMessages {

    @Context
    private UriInfo uri;

    private final JavaMessages messages = new JavaMessages();

    public MessageResource() {
        messages.setUsers(UsersClient.getInstance());
    }

    @Override
    public String postMessage(String pwd, Message msg) {
        var res = messages.postMessage(pwd, msg);
        if (!res.isOK()) {
            throw statusCodeToException(res.error());
        }
        var relativeURI = res.value();
        var baseURI = uri.getBaseUri();
        var uri = UriBuilder.fromUri(baseURI).path(RestMessages.PATH).path(relativeURI).build();
        return uri.toASCIIString();
    }

    @Override
    public Message getMessage(String name, String mid, String pwd) {
        return wrapResult(messages.getInboxMessage(name, mid, pwd));
    }

    @Override
    public List<String> getMessages(String name, String pwd, String query) {
        return wrapResult(messages.getAllInboxMessages(name, pwd));
    }

    @Override
    public void removeFromUserInbox(String name, String mid, String pwd) {
        wrapResult(messages.removeInboxMessage(name, mid, pwd));
    }

    @Override
    public void deleteMessage(String name, String mid, String pwd) {
        wrapResult(messages.deleteMessage(name, mid, pwd));
    }
}

