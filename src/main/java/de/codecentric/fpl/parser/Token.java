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
		LEFT_PAREN,
		/** ) */
		RIGHT_PAREN,
		/** [ */
		LEFT_SQUARE_BRACKET,
		/** ] */
		RIGHT_SQUARE_BRACKET,
		/** , */
		COMMA,
		/** 'x, short for (qoute x) */
		QUOTE,
		/** Integral number (stored as {@link BigInteger} */
		INTEGER,
		/** Double precision number */
		DOUBLE,
		/** Symbol */
		SYMBOL,
		/** String */
		STRING
	};

	private final Position position;
	private final Id id;
	private final long integerValue;
	private final double doubleValue;
	private final String stringValue;
	private final List<String> commentLines;

	/**
	 * Create a token without a value.
	 * 
	 * @param position
	 *            Position in the source, where this token has been found (not
	 *            null).
	 * @param id
	 *            Token id, not null.
	 * @throws IllegalArgumentException
	 *             If id is not {@link Id#LEFT_PAREN} or {@link Id#RIGHT_PAREN} or
	 *             {@link Id#QUOTE}.
	 */
	public Token(Position position, Id id) {
		if (position == null) {
			throw new NullPointerException("position");
		}
		if (id != Id.LEFT_PAREN && id != Id.RIGHT_PAREN && id != Id.QUOTE && id != Id.LEFT_SQUARE_BRACKET && id != Id.RIGHT_SQUARE_BRACKET) {
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
	 * @param position
	 *            Position in the source, where this token has been found (not
	 *            null).
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
	 * @param position
	 *            Position in the source, where this token has been found (not
	 *            null).
	 * @param integerValue
	 *            Value.
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
	 * @param position
	 *            Position in the source, where this token has been found (not
	 *            null).
	 * @param id
	 *            Token id, not null.
	 * @param stringValue
	 *            String, not null.
	 * @param commentLines
	 *            Comments preceding this symbol or string.
	 * @throws IllegalArgumentException
	 *             If id is not {@link Id#SYMBOL} or {@link Id#STRING}.
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
	 * @return Double
	 * @throws IllegalStateException
	 *             If id is not {@link Id#DOUBLE}.
	 */
	public double getDoubleValue() {
		if (id != Id.DOUBLE) {
			throw new IllegalStateException("id = " + id);
		}
		return doubleValue;
	}

	/**
	 * @return integer
	 * @throws IllegalStateException
	 *             If id is not {@link Id#INTEGER}.
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
	 * @throws IllegalStateException
	 *             If id is not {@link Id#SYMBOL} or {@link Id#STRING}.
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
		switch (id) {
		case COMMA:
			return ",";
		case DOUBLE:
			Double.toString(doubleValue);
		case INTEGER:
			Long.toString(integerValue);
		case LEFT_PAREN:
			return "(";
		case LEFT_SQUARE_BRACKET:
			return "[";
		case QUOTE:
			return "'";
		case RIGHT_PAREN:
			return ")";
		case RIGHT_SQUARE_BRACKET:
			return "]";
		case STRING:
			return '"' + stringValue + '"';
		case SYMBOL:
			return stringValue;
		default:
			throw new IllegalStateException();
		}
	}
}
