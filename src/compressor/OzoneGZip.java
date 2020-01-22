package compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterOutputStream;

import compressor.Compressable;;

public class OzoneGZip implements Compressable {

	static final int DEFAULT_BUFFER_SIZE = 1024;
	int bufferSize = DEFAULT_BUFFER_SIZE;

	/*
	 * GZIP æ–√‡
	 * @param bufferSize 
	 */
	public OzoneGZip(int bufferSize) {
		this.bufferSize = 0 < bufferSize ? bufferSize : this.bufferSize;
	}
	
	@Override
	public byte[] compress(byte[] data) {
		byte[] result = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			try (GZIPOutputStream gs = new GZIPOutputStream(os, true)) {
				gs.write(data, 0, data.length);
			}
			result = os.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public byte[] decompress(byte[] data) {
		byte[] result = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
				try (GZIPInputStream gs = new GZIPInputStream(is)) {
					byte[] buffer = new byte[this.bufferSize];
					int readBytes = 0;
					while (0 < (readBytes = gs.read(buffer))) {
						os.write(buffer, 0, readBytes);
					}
				}
			}
			result = os.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}
}