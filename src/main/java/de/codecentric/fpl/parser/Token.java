package de.codecentric.fpl.parser;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * Tokens, immutable class.
 */
public final class Token {
	/**
	 * Token Id
	 */
	public enum Id {
		/** ( */
		LEFT_PAREN(true),
		/** ) */
		RIGHT_PAREN(true),
		/** [ */
		LEFT_SQUARE_BRACKET(true),
		/** ] */
		RIGHT_SQUARE_BRACKET(true),
		/** { */
		LEFT_CURLY_BRACKET(true),
		/** } */
		RIGHT_CURLY_BRACKET(true),
		/** , */
		COMMA(true),
		/** : */
		COLON(true),
		/** 'x, short for (qoute x) */
		QUOTE(true),
		/** Integral number (stored as {@link BigInteger} */
		INTEGER(false),
		/** Double precision number */
		DOUBLE(false),
		/** Symbol */
		SYMBOL(false),
		/** String */
		STRING(false),
		/** String */
		EOF(true);

		private Id(boolean primitive) {
			this.primitive = primitive;
		}

		private final boolean primitive;

		public boolean isPrimitive() {
			return primitive;
		}
	}

	private final Position position;
	private final Id id;
	private final long integerValue;
	private final double doubleValue;
	private final String stringValue;
	private final List<String> commentLines;

	/**
	 * Create a token without a value.
	 *
	 * @param position Position in the source, where this token has been found (not
	 *                 null).
	 * @param id       Token id, not null.
	 * @throws IllegalArgumentException If id is not {@link Id#LEFT_PAREN} or
	 *                                  {@link Id#RIGHT_PAREN} or {@link Id#QUOTE}.
	 */
	public Token(Position position, Id id) {
		if (position == null) {
			throw new NullPointerException("position");
		}
		if (!id.isPrimitive()) {
			throw new IllegalArgumentException("id = " + id);
		}
		this.position = position;
		this.id = id;
		doubleValue = 0;
		integerValue = 0;
		stringValue = null;
		commentLines = Collections.emptyList();
	}

	/**
	 * Create a double Token.
	 *
	 * @param position    Position in the source, where this token has been found
	 *                    (not null).
	 * @param doubleValue
	 */
	public Token(Position position, double doubleValue) {
		if (position == null) {
			throw new NullPointerException("position");
		}
		this.position = position;
		id = Id.DOUBLE;
		this.doubleValue = doubleValue;
		integerValue = 0;
		stringValue = null;
		commentLines = Collections.emptyList();
	}

	/**
	 * Create an integer Token.
	 *
	 * @param position     Position in the source, where this token has been found
	 *                     (not null).
	 * @param integerValue Value.
	 */
	public Token(Position position, long integerValue) {
		if (position == null) {
			throw new NullPointerException("position");
		}
		this.position = position;
		id = Id.INTEGER;
		this.integerValue = integerValue;
		doubleValue = 0;
		stringValue = null;
		commentLines = Collections.emptyList();
	}

	/**
	 * Create a String or Symbol token.
	 *
	 * @param position     Position in the source, where this token has been found
	 *                     (not null).
	 * @param id           Token id, not null.
	 * @param stringValue  String, not null.
	 * @param commentLines Comments preceding this symbol or string.
	 * @throws IllegalArgumentException If id is not {@link Id#SYMBOL} or
	 *                                  {@link Id#STRING}.
	 */
	public Token(Position position, Id id, String stringValue, List<String> commentLines) {
		if (position == null) {
			throw new NullPointerException("position");
		}
		if (id != Id.STRING && id != Id.SYMBOL) {
			throw new IllegalArgumentException("id = " + id);
		}
		if (stringValue == null) {
			throw new NullPointerException("stringValue");
		}
		this.position = position;
		this.id = id;
		doubleValue = 0;
		integerValue = 0;
		this.stringValue = stringValue;
		this.commentLines = Collections.unmodifiableList(commentLines);
	}

	/**
	 * @return Token id, never null.
	 */
	public Id getId() {
		return id;
	}

	/**
	 * Is this a Token with the given id?
	 *
	 * @param id Let's compare with this.
	 * @return Is it?
	 */
	public boolean is(Id id) {
		return id == this.id;
	}

	/**
	 * Is this a Token NOT with the given id?
	 *
	 * @param id Let's compare with this.
	 * @return Is it?
	 */
	public boolean isNot(Id id) {
		return id != this.id;
	}

	/**
	 * @return Double
	 * @throws IllegalStateException If id is not {@link Id#DOUBLE}.
	 */
	public double getDoubleValue() {
		if (id != Id.DOUBLE) {
			throw new IllegalStateException("id = " + id);
		}
		return doubleValue;
	}

	/**
	 * @return integer
	 * @throws IllegalStateException If id is not {@link Id#INTEGER}.
	 */
	public long getIntegerValue() {
		if (id != Id.INTEGER) {
			throw new IllegalStateException("id = " + id);
		}
		return integerValue;
	}

	/**
	 * @return Position in the source, where this token has been found (not null).
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @return String, not null.
	 * @throws IllegalStateException If id is not {@link Id#SYMBOL} or
	 *                               {@link Id#STRING}.
	 */
	public String getStringValue() {
		if (id != Id.STRING && id != Id.SYMBOL) {
			throw new IllegalStateException("id = " + id);
		}
		return stringValue;
	}

	public List<String> getCommentLines() {
		return Collections.unmodifiableList(commentLines);
	}

	@Override
	public String toString() {
		String s = null;
		switch (id) {
		case COLON:
			s = ":";
			break;
		case COMMA:
			s = ",";
			break;
		case DOUBLE:
			s = Double.toString(doubleValue);
			break;
		case INTEGER:
			s = Long.toString(integerValue);
			break;
		case LEFT_PAREN:
			s = "(";
			break;
		case LEFT_SQUARE_BRACKET:
			s = "[";
			break;
		case LEFT_CURLY_BRACKET:
			s = "{";
			break;
		case QUOTE:
			s = "'";
			break;
		case RIGHT_PAREN:
			s = ")";
			break;
		case RIGHT_SQUARE_BRACKET:
			s = "]";
			break;
		case RIGHT_CURLY_BRACKET:
			s = "}";
			break;
		case STRING:
			s = '"' + stringValue + '"';
			break;
		case SYMBOL:
			s = stringValue;
			break;
		case EOF:
			s = "end of file";
			break;
		}
		return s;
	}
}
