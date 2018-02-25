package de.codecentric.fpl.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple scanner. The reader is closed when EOF is reached.
 */
public class Scanner {
	private final Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");
	private Reader rd;
	private String name;
	private int line;
	private int column;
	private boolean eof;
	private int ch = -1;
	private int nextCh = -2;
	private List<String> commentLines;

	/**
	 * Create scanner with default start line 1.
	 * 
	 * @param name
	 *            Name of the source (filename), not null.
	 * @param line
	 *            First line in the source, &gt;= 1.
	 * @param rd
	 *            Reader, not null.
	 * @throws IOException
	 *             In case {@link Reader} throws.
	 */
	public Scanner(String name, int line, Reader rd) throws IOException {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (line < 1) {
			throw new IllegalArgumentException("line must be >= 1, but is: " + line);
		}
		this.name = name;
		this.rd = rd;
		this.line = line;
		column = 0;
		readChar();
		commentLines = new ArrayList<>();
	}

	/**
	 * Create scanner with default start line 1.
	 * 
	 * @param name
	 *            Name of the source (filename), not null.
	 * @param rd
	 *            Reader, not null.
	 * @throws IOException
	 *             In case {@link Reader} throws.
	 */
	public Scanner(String name, Reader rd) throws IOException {
		this(name, 1, rd);
	}

	/**
	 * @return Next token or null for end of input.
	 * @throws IOException
	 *             In case {@link Reader} throws.
	 * @throws ParseException
	 *             Illegal token.
	 */
	public Token next() throws IOException, ParseException {
		skipComment();
		if (eof) {
			return null;
		}
		Position position = new Position(name, line, column);
		if (ch == '(') {
			readChar();
			return new Token(position, Token.Id.LEFT_PAREN);
		} else if (ch == ')') {
			readChar();
			return new Token(position, Token.Id.RIGHT_PAREN);
		} else if (ch == '\'') {
			readChar();
			return new Token(position, Token.Id.QUOTE);
		} else if (ch == '-' && !Character.isWhitespace(nextCh) || ch >= '0' && ch <= '9') {
			return number(position);
		} else if (ch == '"') {
			return string(position);
		} else {
			return symbol(position);
		}
	}

	public void clearCommentLines() {
		commentLines = new ArrayList<>();
	}
	
	private void skipComment() throws IOException {
		while (ch == ';' || Character.isWhitespace(ch)) {
			if (ch == ';') {
				StringBuilder commentLine = new StringBuilder();
				readChar(); // skip ';'
				while (ch != '\n' && ch != '\r' && ch != -1) {
					commentLine.append((char) ch);
					readChar();
				}
				if (Character.isWhitespace(commentLine.charAt(0))) {
					commentLine.deleteCharAt(0);
				}
				commentLines.add(commentLine.toString());
			} else {
				while (Character.isWhitespace(ch)) {
					readChar();
				}
			}
		}
	}

	private Token number(Position position) throws IOException, ParseException {
		StringBuilder sb = new StringBuilder();
		if (ch == '-') {
			sb.append('-');
			readChar();
		}
		while (ch != -1 && ch != '(' && ch != ')' && ch != '\'' && !Character.isWhitespace(ch)) {
			sb.append((char) ch);
			readChar();
		}
		String str = sb.toString();
		Matcher m = INT_PATTERN.matcher(str);
		if (m.matches()) {
			try {
				return new Token(position, Long.parseLong(str));
			} catch (NumberFormatException e) {
				// should never happen
			}
		}

		try {
			return new Token(position, Double.valueOf(str));
		} catch (NumberFormatException e) {
			throw new ParseException(position, "Bad number: " + str);
		}
	}

	private Token symbol(Position position) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (ch != '(' && ch != ')' && ch != '\'' && !Character.isWhitespace(ch) && ch != -1) {
			sb.append((char) ch);
			readChar();
		}
		Token t = new Token(position, Token.Id.SYMBOL, sb.toString(), commentLines);
		return t;
	}

	private Token string(Position position) throws IOException, ParseException {
		readChar(); // skip leading "
		StringBuilder sb = new StringBuilder();
		while (ch != '"' && ch != -1) {
			if (ch == '\\') {
				readChar();
				if (ch == -1) {
					throw new ParseException(position, "Unterminated \\ at end of input");
				}
				if (ch == '"') {
					sb.append('"');
				} else if (ch == 'n') {
					sb.append('\n');
				} else if (ch == 'r') {
					sb.append('\r');
				} else if (ch == 't') {
					sb.append('\t');
				} else {
					sb.append((char) ch);
				}
			} else {
				sb.append((char) ch);
			}
			readChar();
			if (ch == -1) {
				throw new ParseException(position, "Unterminated string at end of input");
			}
		}
		if (ch == '"') {
			readChar();
		}
		return new Token(position, Token.Id.STRING, sb.toString(), Collections.emptyList());
	}

	private void readChar() throws IOException {
		if (nextCh == -2) {
			ch = rd.read();
			nextCh = rd.read();
		} else {
			ch = nextCh;
			nextCh = rd.read();
		}
		if (ch == -1) {
			eof = true;
			rd.close();
		}
		if (ch == '\n') {
			line++;
			column = 0;
		} else if (ch == '\r') {
			column = 0;
		} else {
			column++;
		}
	}
}
