package sd2526.trab.network;




import java.net.URI;
import java.util.HashSet;

import jakarta.ws.rs.core.UriBuilder;
import sd2526.trab.api.Message;
import sd2526.trab.api.User;
import sd2526.trab.api.grpc.Messages;
import sd2526.trab.api.grpc.Users;


public class Adapter {
    
    public static User GrpcUser_to_User(Users.GrpcUser from )  {
		var name = from.getName().isEmpty() ? null : from.getName();
		var pwd = from.hasPwd() ? from.getPwd() : null;
		var domain = from.hasDomain() ? from.getDomain() : null;
		var displayName = from.hasDisplayName() ? from.getDisplayName() : null;
		return new User(name, pwd, displayName, domain);
	}

	public static Users.GrpcUser User_to_GrpcUser(User from )  {
		var b = Users.GrpcUser.newBuilder();
		if (from.getName() != null)
			b.setName(from.getName());
		if (from.getPwd() != null)
			b.setPwd(from.getPwd());
		if (from.getDomain() != null)
			b.setDomain(from.getDomain());
		if (from.getDisplayName() != null)
			b.setDisplayName(from.getDisplayName());
		return b.build();
	}

	public static Message GrpcMessages_to_Message(Messages.GrpcMessage from) {
		var messageId = from.getId().isEmpty() ? null : from.getId();
		var sender = from.getSender().isEmpty() ? null : from.getSender();
        var destination = new HashSet<>(from.getDestinationList());
		var subject = from.getSubject().isEmpty() ? null : from.getSubject();
		var contents = from.getContents().isEmpty() ? null : from.getContents();
		return new Message(messageId, sender, destination, subject, contents);
	}

	public static Messages.GrpcMessage Messages_to_GrpcMessage(Message from) {
		var b = Messages.GrpcMessage.newBuilder();
		if (from.getId() != null)
			b.setId(from.getId());
		if (from.getSender() != null)
			b.setSender(from.getSender());
		b.setCreationTime(from.getCreationTime());
		if (from.getDestination() != null && !from.getDestination().isEmpty())
			b.addAllDestination(from.getDestination());
		if (from.getSubject() != null)
			b.setSubject(from.getSubject());
		if (from.getContents() != null)
			b.setContents(from.getContents());
		return b.build();
	}

	public static String extractIdFromUrl(String postUrl) {
		var splitUrl = postUrl.split("/");
		return splitUrl[splitUrl.length - 1];
	}

	public static String incorporateUrlToId(URI base, String postId) {
		return UriBuilder.fromUri(base).path(postId).build().toASCIIString();
	}
}
