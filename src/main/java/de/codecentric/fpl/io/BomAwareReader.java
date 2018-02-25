package de.codecentric.fpl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BomAwareReader extends Reader {
	private static final int[] UTF_8_HEADER = new int[] { 0xEF, 0xBB, 0xBF };
	private static final int[] UTF_16_LE_HEADER = new int[] { 0xFF, 0xFE };
	private static final int[] UTF_16_BE_HEADER = new int[] { 0xFE, 0xFF };

	private Reader reader;

	public BomAwareReader(InputStream is) throws IOException {
		byte[] header = new byte[3];
		is.read(header);
		int headerLength;
		Charset charset;
		if (matches(header, UTF_8_HEADER)) {
			charset = StandardCharsets.UTF_8;
			headerLength = 3;
		} else if (matches(header, UTF_16_LE_HEADER)) {
			charset = StandardCharsets.UTF_16LE;
			headerLength = 2;
		} else if (matches(header, UTF_16_BE_HEADER)) {
			charset = StandardCharsets.UTF_16BE;
			headerLength = 2;
		} else {
			// let's assume UTF-8 without BOM
			charset = StandardCharsets.UTF_8;
			headerLength = 0;
		}
		// We must have got at least "headerLength" bytes, otherwise we wouldn't have a match.
		// So it's not necessary to check the return value of read().

		InputStream isWithoutBom = new InputStreamWithPrefix(header, headerLength, 3 - headerLength, is);
		reader = new InputStreamReader(isWithoutBom, charset);
	}

	private boolean matches(byte[] header, int[] base) {
		for (int i = 0; i < base.length; i++) {
			if ((0xff & header[i]) != base[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int read(char[] buf, int off, int len) throws IOException {
		return reader.read(buf, off, len);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	private static class InputStreamWithPrefix extends InputStream {
		private byte[] prefix;
		private int pos;
		private int count;
		private InputStream is;

		public InputStreamWithPrefix(byte[] prefix, int pos, int count, InputStream is) {
			this.prefix = prefix;
			this.pos = pos;
			this.count = count;
			this.is = is;
		}

		@Override
		public int read() throws IOException {
			if (count > 0) {
				count--;
				return 0xff & prefix[pos++];
			} else {
				return is.read();
			}
		}

		@Override
		public void close() throws IOException {
			count = 0;
			is.close();
		}
	}
}
