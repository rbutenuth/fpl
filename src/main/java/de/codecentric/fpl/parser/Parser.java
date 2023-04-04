package de.codecentric.fpl.parser;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Token.Id;

/**
 * A simple Lisp parser.
 */
public class Parser implements Closeable, Iterator<FplValue> {
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
	 * @throws ParseException On Syntax or I/O problems.
	 */
	public Parser(Scanner scanner) throws ParseException {
		if (scanner == null) {
			throw new NullPointerException("scanner");
		}
		this.scanner = scanner;
		// the parser would be simpler when we parse the first value here, but on the other
		// side (caller) code would be more complicate because we would throw ParseException here.
	}

	public boolean hasNext() throws ParseException {
		if (!haveFetchedFirstToken) {
			haveFetchedFirstToken = true;
			fetchNextToken();
		}
		return nextToken.isNot(Id.EOF);
	}

	/**
	 * @return The next object in the input.
	 * @throws NoSuchElementException On end of input
	 * @throws ParseException On Syntax or I/O problems.
	 */
	public FplValue next() throws ParseException {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return value();
	}

	private FplValue value() throws ParseException {
		FplValue result;
		switch (nextToken.getId()) {
		case LEFT_PAREN:
			result = list();
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
				scanner.clearComment();
			} else {
				s = new Symbol(nextToken.getStringValue(), nextToken.getPosition(), nextToken.getComment());
				if (!keepCommentSymbols.contains(s.getName())) {
					scanner.clearComment();
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

	private FplValue quote() throws ParseException {
		List<FplValue> elements = new ArrayList<FplValue>();
		elements.add(new Symbol("quote", nextToken.getPosition(), nextToken.getComment()));
		fetchNextToken(); // skip '
		elements.add(value());
		return FplList.fromValues(elements);
	}

	private FplValue list() throws ParseException {
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

	private void fetchNextToken() throws ParseException {
		lastToken = nextToken;
		nextToken = scanner.next();
	}

	private void expectNotEof(String message) throws ParseException {
		if (nextToken.is(Id.EOF)) {
			throw new ParseException(lastToken.getPosition(), message);
		}
	}

	@Override
	public void close() throws ParseException {
		nextToken = new Token(nextToken.getPosition(), Token.Id.EOF);
		scanner.close();
	}
}
