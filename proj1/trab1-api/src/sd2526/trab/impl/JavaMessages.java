package sd2526.trab.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.hibernate.Session;

import sd2526.trab.api.Message;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;

import static sd2526.trab.api.java.Result.ErrorCode.*;
import static sd2526.trab.api.java.Result.error;

public class JavaMessages implements Messages {

    private static final Logger log = Logger.getLogger(JavaMessages.class.getName());
    private static final long DELETE_WINDOW_MS = 30_000L;

    private Users users;
    private final Hibernate db = Hibernate.getInstance();

    public void setUsers(Users users) {
        this.users = users;
    }



    // ---- operations ----

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        log.info("postMessage(pwd -> %s, msg -> %s)\n".formatted(pwd, msg));

        // validate inputs
        if (pwd == null || !isValidMessage(msg))
            return error(BAD_REQUEST);

        // sender can be "name" or "name@domain"
        String senderName = msg.getSender().contains("@")
                ? msg.getSender().split("@")[0]
                : msg.getSender();

        // authenticate sender
        var senderResult = users.getUser(senderName, pwd);
        if (!senderResult.isOK())
            return error(FORBIDDEN);

        User sender = senderResult.value();

        // format sender as "display name <name@domain>"
        msg.setSender("%s <%s@%s>".formatted(
                sender.getDisplayName(),
                sender.getName(),
                sender.getDomain()));

        // assign unique id
        msg.setId(UUID.randomUUID().toString());

        String senderDomain = sender.getDomain();

