package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;

public class PrintTest extends AbstractFplTest {
	private ByteArrayOutputStream stream;
	private PrintStream printStream;
	
	@BeforeEach
	public void before() throws Exception {
		stream = new ByteArrayOutputStream();
		printStream = new PrintStream(stream, true, "UTF-8");
		engine.setSystemOut(printStream);
	}
	
	@AfterEach
	public void after() throws Exception {
		engine.setSystemOut(System.out);
	}
	
	@Test
	public void print() throws Exception {
		evaluate("print", "(print 42 43)");
		printStream.flush();
		assertEquals("42 43", new String(stream.toByteArray(), "UTF-8"));
	}
	
	@Test
	public void printLine() throws Exception {
		evaluate("print", "(println 42 43)");
		assertEquals("42 43" + System.lineSeparator(), new String(stream.toByteArray(), "UTF-8"));
	}
}
