package se.fnord.jamon;

import static org.junit.Assert.assertEquals;
import static se.fnord.jamon.Matchers.letter;
import static se.fnord.jamon.Parsers.alternative;
import static se.fnord.jamon.Parsers.exact;
import static se.fnord.jamon.Parsers.lift;
import static se.fnord.jamon.Parsers.matches;
import static se.fnord.jamon.Parsers.parse;
import static se.fnord.jamon.Parsers.sequence;

import org.junit.Test;

public final class TestParsers {
	@Test
	public void testParse() throws ParseException, FatalParseException {
		Node n = parse(matches(letter()), "abc");
		System.err.println(n);
	}

	@Test
	public void testLift() throws ParseException, FatalParseException {
		final Node expected =
		    new Node(0, 3, "Inner").addChildren(new Node(0, 1, "a", "A"), new Node(1, 2, "b", "B"), new Node(2, 3, "d", "D"));

		final Consumer p =
		    sequence(lift(sequence(exact("a").attach("A"), exact("b").attach("B"))), lift(alternative(exact("c").attach("C"), exact("d").attach("D")))).attach("Inner");
		final Node n = Parsers.parse(p, "abd");

		assertEquals(expected, n);
	}
}
