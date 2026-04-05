package sd2526.trab.impl;

import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;

public class JavaUsers implements Users {

    private static final Logger log = Logger.getLogger(JavaUsers.class.getName());

    private Messages messages;

    private final Hibernate db = Hibernate.getInstance();

    public void setMessages(Messages messages) {
        this.messages = messages;
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

