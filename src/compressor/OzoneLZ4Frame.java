package compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

public class OzoneLZ4Frame implements Compressable {
	
	static final int DEFAULT_BUFFER_SIZE = 1024;
	int bufferSize = DEFAULT_BUFFER_SIZE;

	@Override
	public byte[] compress(byte[] data) {
		byte[] result = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			try (LZ4FrameOutputStream zs = new LZ4FrameOutputStream(os)) {
				zs.write(data);
			}
			os.flush();
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
				try (LZ4FrameInputStream zs = new LZ4FrameInputStream(is)) {
					byte[] buffer = new byte[this.bufferSize];
					int readBytes = 0;
					while (0 < (readBytes = zs.read(buffer))) {
						os.write(buffer, 0, readBytes);
					}
				}
			}
			os.flush();
			result = os.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

}
