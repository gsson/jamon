package se.fnord.jamon;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import se.fnord.jamon.internal.Contexts;

public final class Parsers {
	public static final class Group implements Consumer {
		private final String name;
		private final Consumer parser;
		public Group(String name, Consumer parser) {
			this.name = name;
			this.parser = parser;
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			try {
				return parser.consume(input, parent);
			}
			catch (ParseException e) {
				throw new ParseException("Group " + name, e);
			}
			catch (FatalParseException e) {
				throw new FatalParseException("Group " + name, e);
			}
		}

		@Override
		public String toString() {
			return String.format("group[name=\"%s\", parser=%s]", name, parser);
		}
	}

	private static final class StaticAttachmentFactory implements AttachmentFactory {
		private final Object o;

		public StaticAttachmentFactory(Object o) {
			this.o = o;
		}

		@Override
		public Object create(String value, List<Node> children) {
			return o;
		}

		@Override
		public String toString() {
			return Objects.toString(o);
		}
	}

	private static abstract class AbstractConsumer implements Consumer {
		void doApply(Node parent, Node me) {
			throw new UnsupportedOperationException();
		}

		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
			throw new UnsupportedOperationException();
		}

		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			Node me;
			me = input.consumerMatched(this);
			if (me != null) {
				doApply(parent, me);
				return input.splice(me.end());
			}

