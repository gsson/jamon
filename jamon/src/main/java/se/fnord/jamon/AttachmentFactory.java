package se.fnord.jamon;

import java.util.List;

public interface AttachmentFactory {
	Object create(String value, List<Node> children);
}
