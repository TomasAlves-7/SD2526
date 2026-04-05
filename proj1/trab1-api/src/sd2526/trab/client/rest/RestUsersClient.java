package sd2526.trab.client.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import static sd2526.trab.api.java.Result.ErrorCode.BAD_REQUEST;
import static sd2526.trab.api.java.Result.ErrorCode.FORBIDDEN;
import static sd2526.trab.api.java.Result.error;
import sd2526.trab.api.java.Users;
import sd2526.trab.api.rest.RestUsers;
import static sd2526.trab.client.rest.RestClientUtils.deleteRequest;
import static sd2526.trab.client.rest.RestClientUtils.getRequest;
import static sd2526.trab.client.rest.RestClientUtils.postRequest;
import static sd2526.trab.client.rest.RestClientUtils.putRequest;



public class RestUsersClient implements Users {
    
    private static final Logger log = Logger.getLogger(RestUsersClient.class.getName());

    private final WebTarget baseTarget;

    public RestUsersClient(URI serverUri) {
        Client client = RestClientUtils.computeClient();
        this.baseTarget = client.target(serverUri).path(RestUsers.PATH);
    }

    @Override
    public Result<String> postUser(User user) {
        log.info("sending postUser request");
        return postRequest(baseTarget, user, String.class);
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        log.info("sending get user request");
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        var target = baseTarget.path(name).queryParam(RestUsers.PWD, pwd);
        return getRequest(target, User.class);
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        log.info("sending update user request");
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        var target = baseTarget.path(name).queryParam(RestUsers.PWD, pwd);
        return putRequest(target, info, User.class);
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        log.info("sending delete user request");
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        var target = baseTarget.path(name).queryParam(RestUsers.PWD, pwd);
        return deleteRequest(target, User.class);
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        log.info("sending search users request");
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        var target = baseTarget.path(name).queryParam(RestUsers.PWD, pwd).queryParam(RestUsers.QUERY, query);
        return Result.map(getRequest(target, User[].class), arr -> Arrays.stream(arr).toList());
    }
}
