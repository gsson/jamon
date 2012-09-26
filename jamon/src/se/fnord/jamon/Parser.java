package se.fnord.jamon;

public interface Parser extends Consumer {
	Parser attach(Object o);
	Parser attachmentFactory(AttachmentFactory f);
}
