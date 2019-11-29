package de.codecentric.fpl.parser;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.codecentric.fpl.parser.Token.Id;

/**
 * A simple scanner. The reader is closed implicitly when EOF is reached.
 */
public class Scanner implements Closeable {
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
	 * @param name Name of the source (filename), not null.
	 * @param line First line in the source, &gt;= 1.
	 * @param rd   Reader, not null.
	 * @throws IOException In case {@link Reader} throws.
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
	 * @param name Name of the source (filename), not null.
	 * @param rd   Reader, not null.
	 * @throws IOException In case {@link Reader} throws.
	 */
	public Scanner(String name, Reader rd) throws IOException {
		this(name, 1, rd);
	}

	/**
	 * @return Next token. {@link Id.EOF} at end of file
	 * @throws IOException    In case {@link Reader} throws.
	 * @throws ParseException Illegal token.
	 */
	public Token next() throws IOException, ParseException {
		skipComment();
		Position position = new Position(name, line, column);
		if (eof) {
			return new Token(position, Id.EOF);
		} else  if (ch == '(') {
			readChar();
			return new Token(position, Id.LEFT_PAREN);
		} else if (ch == ')') {
			readChar();
			return new Token(position, Id.RIGHT_PAREN);
		} else if (ch == '{') {
			readChar();
			return new Token(position, Id.LEFT_CURLY_BRACKET);
		} else if (ch == '}') {
			readChar();
			return new Token(position, Id.RIGHT_CURLY_BRACKET);
		} else if (ch == ':') {
			readChar();
			return new Token(position, Id.COLON);
		} else if (ch == '\'') {
			readChar();
			return new Token(position, Id.QUOTE);
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
		boolean negative = false;
		if (ch == '-') {
			readChar();
			negative = true;
		}
		long value = 0;
		while (Character.isDigit(ch)) {
			value = 10 * value + ch - '0';
			readChar();
		}
		if (ch == '.' || ch == 'e' || ch == 'E') {
			double dValue = value;
			if (ch == '.') {
				readChar();
				double base = 0.1;
				while (Character.isDigit(ch)) {
					dValue += base * (ch - '0');
					base /= 10;
					readChar();
				}
			}
			boolean negativeExponent = false;
			if (ch == 'e' || ch == 'E') {
				readChar();
				if (ch == '+') {
					readChar();
				} else if (ch == '-') {
					negativeExponent = true;
					readChar();
				}
				int expValue = 0;
				while (Character.isDigit(ch)) {
					expValue = 10 * expValue + ch - '0';
					readChar();
				}
				dValue *= Math.pow(10, negativeExponent ? -expValue : expValue);
			}
			return new Token(position, negative ? -dValue : dValue);
		} else {
			return new Token(position, negative ? -value : value);
		}
	}

	private Token symbol(Position position) throws IOException {
		final String NON_SYMBOL_CHARS = "\"()[]:";
		StringBuilder sb = new StringBuilder();
		while (ch != -1 && !Character.isWhitespace(ch) && NON_SYMBOL_CHARS.indexOf(ch) == -1) {
			sb.append((char) ch);
			readChar();
		}
		Token t = new Token(position, Id.SYMBOL, sb.toString(), commentLines);
		return t;
	}

	private Token string(Position position) throws IOException, ParseException {
		readChar(); // skip leading "
		StringBuilder sb = new StringBuilder();
		while (ch != '"') {
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
				} else if (ch == 'f') {
					sb.append('\f');
				} else if (ch == 'b') {
					sb.append('\b');
				} else if (ch == 'u') {
					sb.append(readHexadecimalCharacter(position));
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
		readChar(); // skip "
		return new Token(position, Id.STRING, sb.toString(), Collections.emptyList());
	}

	private char readHexadecimalCharacter(Position position) throws IOException, ParseException {
		char result = 0;
		for (int i = 0; i < 4; i++) {
			result <<= 4;
			readChar();
			if (ch == -1) {
				throw new ParseException(position, "Unterminated string at end of input");
			}
			char low = Character.toLowerCase((char) ch);
			result += hexDigit(low, position);

		}
		return result;
	}

	private char hexDigit(char low, Position position) throws ParseException {
		if (low >= '0' && low <= '9') {
			return (char) (low - '0');
		} else if (low >= 'a' && low <= 'f') {
			return (char) (low - 'a' + 10);
		}
		throw new ParseException(position, "Illegal hex digit: " + (char) ch);
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

	@Override
	public void close() throws IOException {
		eof = true;
		ch = -1;
		rd.close();
	}
}
