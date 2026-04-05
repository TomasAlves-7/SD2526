package sd2526.trab.server.rest;

import java.util.List;

import sd2526.trab.api.User;
import sd2526.trab.api.rest.RestUsers;
import sd2526.trab.client.MessageClient;
import sd2526.trab.impl.JavaUsers;
import static sd2526.trab.server.rest.RestServerUtils.wrapResult;

public class UsersResource implements RestUsers {

    private final JavaUsers users = new JavaUsers();

    public UsersResource() {
        users.setMessages(MessageClient.getInstance());
    }

    @Override
    public String postUser(User user) {
        return wrapResult(users.postUser(user));
    }

    @Override
    public User getUser(String name, String pwd) {
        return wrapResult(users.getUser(name, pwd));
    }

    @Override
    public User updateUser(String name, String pwd, User info) {
        return wrapResult(users.updateUser(name, pwd, info));
    }

    @Override
    public User deleteUser(String name, String pwd) {
        return wrapResult(users.deleteUser(name, pwd));
    }

    @Override
    public List<User> searchUsers(String name, String pwd, String pattern) {
        return wrapResult(users.searchUsers(name, pwd, pattern));
    }

}