			try {
				me = new Node(null);
				final ParseContext remaining = doConsume(input, me);
				me.start(input.start());
				me.end(remaining.start());
				input.consumerMatches(this, me);
				doApply(parent, me);
				return remaining;
			}
			catch (ParseException e) {
				input.consumerMismatches(this);
				throw e;
			}
		}
	}

	private static abstract class AbstractParser extends AbstractConsumer implements Parser {
		private static final AttachmentFactory NULL_FACTORY = new StaticAttachmentFactory(null);
		protected final AttachmentFactory attachmentFactory;

		public AbstractParser(AttachmentFactory attachmentFactory) {
			this.attachmentFactory = attachmentFactory == null ? NULL_FACTORY : attachmentFactory;
		}

		public AbstractParser() {
			this(null);
		}

		protected Object createAttachment(String value, List<Node> children) {
			return attachmentFactory.create(value, children);
		}

		protected Object createAttachment(String value) {
			return attachmentFactory.create(value, Collections.<Node> emptyList());
		}

		@Override
		public final Parser attach(Object o) {
			return attachmentFactory(new StaticAttachmentFactory(o));
		}
	}

	private static final class ParserReferenceImpl implements ParserReference {
		private Consumer target = null;

		@Override
		public String toString() {
			return "reference[]";
		}

		@Override
		public void setTarget(Consumer parser) {
			this.target = parser;
		}

		@Override
		public Parser attach(Object o) {
			throw new IllegalStateException("Can not add attachments to references");
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			throw new IllegalStateException("Can not add attachments to references");
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			return target.consume(input, parent);
		}
	}

	private static final class AlternativeParser extends AbstractParser {
		private final Consumer[] parsers;

		private AlternativeParser(AttachmentFactory attachmentFactory, Consumer[] parsers) {
			super(attachmentFactory);
			this.parsers = parsers.clone();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("alternative[");
			if (attachmentFactory != null)
				sb.append(attachmentFactory).append(", ");
			sb.append("{").append(parsers[0]);
			for (int i = 1; i < parsers.length; i++)
				sb.append(", ").append(parsers[i]);
			sb.append("}]");
			return sb.toString();
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			return new AlternativeParser(f, parsers);
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
			for (Consumer parser : parsers) {
				try {
					final ParseContext remaining = parser.consume(input, me);
					me.attachment(createAttachment(me.value(), me.children()));
					return remaining;
				}
				catch (ParseException e) {
				}
			}
			throw new ParseException("No matching alternative");
		}

		@Override
		void doApply(Node parent, Node me) {
			parent.addChildren(me);
		}
	}

	// TODO: Make me a Transformer
	private static final class JoinTransform extends AbstractParser {
		private final Consumer parser;
		private final String joint;

		private JoinTransform(Consumer parser, String joint, AttachmentFactory attachmentFactory) {
			super(attachmentFactory);
			this.parser = parser;
			this.joint = joint;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("compress[");
			if (attachmentFactory != null)
				sb.append(attachmentFactory).append(", ");
			sb.append(parser);
			sb.append("]");
			return sb.toString();
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			return new JoinTransform(parser, joint, f);
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me)
				throws ParseException, FatalParseException {
			return parser.consume(input, me);
		}

		@Override
		void doApply(Node parent, Node me) {
			parent.addChildren(new Node(me.start(), me.end(), join(joint, me), createAttachment(me.value(), me.children())));
		}
	}

	private static final class SequenceParser extends AbstractParser {
		private final Consumer[] parsers;

		private SequenceParser(AttachmentFactory attachmentFactory, Consumer[] parsers) {
			super(attachmentFactory);
			this.parsers = parsers.clone();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("sequence[");
			if (attachmentFactory != null)
				sb.append(attachmentFactory).append(", ");
			sb.append("{").append(parsers[0]);
			for (int i = 1; i < parsers.length; i++)
				sb.append(", ").append(parsers[i]);
			sb.append("}");
			return sb.toString();
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			return new SequenceParser(f, parsers);
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
			try {
				for (Consumer parser : parsers)
					input = parser.consume(input, me);
				me.attachment(createAttachment(me.value(), me.children()));
			}
			catch (ParseException e) {
				throw new ParseException("Sequence failed", e);
			}
			return input;
		}

		@Override
		void doApply(Node parent, Node me) {
			parent.addChildren(me);
		}
	}

	private static final class RepeatParser extends AbstractParser {
		private final Consumer parser;
		private final int min;
		private final int max;

		private RepeatParser(Consumer parser, AttachmentFactory attachmentFactory, int min, int max) {
			super(attachmentFactory);
			this.parser = parser;
			this.min = min;
			this.max = max;
		}

		@Override
		public String toString() {
			if (attachmentFactory != null)
				return "repeat[" + attachmentFactory + ", " + min + ", " + max + ", " + parser + "]";
			return "repeat[" + min + ", " + max + ", " + parser + "]";
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			return new RepeatParser(parser, f, min, max);
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
			int i = 0;
			try {
				while (max == -1 || i < max) {
					input = parser.consume(input, me);
					i++;
				}
				me.attachment(createAttachment(me.value(), me.children()));
			}
			catch (ParseException e) {
			}
			if (i < min)
				throw new ParseException("Out of bounds");
			return input;
		}

		@Override
		void doApply(Node parent, Node me) {
			parent.addChildren(me);
		}
	}

	private static final class LiftTransform extends AbstractParser {
		private final Consumer parser;

		private LiftTransform(Consumer parser) {
			this.parser = parser;
		}

		@Override
		public String toString() {
			return "lift[" + parser + "]";
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory o) {
			return new LiftAndReplaceAttachmentTransform(o, parser);
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
		    return parser.consume(input, me);
		}

		@Override
		void doApply(Node parent, Node me) {
			for (Node m : me.children())
				parent.addChildren(m.children());
		}
	}

	private static final class LiftAndReplaceAttachmentTransform extends AbstractParser {
		private final Consumer parser;

		private LiftAndReplaceAttachmentTransform(AttachmentFactory attachmentFactory, Consumer parser) {
			super(attachmentFactory);
			this.parser = parser;
		}

		@Override
		public String toString() {
			return "lift[" + parser + "]";
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory o) {
			return new LiftAndReplaceAttachmentTransform(o, parser);
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
		    return parser.consume(input, me);
		}

		@Override
		void doApply(Node parent, Node me) {
			for (Node m : me.children()) {
				for (Node mm : m.children()) {
					parent.addChildren(new Node(mm.start(), mm.end(), mm.value(), createAttachment(mm.value(), mm.children())));
				}
			}
		}
	}

	private static final class RequireTransform implements Consumer {
		private final Consumer consumer;

		public RequireTransform(Consumer consumer) {
			this.consumer = consumer;
		}

		@Override
		public String toString() {
			return "require[" + consumer + "]";
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			try {
				return consumer.consume(input, parent);
			}
			catch (ParseException e) {
				throw new FatalParseException(e);
			}
		}
	}

	private static final class EndOfInputTransform implements Transformer {
		private EndOfInputTransform() {
		}

		@Override
		public String toString() {
			return "endOfInput";
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			if (input.length() > 0)
				throw new ParseException("End of input expected");
			return input;
		}
	}

	private static final class IgnoreTransform implements Transformer {
		private final Consumer parser;

		private IgnoreTransform(Consumer parser) {
			this.parser = parser;
		}

		@Override
		public String toString() {
			return "ignore[" + parser + "]";
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			return parser.consume(input, new Node(null));
		}
	}

	private static final class InputPreservingParser extends AbstractParser {
		private final Consumer parser;

		private InputPreservingParser(AttachmentFactory attachment, Consumer parser) {
			super(attachment);
			this.parser = parser;
		}

		@Override
		public String toString() {
			return "preserve[" + parser + "]";
		}

		@Override
		ParseContext doConsume(ParseContext input, Node me) throws ParseException, FatalParseException {
			parser.consume(input, me);
			me.attachment(createAttachment(me.value(), me.children()));
		    return input;
		}

		@Override
		void doApply(Node parent, Node me) {
			parent.addChildren(me);
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			return new InputPreservingParser(f, parser);
		}
	}

	private static final class ExactParser extends AbstractParser {
		private final String token;

		private ExactParser(AttachmentFactory attachment, String token) {
			super(attachment);
			this.token = token;
		}

		@Override
		public String toString() {
			return "exact[\"" + token + "\"]";
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory f) {
			return new ExactParser(f, token);
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			int i;
			if (input.length() < token.length())
				throw new ParseException(String.format("Token mismatch (%s)", token));
			for (i = 0; i < token.length(); i++) {
				if (token.charAt(i) != input.charAt(i))
					throw new ParseException(String.format("Token mismatch (%s)", token));
			}

			final Node me = input.node(i, token, createAttachment(token));
			parent.addChildren(me);
			return input.splice(input.start() + i);
		}
	}

	private static final class MatchParser extends AbstractParser {
		private final int min;
		private final int max;
		private final Matcher matcher;

		private MatchParser(AttachmentFactory attachment, int min, int max, Matcher matcher) {
			super(attachment);
			this.min = min;
			this.max = max;
			this.matcher = matcher;
		}

		@Override
		public String toString() {
			return "matches[" + min + ", " + max + ", " + matcher + "]";
		}

		@Override
		public Parser attachmentFactory(AttachmentFactory o) {
			return new MatchParser(o, min, max, matcher);
		}

		public ParseContext consume(ParseContext input, Node parent) throws ParseException {
			int i;
			int m;
			if (max != -1)
				m = Math.min(input.length(), max);
			else
				m = input.length();

			for (i = 0; i < m; i++) {
				if (!matcher.match(input.charAt(i))) {
					break;
				}
			}

			if (i < min)
				throw new ParseException(String.format("Match count out of bounds (%d >= %d)", i, min));

			final Node me = input.node(i, null);
			me.attachment(createAttachment(me.value(), me.children()));
			parent.addChildren(me);
			return input.splice(input.start() + i);
		}
	}

	private static final class StaticTranslator implements Translator {
		private final String replacement;

		public StaticTranslator(final String replacement) {
			this.replacement = replacement;
		}

		@Override
		public String translate(final String input) {
			return replacement;
		}
	}

	private static final class ReplaceTransform implements Transformer {
		private final Translator translator;
		private final Consumer parser;

		private ReplaceTransform(Translator translator, Consumer parser) {
			this.translator = translator;
			this.parser = parser;
		}

		@Override
		public ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException {
			final Node x = new Node(null);
			final ParseContext remaining = parser.consume(input, x);
			if (x.children().size() != 1)
				throw new IllegalStateException();
			final Node valueNode = x.children().get(0);
			final String value = translator.translate(valueNode.value());
			final Node me = input.node(remaining.start() - input.start(), value, valueNode.attachment());
			me.addChildren(valueNode.children());
			parent.addChildren(me);
			return remaining;
		}
	}

	private Parsers() {
	}

	private static String join(final String joint, final Node node) {
		StringBuilder sb = new StringBuilder();
		join("", joint, node, sb);
		return sb.toString();
	}

	private static void join(final String prefix, final String joint, final Node node, StringBuilder sb) {
		Iterator<Node> i = node.children().iterator();
		if (!i.hasNext())
			return;
		Node n = i.next();
		sb.append(prefix);
		if (n.value() != null) {
			sb.append(n.value());
			join(joint, joint, n, sb);
		}
		else {
			join("", joint, n, sb);
		}
		while (i.hasNext()) {
			n = i.next();
			sb.append(joint);
			if (n.value() != null) {
				sb.append(n.value());
				join(joint, joint, n, sb);
			}
			else {
				join("", joint, n, sb);
			}
		}
	}

	/**
	 * Returns a parser that creates a node according to the parser
	 * parameter. Converts all ParseExceptions into
	 * FatalParseExceptions.
	 * <p>
	 * @param parser The parser that will create the node
	 *
	 * @return The composite parser
	 */
	public static Consumer require(final Parser parser) {
		return new RequireTransform(parser);
	}

	/**
	 * Returns a parser that creates a node according to the parser
	 * parameter except that all characters accepted by the strip matcher
	 * is removed before and after the parsed section.
	 * <p>
	 * Equivalent to:
	 * <p>
	 * <code>lsequence(ignore(strip), parser, ignore(strip))</code>
	 * <p>
	 * Example:
	 * <p>
	 * <code>strip(matches(Matchers.digit()), Matchers.white())</code>
	 * <p>
	 * @param parser The parser that will create the node
	 * @param strip The matcher that indicates characters to strip.
	 *
	 * @return The composite parser
	 */
	public static Parser strip(final Consumer parser, final Matcher strip) {
		return lsequence(ignore(strip), parser, ignore(strip));
	}

	/**
	 * Returns a parser that creates a node according to the parser
	 * parameter except that all (non-line-delimiting) whitespace before
	 * and after the parsed section is removed.
	 * <p>
	 * Equivalent to:
	 * <p>
	 * <code>strip(parser, Matchers.white())</code>
	 * <p>
	 * @param parser The parser that will create the node
	 *
	 * @return The composite parser
	 */
	public static Parser strip(final Consumer parser) {
		return strip(parser, Matchers.white());
	}

	/**
	 * Returns a parser that creates a node with all characters accepted by the matcher.
	 * <p>
	 * Successful matching requires at least one matching character.
	 * <p>
	 * Equivalent to:
	 * <p>
	 * <code>matches(1, -1, matcher)</code>
	 * <p>
	 * @param matcher The matcher to collect characters
	 *
	 * @return The parser
	 */
	public static Parser matches(final Matcher matcher) {
		return matches(1, -1, matcher);
	}

	/**
	 * Returns a parser that creates a node with all characters accepted by the matcher.
	 * <p>
	 * Successful matching requires at least min matching character(s).
	 * <p>
	 * Equivalent to:
	 * <p>
	 * <code>matches(min, -1, matcher)</code>
	 * <p>
	 * @param min The minimum number of matching characters for this parser to return successfully.
	 * @param matcher The matcher to collect characters
	 *
	 * @return The parser
	 */
	public static Parser matches(final int min, final Matcher matcher) {
		return matches(min, -1, matcher);
	}

	/**
	 * Returns a parser that creates a node with all characters accepted by the matcher.
	 * <p>
	 * Successful matching requires at least min matching character(s).
	 * <p>
	 * @param min The minimum number of matching characters for this parser to return successfully.
	 * @param max The maximum number of matching characters for this parser to return successfully.
	 * @param matcher The matcher to collect characters
	 *
	 * @return The parser
	 */
	public static Parser matches(final int min, final int max, final Matcher matcher) {
		return new MatchParser(null, min, max, matcher);
	}

	/**
	 * Returns a parser that creates a node with all characters accepted by the matcher.
	 * <p>
	 * Successful matching requires at least min matching character(s) but will never
	 * match more than max matching characters.
	 * <p>
	 * Equivalent to:
	 * <p>
	 * <code>matches(min, max, Matchers.match(chars))</code>
	 * <p>
	 * @param min The minimum number of matching characters for this parser to return successfully.
	 * @param max The maximum number of matching characters for this parser to return successfully.
	 * @param chars The characters to match
	 *
	 * @return The parser
	 */
	public static Parser matches(final int min, final int max, final char ... chars) {
		return new MatchParser(null, min, max, Matchers.match(chars));
	}

	/**
	 * Returns a parser matches exactly the provided string.
	 * <p>
	 * @param token The string to match
	 *
	 * @return The parser
	 */
	public static Parser exact(final String token) {
		if (token.isEmpty())
			throw new IllegalArgumentException();

		return new ExactParser(null, token);
	}

	/**
	 * Returns a parser matches exactly one of the provided strings.
	 * <p>
	 * @param alternatives The strings to match
	 *
	 * @return The parser
	 */
	public static Parser oneOf(final String... alternatives) {
		Parser[] parsers = new Parser[alternatives.length];
		for (int i = 0; i < alternatives.length; i++)
			parsers[i] = exact(alternatives[i]);

		return lalternative(parsers);
	}

	public static Transformer ignore(final Matcher matcher) {
		return skip(matches(0, -1, matcher));
	}

	public static Transformer skip(final String token) {
		return skip(exact(token));
	}

	public static Transformer skip(final Matcher matcher) {
		return skip(matches(matcher));
	}

	public static Transformer skip(final Consumer parser) {
		return new IgnoreTransform(parser);
	}

	public static Parser lift(final Consumer parser) {
		return new LiftTransform(parser);
	}

	public static Transformer replace(final Consumer parser, final String replacement) {
		return replace(parser, new StaticTranslator(replacement));
	}

	public static Transformer replace(final Consumer parser, final Translator translator) {
		return new ReplaceTransform(translator, parser);
	}

	public static Parser optional(final Consumer parser) {
		return repeat(0, 1, parser);
	}

	public static Parser loptional(final Consumer parser) {
		return lift(optional(parser));
	}

	public static Parser lrepeat(final Consumer parser) {
		return lift(repeat(parser));
	}

	public static Parser lrepeat(final int min, final int max, final Consumer parser) {
		return lift(repeat(min, max, parser));
	}

	public static Parser repeat(final Consumer parser) {
		return repeat(0, -1, parser);
	}

	public static Parser repeat(final int min, final Consumer parser) {
		return repeat(min, -1, parser);
	}

	public static Parser repeat(final int min, final int max, final Consumer parser) {
		return new RepeatParser(parser, null, min, max);
	}

	/**
	 * Parses a sequence between two simple delimiters, for example ( seq )
	 * <p>
	 * @param lsep left separator
	 * @param item parser called between separators
	 * @param rsep right separator
	 *
	 * @return Parser
	 */
	public static Parser between(final String lsep, final Consumer item, final String rsep) {
		return sequence(skip(lsep), item, skip(rsep));
	}

	/**
	 * Variant of between() that lifts central item
	 * <p>
	 * @param lsep left separator
	 * @param item parser called between separators
	 * @param rsep right separator
	 *
	 * @return Parser
	 */
	public static Parser lbetween(final String lsep, final Consumer item, final String rsep) {
		return sequence(skip(lsep), lift(item), skip(rsep));
	}

	public static Parser delimitedSequence(final Consumer item, final String delimiter) {
		return delimitedSequence(1, item, skip(delimiter));
	}

	public static Parser delimitedSequence(final Consumer item, final Matcher delimiter) {
		return delimitedSequence(1, item, skip(delimiter));
	}

	public static Parser delimitedSequence(final Consumer item, final Consumer delimiter) {
		return delimitedSequence(1, item, delimiter);
	}

	public static Parser delimitedSequence(final int min, final Consumer item, final String delimiter) {
		return delimitedSequence(min, item, skip(delimiter));
	}

	public static Parser delimitedSequence(final int min, final Consumer item, final Matcher delimiter) {
		return delimitedSequence(min, item, skip(delimiter));
	}

	public static Parser delimitedSequence(final int min, final Consumer item, final Consumer delimiter) {
		if (min == 0)
			return sequence(loptional(lsequence(item, lrepeat(lsequence(delimiter, item)))));
		return sequence(item, lrepeat(min - 1, -1, lsequence(delimiter, item)));
	}

	public static Parser lsequence(final Consumer... parsers) {
		return lift(sequence(parsers));
	}

	public static Parser sequence(final Consumer... parsers) {
		return new SequenceParser(null, parsers);
	}

	public static Parser preserve(final Consumer parser) {
		return new InputPreservingParser(null, parser);
	}

	public static Transformer peek(final Consumer parser) {
		return skip(preserve(parser));
	}

	public static Parser join(final Consumer parser) {
		return new JoinTransform(parser, "", null);
	}

	public static Parser join(final Consumer parser, final String joint) {
		return new JoinTransform(parser, joint, null);
	}

	public static Parser lalternative(final Consumer... parsers) {
		return lift(alternative(parsers));
	}

	public static Parser alternative(final Consumer... parsers) {
		if (parsers.length == 0)
			throw new IllegalArgumentException("At least one parser required as argument");
		return new AlternativeParser(null, parsers);
	}

	public static Transformer endOfInput() {
		return new EndOfInputTransform();
	}

	public static Parser terminal(Consumer p) {
		return lsequence(p, endOfInput());
	}

	public static ParserReference reference() {
		return new ParserReferenceImpl();
	}

	/**
	 * Creates a grouping consumer.
	 *
	 * Mostly useful for debugging. Transparently delegates the consume() call to the provided parser.
	 *
	 * @param name The name of the group
	 * @param parser The parser to delegate to
	 * @return The group
	 */
	public static Group group(final String name, final Consumer parser) {
		return new Group(name, parser);
	}

	public static Node parse(Consumer parser, CharSequence input) throws ParseException, FatalParseException {
		final ParseContext context = Contexts.create(input.toString());
		final Node root = new Node(null);
		final ParseContext remaining = parser.consume(context, root);
		if (remaining.length() > 0)
			throw new ParseException("Remaining characters: " + remaining.toString());
		return root.firstChild();
	}

	public static Node sloppyParse(Consumer parser, CharSequence input) throws ParseException, FatalParseException {
		final ParseContext context = Contexts.create(input.toString());
		final Node root = new Node(null);
		parser.consume(context, root);
		return root.firstChild();
	}
}
