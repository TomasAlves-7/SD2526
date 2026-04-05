package sd2526.trab.client;

import java.util.List;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;
import sd2526.trab.client.grpc.GrpcUsersClient;
import sd2526.trab.client.rest.RestUsersClient;

public class UsersClient implements Users {

    public static final String SERVICE = "Users";

    private final ClientLauncher launcher = new ClientLauncher();

    private volatile Users inner;

    private static UsersClient singleton;

    public static UsersClient getInstance() {
        if (singleton == null) {
            synchronized (UsersClient.class) {
                if (singleton == null)
                    singleton = new UsersClient();
            }
        }
        return singleton;
    }

    private UsersClient() {}

    @Override
    public Result<String> postUser(User user) {
        if (inner == null)
            materializeChannel();
        return inner.postUser(user);
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        if (inner == null)
            materializeChannel();
        return inner.getUser(name, pwd);
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        if (inner == null)
            materializeChannel();
        return inner.updateUser(name, pwd, info);
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        if (inner == null)
            materializeChannel();
        return inner.deleteUser(name, pwd);
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        if (inner == null)
            materializeChannel();
        return inner.searchUsers(name, pwd, query);
    }

    private void materializeChannel() {
        synchronized (this) {
            if (inner == null)
                inner = computeInnerChannel();
        }
    }

    private Users computeInnerChannel() {
        return launcher.launch(SERVICE, RestUsersClient::new, GrpcUsersClient::new);
    }
}

