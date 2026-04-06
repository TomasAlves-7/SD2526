package sd2526.trab.impl;

import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;

public class JavaMessages implements Messages {

    private static final Logger log = Logger.getLogger(JavaMessages.class.getName());

    private static final String SERVICE_NAME = "Messages";

    private Users users;

    public void setUsers(Users users) {
        this.users = users;
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
