package compressor;

import java.io.IOException;

import org.xerial.snappy.Snappy;

public class OzoneSnappy implements Compressable {

	@Override
	public byte[] compress(byte[] data) {
		byte[] result = null;
		try {
			result = Snappy.compress(data);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public byte[] decompress(byte[] data) {
		byte[] result = null;
		try {
			result = Snappy.uncompress(data);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

}
