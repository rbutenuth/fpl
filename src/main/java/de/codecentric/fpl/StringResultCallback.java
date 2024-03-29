package de.codecentric.fpl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import de.codecentric.fpl.datatypes.FplValue;

/**
 * Collect evaluation results in a String.
 */
public class StringResultCallback implements ResultCallback {
	private boolean continueOnException;
	private StringBuilder builder;
	private CopyOutputStream out;

	public StringResultCallback(boolean continueOnException) {
		this.continueOnException = continueOnException;
		builder = new StringBuilder();
		out = new CopyOutputStream();
	}
	
	@Override
	public synchronized String toString() {
		out.flush();
		return builder.toString();
	}
	
	/**
	 * Delete the content of the accumulated String.
	 */
	public void clear() {
		builder.setLength(0);
	}
	
	@Override
	public synchronized boolean handleSuccess(FplValue result) {
		separate();
		builder.append(result == null ? "nil" : result.toString());
		newline();
		return true;
	}

	@Override
	public synchronized boolean handleException(Exception exception) {
		separate();
		builder.append(exception.getMessage());
		newline();
		if (exception instanceof EvaluationException) {
			builder.append(((EvaluationException) exception).stackTraceAsString());
		}
		return continueOnException;
	}

	public OutputStream getOutputStream() {
		return out;
	}
	
	private void separate() {
		if (builder.length() > 0) {
			newline();
		}
	}

	private void newline() {
		builder.append(System.lineSeparator());
	}
	
	private class CopyOutputStream extends OutputStream {
		private byte[] buffer;
		private int length;
		
		CopyOutputStream() {
			length = 0;
			buffer = new byte[10];
		}
		
		@Override
		public synchronized void write(int b) throws IOException {
			ensureCapacity(length + 1);
			buffer[length++] = (byte)b;
		}

		@Override
		public synchronized void write(byte[] b) throws IOException {
			ensureCapacity(length + b.length);
			System.arraycopy(b, 0, buffer, length, b.length);
			length += b.length;
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			ensureCapacity(length + len);
			System.arraycopy(b, off, buffer, length, len);
			length += len;
		}

		@Override
		public synchronized void flush() {
			builder.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
			length = 0;
		}

		@Override
		public void close() throws IOException {
			flush();
		}
		
		private void ensureCapacity(int capacity) {
			if (capacity > buffer.length) {
				int newCapacity = Math.max(capacity, buffer.length + buffer.length / 2);
				byte[] newBuffer = new byte[newCapacity];
				System.arraycopy(buffer, 0, newBuffer, 0, length);
				buffer = newBuffer;
			}
		}
	}
}
