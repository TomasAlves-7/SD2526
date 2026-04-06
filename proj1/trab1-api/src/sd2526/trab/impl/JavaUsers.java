package sd2526.trab.impl;

import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import static sd2526.trab.api.java.Result.ErrorCode.BAD_REQUEST;
import static sd2526.trab.api.java.Result.ErrorCode.CONFLICT;
import static sd2526.trab.api.java.Result.ErrorCode.FORBIDDEN;
import static sd2526.trab.api.java.Result.ErrorCode.NOT_FOUND;
import static sd2526.trab.api.java.Result.error;
import sd2526.trab.api.java.Users;
import org.hibernate.Session;

public class JavaUsers implements Users {

    private static final Logger log = Logger.getLogger(JavaUsers.class.getName());

    private Messages messages;

    private final Hibernate db = Hibernate.getInstance();

    public void setMessages(Messages messages) {
        this.messages = messages;
    }
    

    @Override
    public Result<String> postUser(User user) {
        log.info("postUser(user -> %s)\n".formatted(user));
        if(!inputHasAllFields(user)) {
            return error(BAD_REQUEST);
        }
        try{
            db.persist(user);
        } catch (Exception e) {
            log.warning(e.getMessage());
            return error(CONFLICT);
        }
        return Result.ok(user.getName());
    }

    private boolean inputHasAllFields(User user) {
        return user.getName() != null && !user.getName().isEmpty() &&
                user.getPwd() != null && !user.getPwd().isEmpty() &&
                user.getDomain() != null && !user.getDomain().isEmpty() &&
                user.getDisplayName() != null && !user.getDisplayName().isEmpty();
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        log.info("getUser(uid -> %s, pwd -> %s)\n".formatted(name, pwd));
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        var user = db.get(User.class, name);
        if (user == null)
            return error(NOT_FOUND);
        if (!user.getPwd().equals(pwd))
            return error(FORBIDDEN);
        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        log.info("updateUser(uid -> %s, pwd -> %s, updatedFields -> %s)\n".formatted(name, pwd, info));
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        return db.execTransaction(s -> updateUserTx(name, pwd, info, s));
    }

    private static Result<User> updateUserTx(String uid, String pwd, User updatedFields, Session s) {
        var res = getUserInTx(uid, pwd, s);
        if (!res.isOK())
            return res;
        var user = res.value();
        if (updatedFields.getName() != null && !updatedFields.getName().isEmpty())
            user.setName(updatedFields.getName());
        if (updatedFields.getPwd() != null && !updatedFields.getPwd().isEmpty())
            user.setPwd(updatedFields.getPwd());
        if (updatedFields.getDisplayName() != null && !updatedFields.getDisplayName().isEmpty())
            user.setDisplayName(updatedFields.getDisplayName());
        return Result.ok(user);
    }

    private static Result<User> getUserInTx(String uid, String pwd, Session s) {
        var user = s.get(User.class, uid);
        if (user == null)
            return error(NOT_FOUND);
        if (!user.getPwd().equals(pwd))
            return error(FORBIDDEN);
        return Result.ok(user);
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {

        log.info("deleteUser(uid -> %s, pwd -> %s)\n".formatted(name, pwd));
        if (name == null)
            return error(BAD_REQUEST);
        if (pwd == null)
            return error(FORBIDDEN);
        var res = db.execTransaction(s -> deleteUserTx(name, pwd, s));
        if (!res.isOK())
            return res;
        var user = res.value();
        return Result.ok(user);
    }

    private static Result<User> deleteUserTx(String uid, String pwd, Session s) {
        var res = getUserInTx(uid, pwd, s);
        if (!res.isOK())
            return error(res.error());
        var u = res.value();
        s.remove(u);
        return res;
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        log.info("searchUsers(name -> %s, pwd -> %s, query -> %s)\n".formatted(name, pwd, query));
        if (name == null || pwd == null || query == null)
            return error(BAD_REQUEST);
        var user = db.get(User.class, name);
        if (user == null || !user.getPwd().equals(pwd))
            return error(FORBIDDEN);
        query = query == null ? "" : query;
        List<User> users = db.sql("SELECT * FROM User u WHERE UPPER(u.displayName) LIKE UPPER('%" + query + "%')", User.class);
        return Result.ok(users.stream().map(this::hidePwd).toList());
    }

    private User hidePwd(User user) {
        return new User(user.getName(), "", user.getDisplayName(), user.getDomain());
    }


}

