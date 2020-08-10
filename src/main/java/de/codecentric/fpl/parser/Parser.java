package de.codecentric.fpl.parser;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Token.Id;

/**
 * A simple Lisp parser. (It could nearly implementing {@link Iterator}, but
 * alas: Parse- and IOException...
 */
public class Parser implements Closeable {
	private static Set<String> keepCommentSymbols = new HashSet<>();
	static {
		keepCommentSymbols.add("def-function");
	}
	private Scanner scanner;
	private Token lastToken;
	private Token nextToken;
	private boolean haveFetchedFirstToken;

	/**
	 * @param scanner Scanner, not null.
	 * @throws ParseException On Syntax problems.
	 * @throws IOException    I/O problems.
	 */
	public Parser(Scanner scanner) throws IOException {
		if (scanner == null) {
			throw new NullPointerException("scanner");
		}
		this.scanner = scanner;
		// the parser would be simpler when we parse the first value here, but on the other
		// side (caller) code would be more complicate because we would throw ParseException here.
	}

	public boolean hasNext() throws ParseException, IOException {
		if (!haveFetchedFirstToken) {
			haveFetchedFirstToken = true;
			fetchNextToken();
		}
		return nextToken.isNot(Id.EOF);
	}

	/**
	 * @return The next object in the input, <code>null</code> for end of input.
	 * @throws ParseException On Syntax problems.
	 * @throws IOException    I/O problems.
	 */
	public FplValue next() throws ParseException, IOException {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return value();
	}

	private FplValue value() throws ParseException, IOException {
		FplValue result;
		switch (nextToken.getId()) {
		case LEFT_PAREN:
			result = list();
			break;
		case LEFT_CURLY_BRACKET:
			result = object();
			break;
		case DOUBLE:
			result = new FplDouble(nextToken.getDoubleValue());
			fetchNextToken();
			break;
		case INTEGER:
			result = FplInteger.valueOf(nextToken.getIntegerValue());
			fetchNextToken();
			break;
		case QUOTE:
			result = quote();
			break;
		case STRING:
			result = new FplString(nextToken.getStringValue());
			fetchNextToken();
			break;
		case SYMBOL:
			Symbol s;
			if ("nil".equals(nextToken.getStringValue())) {
				s = null;
				scanner.clearCommentLines();
			} else {
				s = new Symbol(nextToken.getStringValue(), nextToken.getPosition(), nextToken.getCommentLines());
				if (!keepCommentSymbols.contains(s.getName())) {
					scanner.clearCommentLines();
				}
			}
			fetchNextToken();
			result = s;
			break;
		default:
			fetchNextToken();
			throw new ParseException(nextToken.getPosition(), "unexpected token: " + lastToken);
		}
		return result;
	}

	private FplValue quote() throws ParseException, IOException {
		List<FplValue> elements = new ArrayList<FplValue>();
		elements.add(new Symbol("quote", nextToken.getPosition(), nextToken.getCommentLines()));
		fetchNextToken(); // skip '
		elements.add(value());
		return FplList.fromValues(elements);
	}

	private FplValue list() throws ParseException, IOException {
		fetchNextToken(); // skip LEFT_PAREN
		expectNotEof("Unexpected end of source in list");
		if (nextToken.is(Id.RIGHT_PAREN)) {
			fetchNextToken();
			return FplList.EMPTY_LIST;
		}
		List<FplValue> elements = new ArrayList<>();
		elements.add(value());
		while (nextToken.isNot(Id.EOF) && nextToken.isNot(Id.RIGHT_PAREN)) {
			elements.add(value());
		}
		expectNotEof("Unexpected end of source in list");
		fetchNextToken(); // skip RIGHT_PAREN
		return FplList.fromValues(elements);
	}

	/*
	 * An object is a sequence of key/value pairs.
	 * A pair is either a symbol or a string, followed by a colon, followed by a value.
	 * There is no comma between the pairs.
	 */
	private FplValue object() throws ParseException, IOException {
		FplObject obj = new FplObject("dict", nextToken.getPosition());
		fetchNextToken(); // skip LEFT_CURLY_BRACKET
		if (nextToken.isNot(Id.EOF) && nextToken.isNot(Id.RIGHT_CURLY_BRACKET)) {
			keyValuePair(obj);
		}
		keyValuePairs(obj);
		fetchNextToken(); // skip RIGHT_CURLY_BRACKET
		return obj;
	}

	private void keyValuePairs(FplObject obj) throws ParseException, IOException {
		while (nextToken.isNot(Id.RIGHT_CURLY_BRACKET)) {
			if (nextToken.is(Id.SYMBOL)) {
				keyValuePair(obj);
			} else {
				throw new ParseException(nextToken.getPosition(), "Symbol or } expected.");
			}
		}
		expectNotEof("Unexpected end of source in object");
	}

	/*
	 * string or symbol, comma, value
	 */
	private void keyValuePair(FplObject obj) throws ParseException, IOException {
		if (nextToken.isNot(Id.SYMBOL)) {
			throw new ParseException(lastToken.getPosition(), "Expect key (symbol), but got " + nextToken);
		}
		String key = nextToken.getStringValue();
		fetchNextToken(); // skip STRING or SYMBOL
		expectNotEof("Unexpected end of source in object");
		if (nextToken.isNot(Id.COLON)) {
			throw new ParseException(nextToken.getPosition(), "Expect : after key.");
		}
		fetchNextToken(); // skip COLON
		expectNotEof("Unexpected end of source in object");
		FplValue v = value();
		if (v == null) {
			throw new ParseException(lastToken.getPosition(), "Null no allowed as value.");
		}
		try {
			obj.define(key, v);
		} catch (ScopeException e) {
			throw new ParseException(lastToken.getPosition(), e.getMessage(), e);
		}
	}

	private void fetchNextToken() throws ParseException, IOException {
		lastToken = nextToken;
		nextToken = scanner.next();
	}

	private void expectNotEof(String message) throws ParseException, IOException {
		if (nextToken.is(Id.EOF)) {
			throw new ParseException(lastToken.getPosition(), message);
		}
	}

	@Override
	public void close() throws IOException {
		nextToken = new Token(nextToken.getPosition(), Token.Id.EOF);
		scanner.close();
	}
}
