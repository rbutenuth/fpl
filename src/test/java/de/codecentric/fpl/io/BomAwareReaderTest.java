package de.codecentric.fpl.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import de.codecentric.fpl.io.BomAwareReader;

public class BomAwareReaderTest {
	private static final int[] UTF_8 = new int[] {};
	private static final int[] UTF_8_WITH_HEADER = new int[] { 0xEF, 0xBB, 0xBF };
	private static final int[] UTF_16_LE = new int[] { 0xFF, 0xFE };
	private static final int[] UTF_16_BE = new int[] { 0xFE, 0xFF };

	private static final String HELLO = "Hello, world!";

	@Test
	public void withoutBom() throws IOException {
		withOutBom("");
		withOutBom("a");
		withOutBom("ab");
		withOutBom("abc");
		withOutBom("abcd");
	}
	
	@Test
	public void bomUTF8() throws IOException {
		withCharset(UTF_8, StandardCharsets.UTF_8);
	}

	@Test
	public void bomUTF8WithHeader() throws IOException {
		withCharset(UTF_8_WITH_HEADER, StandardCharsets.UTF_8);
	}

	@Test
	public void bomUTF16LE() throws IOException {
		withCharset(UTF_16_LE, StandardCharsets.UTF_16LE);
	}

	@Test
	public void bomUTF16BE() throws IOException {
		withCharset(UTF_16_BE, StandardCharsets.UTF_16BE);
	}

	private void withCharset(int[] header, Charset charset) throws IOException {
		byte[] hello = HELLO.getBytes(charset);
		byte[] input = new byte[header.length + hello.length];
		for (int i = 0; i < header.length; i++) {
			input[i] = (byte) (0xff & header[i]);
		}
		for (int i = header.length, j = 0; j < hello.length; i++, j++) {
			input[i] = hello[j];
		}
		try (InputStream is = new ByteArrayInputStream(input); BomAwareReader rd = new BomAwareReader(is)) {
			char[] buffer = new char[1024];
			int got = rd.read(buffer, 0, buffer.length);
			assertEquals(HELLO.length(), got);
			assertEquals(HELLO, new String(buffer, 0, got));
		}
	}
	private void withOutBom(String text) throws IOException {
		byte[] textAsBytes = text.getBytes("UTF-8");
		byte[] input = new byte[textAsBytes.length];
		for (int i = 0; i < textAsBytes.length; i++) {
			input[i] = textAsBytes[i];
		}
		try (InputStream is = new ByteArrayInputStream(input); BomAwareReader rd = new BomAwareReader(is)) {
			char[] buffer = new char[1024];
			int got = Math.max(rd.read(buffer, 0, buffer.length), 0);
			assertEquals(textAsBytes.length, got);
			assertEquals(text, new String(buffer, 0, got));
		}
	}
}
