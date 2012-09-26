package se.fnord.jamon.internal;

public final class Util {
	private Util() {}

	public static boolean equals(final Object a, final Object b) {
		return (a == null) ? (b == null) : a.equals(b);
	}

}
