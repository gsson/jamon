package se.fnord.jamon;

import static org.junit.Assert.assertEquals;
import static se.fnord.jamon.Matchers.digit;
import static se.fnord.jamon.Parsers.delimitedSequence;
import static se.fnord.jamon.Parsers.exact;
import static se.fnord.jamon.Parsers.lalternative;
import static se.fnord.jamon.Parsers.matches;
import static se.fnord.jamon.Parsers.parse;
import static se.fnord.jamon.Parsers.reference;
import static se.fnord.jamon.Parsers.sequence;
import static se.fnord.jamon.Parsers.skip;
import static se.fnord.jamon.Parsers.strip;

import java.util.Iterator;

import org.junit.Test;

public class Calculator {

	public static abstract class Evaluator {
		public abstract Long evaluate(Node n);

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	public static class Expr extends Evaluator {
		public Long evaluate(Node n) {
			final Iterator<Node> i = n.children().iterator();
			Long l = eval(i.next());
			while (i.hasNext()) {
				l = reduce(i.next(), l, eval(i.next()));
			}
			return l;
		}
	}

	public static class Literal extends Evaluator {
		public Long evaluate(Node n) {
			return Long.parseLong(n.value());
		}
	}

	public static class Negate extends Evaluator {
		public Long evaluate(Node n) {
			return -eval(n.firstChild());
		}
	}

	public static abstract class Reducer {
		public abstract Long reduce(Long first, Long second);

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	public static class Mul extends Reducer {
		@Override
		public Long reduce(Long first, Long second) {
			return first * second;
		}
	}

	public static class Div extends Reducer {
		@Override
		public Long reduce(Long first, Long second) {
			return first / second;
		}
	}

	public static class Add extends Reducer {
		@Override
		public Long reduce(Long first, Long second) {
			return first + second;
		}
	}

	public static class Sub extends Reducer {
		@Override
		public Long reduce(Long first, Long second) {
			return first - second;
		}
	}

	private static Long eval(Node n) {
		return ((Evaluator) n.attachment()).evaluate(n);
	}

	private static Long reduce(Node n, Long first, Long second) {
		return ((Reducer) n.attachment()).reduce(first, second);
	}

	public static Consumer createSimpleParser() {
		final Evaluator termEvaluator = new Expr();
		final ParserReference exprRef = reference();

		final Consumer mulOp = strip(lalternative(
			exact("*").attach(new Mul()),
			exact("/").attach(new Div())));
		final Consumer addOp = strip(lalternative(
			exact("+").attach(new Add()),
			exact("-").attach(new Sub())));

		final ParserReference atomRef = reference();
		final Consumer atom = lalternative(
		    	matches(digit()).attach(new Literal()),
		        sequence(skip("("), strip(exprRef), skip(")")).attach(termEvaluator),
				sequence(strip(skip("-")), atomRef).attach(new Negate())
		);
		atomRef.setTarget(atom);

		final Parser term = delimitedSequence(atom, mulOp).attach(termEvaluator);
		final Parser expr = delimitedSequence(term, addOp).attach(termEvaluator);

		exprRef.setTarget(expr);
		return strip(expr);
	}

	public static Consumer createParser() {
		final Evaluator termEvaluator = new Expr();

		final ParserReference exprRef = reference();
		final Consumer mulOp = strip(lalternative(
			exact("*").attach(new Mul()),
			exact("/").attach(new Div())));
		final Consumer addOp = strip(lalternative(
			exact("+").attach(new Add()),
			exact("-").attach(new Sub())));

		final ParserReference atomRef = reference();
		final Consumer atom = lalternative(
	    	matches(digit()).attach(new Literal()),
	        sequence(skip("("), strip(exprRef), skip(")")).attach(termEvaluator),
			sequence(strip(skip("-")), atomRef).attach(new Negate())
		);
		atomRef.setTarget(atom);

		final Consumer term = lalternative(
			delimitedSequence(2, atom, mulOp).attach(termEvaluator),
			atom);

		final Consumer expr = lalternative(
			delimitedSequence(2, term, addOp).attach(termEvaluator),
			term,
			atom);

		exprRef.setTarget(expr);
		return strip(expr);
	}

	private static void assertEval(long value, Node n) {
		assertEquals(Long.valueOf(value), eval(n));
	}

	@Test
	public void testSimpleParser() throws ParseException, FatalParseException {
		final Consumer p = createSimpleParser();
		assertEval(0, parse(p, "0"));
		assertEval(1, parse(p, "1"));
		assertEval(0, parse(p, " 0 "));

		assertEval(-2, parse(p, " -1 * 2 "));
		assertEval(2, parse(p, "1*2"));
		assertEval(4, parse(p, "2*2"));
		assertEval(1, parse(p, "2/2"));
		assertEval(2, parse(p, "3*2/3"));

		assertEval(2, parse(p, "1+1"));
		assertEval(0, parse(p, "(1+1)*0"));
		assertEval(1, parse(p, "1+1*0"));
		assertEval(13, parse(p, "2*2+3*3"));
		assertEval(30, parse(p, "2*-(-2+-3)*3"));

		assertEval(0, parse(p, "1-1"));
		assertEval(2, parse(p, "1--1"));
		assertEval(1, parse(p, "1+1-1"));
	}

	@Test
	public void testParser() throws ParseException, FatalParseException {
		final Consumer p = createParser();
		assertEval(0, parse(p, "0"));
		assertEval(1, parse(p, "1"));
		assertEval(0, parse(p, " 0 "));

		assertEval(-2, parse(p, " -1 * 2 "));
		assertEval(2, parse(p, "1*2"));
		assertEval(4, parse(p, "2*2"));
		assertEval(1, parse(p, "2/2"));
		assertEval(2, parse(p, "3*2/3"));

		assertEval(2, parse(p, "1+1"));
		assertEval(0, parse(p, "(1+1)*0"));
		assertEval(1, parse(p, "1+1*0"));
		assertEval(13, parse(p, "2*2+3*3"));
		assertEval(30, parse(p, "2*-(-2+-3)*3"));

		assertEval(0, parse(p, "1-1"));
		assertEval(2, parse(p, "1--1"));
		assertEval(1, parse(p, "1+1-1"));
	}
}
