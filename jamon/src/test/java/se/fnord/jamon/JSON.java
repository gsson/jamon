package se.fnord.jamon;

import static se.fnord.jamon.CharacterMatchers.and;
import static se.fnord.jamon.CharacterMatchers.control;
import static se.fnord.jamon.CharacterMatchers.digit;
import static se.fnord.jamon.CharacterMatchers.match;
import static se.fnord.jamon.CharacterMatchers.newline;
import static se.fnord.jamon.CharacterMatchers.not;
import static se.fnord.jamon.CharacterMatchers.or;
import static se.fnord.jamon.CharacterMatchers.white;
import static se.fnord.jamon.Parsers.alternative;
import static se.fnord.jamon.Parsers.between;
import static se.fnord.jamon.Parsers.delimitedSequence;
import static se.fnord.jamon.Parsers.exact;
import static se.fnord.jamon.Parsers.join;
import static se.fnord.jamon.Parsers.lalternative;
import static se.fnord.jamon.Parsers.lbetween;
import static se.fnord.jamon.Parsers.matches;
import static se.fnord.jamon.Parsers.oneOf;
import static se.fnord.jamon.Parsers.optional;
import static se.fnord.jamon.Parsers.parse;
import static se.fnord.jamon.Parsers.reference;
import static se.fnord.jamon.Parsers.repeat;
import static se.fnord.jamon.Parsers.replace;
import static se.fnord.jamon.Parsers.sequence;
import static se.fnord.jamon.Parsers.skip;
import static se.fnord.jamon.Parsers.strip;

import org.junit.Test;

public class JSON {
	enum JsonType {
		STRING,
		NUMBER,
		BOOLEAN,
		NULL,
		OBJECT,
		ARRAY,
		ENTRY
	}
	private static class UnicodeTranslator implements Translator {
		@Override
		public String translate(final String input) {
			char ch =
			    (char) ((Character.digit(input.charAt(2), 16) << 12) | (Character.digit(input.charAt(3), 16) << 8) |
			        (Character.digit(input.charAt(4), 16) << 4) | Character.digit(input.charAt(5), 16));
			return new String(new char[] { ch });
		}
	}

	public static Parser stringParser() {
		final Parser unquoted = matches(not(or(control(), match('\\', '"'))));
		final Parser quoted = alternative(
			replace(exact("\\\""), "\""), replace(exact("\\\\"), "\\"),
		    replace(exact("\\b"), "\b"), replace(exact("\\t"), "\t"),
		    replace(exact("\\f"), "\f"), replace(exact("\\n"), "\n"),
		    replace(exact("\\r"), "\r"), replace(exact("\\/"), "/"),
		    replace(sequence(exact("\\u"), matches(4, 4, digit(16))), new UnicodeTranslator())
		);

		final Parser string =
				join(between("\"", repeat(alternative(unquoted, quoted)), "\"")).attach(JsonType.STRING);
		return string;
	}

	public static Parser numberParser() {
		final Parser nonZero = matches(and(digit(), not('0')));
		final Parser digit = matches(digit());
		return join(sequence(
			optional(exact("-")),
			alternative(
				exact("0"),
				sequence(nonZero, repeat(digit))
			),
			optional(sequence(
				exact("."),
				repeat(1, digit)
			)),
			optional(sequence(
				matches(1, 1, 'e', 'E'),
				optional(matches(1, 1, '+', '-')),
				repeat(1, digit)
			))
		)).attach(JsonType.NUMBER);
	}

	private static Parser unpad(final Consumer parser) {
		return strip(parser, or(newline(), white()));
	}

	public static Consumer createParser() {
		final ParserReference _value = reference();
		final Consumer delimiter = skip(",");

		final Consumer _boolean = unpad(oneOf("true", "false").attach(JsonType.BOOLEAN));
		final Consumer _null = unpad(exact("null").attach(JsonType.NULL));
		final Consumer _string = unpad(stringParser());
		final Consumer _number = unpad(numberParser());

		final Consumer _entry = sequence(_string, skip(":"), _value).attach(JsonType.ENTRY);
		final Consumer _object = unpad(lbetween("{", delimitedSequence(0, _entry, delimiter), "}").attach(JsonType.OBJECT));
		final Consumer _array = unpad(lbetween("[", delimitedSequence(0, _value, delimiter), "]").attach(JsonType.ARRAY));

		_value.setTarget(lalternative(_string, _number, _object, _array, _boolean, _null));

		return _value;
	}

	@Test
	public void testParser() throws ParseException, FatalParseException {
		final Consumer p = createParser();
		System.err.println(parse(p, "1.0").dump());
		System.err.println(parse(p, "0.1").dump());
		System.err.println(parse(p, "-1.0e+1").dump());
		System.err.println(parse(p, "1e1").dump());
		System.err.println(parse(p, "true").dump());
		System.err.println(parse(p, "false").dump());
		System.err.println(parse(p, "[false, true]").dump());
		System.err.println(parse(p, "\"Hello\"").dump());
		System.err.println(parse(p, "1").dump());
		System.err.println(parse(p, "{}").dump());
		System.err.println(parse(p, "[]").dump());
		System.err.println(parse(p, "{\"A\": 1}").dump());
		System.err.println(parse(p, "{\"A\": [1, 2, { \"B\": \"C\", \"D\": null }, \"E\"]}").dump());
	}
}
