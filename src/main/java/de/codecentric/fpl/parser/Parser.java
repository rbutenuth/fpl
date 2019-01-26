package de.codecentric.fpl.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * A simple Lisp parser. (It could nearly implementing {@link Iterator}, but
 * alas: Parse- and IOException...
 */
public class Parser {
	private static Set<String> keepCommentSymbols = new HashSet<>();
	static {
		keepCommentSymbols.add("defun");
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
		// the parser would be simpler when we parse the first value here, but on the
		// other
		// side (caller) code would be more complicate because we would throw
		// ParseException here.
	}

	public boolean hasNext() throws ParseException, IOException {
		if (!haveFetchedFirstToken) {
			haveFetchedFirstToken = true;
			fetchNextToken();
		}
		return nextToken != null;
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
		case LEFT_SQUARE_BRACKET:
			result = jsonList();
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
			String name = nextToken.getStringValue();
			Symbol s;
			if ("nil".equals(name)) {
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
			throw new ParseException(nextToken.getPosition(), "unexptected token: " + nextToken);
		}
		return result;
	}

	private FplValue quote() throws ParseException, IOException {
		List<FplValue> elements = new ArrayList<FplValue>();
		elements.add(new Symbol("quote", nextToken.getPosition(), nextToken.getCommentLines()));
		fetchNextToken(); // skip '
		elements.add(value());
		return new FplList(elements);
	}

	private FplValue list() throws ParseException, IOException {
		fetchNextToken(); // skip LEFT_PAREN
		expectNotEof("Unexpected end of source in list");
		if (nextToken.getId() == Token.Id.RIGHT_PAREN) {
			fetchNextToken();
			return FplList.EMPTY_LIST;
		}
		List<FplValue> elements = new ArrayList<>();
		elements.add(value());
		while (nextToken != null && nextToken.getId() != Token.Id.RIGHT_PAREN) {
			elements.add(value());
		}
		expectNotEof("Unexpected end of source in list");
		fetchNextToken(); // skip RIGHT_PAREN
		return new FplList(elements);
	}

	private FplValue jsonList() throws ParseException, IOException {
		fetchNextToken(); // skip LEFT_SQUARE_BRACKET
		expectNotEof("Unexpected end of source in json list");
		if (nextToken.getId() == Token.Id.RIGHT_SQUARE_BRACKET) {
			fetchNextToken();
			return FplList.EMPTY_LIST;
		}
		List<FplValue> elements = new ArrayList<>();
		elements.add(value());
		while (nextToken != null && nextToken.getId() == Token.Id.COMMA) {
			fetchNextToken(); // skip COMMA
			elements.add(value());
		}
		expectNotEof("Unexpected end of source in json list");
		if (nextToken.getId() != Token.Id.RIGHT_SQUARE_BRACKET) {
			throw new ParseException(nextToken.getPosition(), "Unexpected token in json list: " + nextToken);
		}
		fetchNextToken(); // skip RIGHT_SQUARE_BRACKET
		return new FplList(elements);
	}

	/*
	 * An object is a sequence of elements, separated by commas. An element is
	 * either a pair or a value (value can't be symbol, symbol is start of a pair) A
	 * pair is either a symbol or a string, followed by a colon, followed by a value
	 */
	private FplValue object() throws ParseException, IOException {
		FplObject obj = new FplObject(nextToken.getPosition());
		fetchNextToken(); // skip LEFT_CURLY_BRACKET
		initCode(obj);
		keyValuePairs(obj);
		fetchNextToken(); // skip RIGHT_CURLY_BRACKET
		return obj;
	}

	private void initCode(FplObject obj) throws ParseException, IOException {
		while (nextToken != null && nextToken.getId() == Token.Id.LEFT_PAREN) {
			FplValue v = value();
			if (v != null) {
				obj.addValue(v);
			}
		}
	}

	private void keyValuePairs(FplObject obj) throws ParseException, IOException {
		while (nextToken != null && nextToken.getId() != Token.Id.RIGHT_CURLY_BRACKET) {
			if (nextToken.getId() == Token.Id.SYMBOL || nextToken.getId() == Token.Id.STRING) {
				keyValuePair(obj);
				if (nextToken.getId() == Token.Id.COMMA) {
					fetchNextToken(); // skip COMMA
				}
			} else {
				throw new ParseException(nextToken.getPosition(), "Symbol, String or } expected.");
			}
		}
		expectNotEof("Unexpected end of source in object");
	}

	/*
	 * string or symbol, comma, value
	 */
	private void keyValuePair(FplObject obj) throws ParseException, IOException {
		String key = nextToken.getStringValue();
		if (key.length() == 0) {
			throw new ParseException(lastToken.getPosition(), "Key in map must have length > 0");
		}
		fetchNextToken(); // skip STRING or SYMBOL
		expectNotEof("Unexpected end of source in object");
		if (nextToken.getId() != Token.Id.COLON) {
			throw new ParseException(nextToken.getPosition(), "Expect : after key");
		}
		fetchNextToken(); // skip COLON
		FplValue v = value();
		if (v == null) {
			throw new ParseException(lastToken.getPosition(), "Null no allowed as value.");
		}
		obj.putUnsafe(key, v);
	}

	private void fetchNextToken() throws ParseException, IOException {
		lastToken = nextToken;
		nextToken = scanner.next();
	}

	private void expectNotEof(String message) throws ParseException, IOException {
		if (nextToken == null) {
			throw new ParseException(lastToken.getPosition(), message);
		}
	}
}
