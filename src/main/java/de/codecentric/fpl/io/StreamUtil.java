package de.codecentric.fpl.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamUtil {

	public static byte[] readStreamToBytes(InputStream is) throws IOException {
		int totalSize = 0;
		List<Chunk> chunks = new ArrayList<>();
		boolean eof = false;
		do {
			Chunk chunk = new Chunk();
			chunk.size = is.read(chunk.buffer);
			if (chunk.size > 0) {
				chunks.add(chunk);
				totalSize += chunk.size;
			} else {
				eof = true;
			}
		} while (!eof);
		byte[] buffer = new byte[totalSize];
		int pos = 0;
		for (Chunk ch : chunks) {
			System.arraycopy(ch.buffer, 0, buffer, pos, ch.size);
			pos += ch.size;
		}
		is.close();
		return buffer;

	}

	private static class Chunk {
		private byte[] buffer = new byte[8 * 1024];
		private int size;
	}
}