        return db.execTransaction(s -> {
            // check if message already exists to ensure idempotency
            if (s.get(Message.class, msg.getId()) == null) {
                s.persist(msg);
            }

            for (String dest : msg.getDestination()) {
                String[] parts = dest.split("@");
                String destName = parts[0];
                String destDomain = (parts.length > 1) ? parts[1] : senderDomain;

                // local delivery
                if (destDomain.equals(senderDomain)) {
                    // check if local user exists
                    if (s.get(User.class, destName) != null) {
                        String entryId = destName + "_" + msg.getId();

                        // only persist if the InboxEntry doesn't already exist
                        if (s.get(InboxEntry.class, entryId) == null) {
                            s.persist(new InboxEntry(destName, msg.getId()));
                        }
                    }else sendErrorNotification(s, msg, destName, destDomain);
                }
                // TODO: Handle remote domains
            }
            return Result.ok(msg.getId());
        });

    }


    private void sendErrorNotification(Session s, Message original, String failedUser, String unknownUser) {
        Message notification = new Message(
                original.getId() + "." + failedUser.split("@")[0],                         // id = mid.user
                original.getSender(),                                                               // not specified, staying the same
                original.getSender(),                                                               // not specified, but notification is destined to sender
                ("FAILED TO SEND " + original.getId() + " TO " + failedUser + ": " + unknownUser),  // notification subject format
                original.getContents());                                                            // original content

        s.persist(notification);                                       // persist notification(message) to db
        String senderName = original.getSender().split("@")[0];
        s.persist(new InboxEntry(senderName, notification.getId()));   // persist new inbox entry for original sender
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        log.info("getInboxMessage(name -> %s, mid -> %s, pwd -> %s)\n".formatted(name, mid, pwd));

        if (!isValidInboxRequest(name, mid, pwd)) {
            log.warning("Invalid inbox request parameters for: " + name);
            return error(BAD_REQUEST);
        }

        var userResult = users.getUser(name, pwd);
        if (!userResult.isOK()){
            log.warning("User not found: " + name+" - Status: "+ userResult.error());
            return error(FORBIDDEN);
        }

        log.info("Finding InboxEntry for: " + name + " message id: "+mid);
        var entry = db.get(InboxEntry.class, name + "_" + mid);
        if (entry == null){
            log.severe("FAIL: InboxEntry not found: " + name+" msgId: "+mid);
            return error(NOT_FOUND);
        }

        log.info("Found InboxEntry, fetching message content for message id: " + mid);
        var msg = db.get(Message.class, mid);
        if (msg == null){
            log.severe("FAIL: Inbox entry exists, but message content is missing for: "+mid);
            return error(NOT_FOUND);
        }

        return Result.ok(msg);
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        log.info("getAllInboxMessages(name -> %s, pwd -> %s)\n".formatted(name, pwd));

        if (!isValidUserRequest(name, pwd))
            return error(BAD_REQUEST);

        var userResult = users.getUser(name, pwd);
        if (!userResult.isOK())
            return error(FORBIDDEN);

        List<InboxEntry> entries = db.jpql(
                "SELECT e FROM InboxEntry e WHERE e.owner = '" + name + "'", InboxEntry.class);

        return Result.ok(entries.stream().map(InboxEntry::getMessageId).toList());
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        log.info("removeInboxMessage(name -> %s, mid -> %s, pwd -> %s)\n".formatted(name, mid, pwd));

        if (!isValidInboxRequest(name, mid, pwd))
            return error(BAD_REQUEST);

        var userResult = users.getUser(name, pwd);
        if (!userResult.isOK())
            return error(FORBIDDEN);

        return db.execTransaction(s -> removeInboxMessageTx(name, mid, s));
    }

    private static Result<Void> removeInboxMessageTx(String name, String mid, Session s) {
        var entry = s.get(InboxEntry.class, name + "_" + mid);
        if (entry == null)
            return error(NOT_FOUND);
        s.remove(entry);
        return Result.ok();
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        log.info("deleteMessage(name -> %s, mid -> %s, pwd -> %s)\n".formatted(name, mid, pwd));

        if (!isValidInboxRequest(name, mid, pwd))
            return error(BAD_REQUEST);

        var userResult = users.getUser(name, pwd);
        if (!userResult.isOK())
            return error(FORBIDDEN);

        return db.execTransaction(s -> deleteMessageTx(name, mid, s));
    }

    private Result<Void> deleteMessageTx(String name, String mid, Session s) {
        var msg = s.get(Message.class, mid);
        if (msg == null)
            return Result.ok(); // spec says no error if not found

        // only delete if posted less than 30 seconds ago
        long age = System.currentTimeMillis() - msg.getCreationTime();
        if (age > DELETE_WINDOW_MS)
            return Result.ok(); // silently ignore

        // verify the deleter is the sender
        // sender is stored as "display name <name@domain>"
        String senderName = msg.getSender().replaceAll(".*<(.+)@.*>", "$1");
        if (!senderName.equals(name))
            return error(FORBIDDEN);

        // remove from all local inboxes
        List<InboxEntry> entries = db.jpql(
                "SELECT e FROM InboxEntry e WHERE e.messageId = '" + mid + "'", InboxEntry.class);
        for (var entry : entries)
            s.remove(entry);

        s.remove(msg);
        return Result.ok();
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        log.info("searchInbox(name -> %s, pwd -> %s, query -> %s)\n".formatted(name, pwd, query));

        if (!isValidUserRequest(name, pwd) || query == null)
            return error(BAD_REQUEST);

        var userResult = users.getUser(name, pwd);
        if (!userResult.isOK())
            return error(FORBIDDEN);

        String q = query.toLowerCase();

        List<InboxEntry> entries = db.jpql(
                "SELECT e FROM InboxEntry e WHERE e.owner = '" + name + "'", InboxEntry.class);

        List<String> hits = entries.stream()
                .map(e -> db.get(Message.class, e.getMessageId()))
                .filter(msg -> msg != null &&
                        (msg.getSubject().toLowerCase().contains(q) ||
                                msg.getContents().toLowerCase().contains(q)))
                .map(Message::getId)
                .toList();

        return Result.ok(hits);
    }

    // ---- validation helpers ----

    private boolean isValidMessage(Message msg) {
        return msg != null
                && msg.getSender() != null && !msg.getSender().isEmpty()
                && msg.getDestination() != null && !msg.getDestination().isEmpty()
                && msg.getSubject() != null && !msg.getSubject().isEmpty()
                && msg.getContents() != null && !msg.getContents().isEmpty();
    }

    private boolean isValidInboxRequest(String name, String mid, String pwd) {
        return name != null && !name.isEmpty()
                && mid != null && !mid.isEmpty()
                && pwd != null && !pwd.isEmpty();
    }

    private boolean isValidUserRequest(String name, String pwd) {
        return name != null && !name.isEmpty()
                && pwd != null && !pwd.isEmpty();
    }
}