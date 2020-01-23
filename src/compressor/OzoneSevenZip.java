package compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import SevenZip.Compression.LZMA.Encoder;

public class OzoneSevenZip implements Compressable {
	
	static final int DEFAULT_ALGORITHM = 2;
	static final int DEFAULT_DICTIONARY_SIZE = (1 << 21);
	static final int DEFAULT_FAST_BYTES = 128;
	static final int DEFAULT_MATCH_FINDER = 1;
	static final int Lc = 3, Lp = 0, Pb = 2;
	static final boolean EOS = false;
	
	static final int PROPERTIES_SIZE = 5;

	@Override
	public byte[] compress(byte[] data) {
		byte[] result = null;
		try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream(data.length)) {
				SevenZip.Compression.LZMA.Encoder encoder = new SevenZip.Compression.LZMA.Encoder();
				if (!encoder.SetAlgorithm(DEFAULT_ALGORITHM)) {
					throw new Exception("Incorrect compression mode");
				}
				if (!encoder.SetDictionarySize(DEFAULT_DICTIONARY_SIZE)) {
					throw new Exception("Incorrect dictionary size");
				}
				if (!encoder.SetNumFastBytes(DEFAULT_FAST_BYTES)) {
					throw new Exception("Incorrect -fb value");
				}
				if (!encoder.SetMatchFinder(DEFAULT_MATCH_FINDER)) {
					throw new Exception("Incorrect -mf value");
				}
				if (!encoder.SetLcLpPb(Lc, Lp, Pb)) {
					throw new Exception("Incorrect -lc or -lp or -pb value");
				}
				encoder.SetEndMarkerMode(EOS);
				encoder.WriteCoderProperties(os);
				long length = EOS ? -1 : data.length;
				for (int i = 0; i < 8; i++) {
					os.write((int)(length >>> (8 * i)) & 0xFF);
				}
				encoder.Code(is, os, -1, -1, null);
				os.flush();
				result = os.toByteArray();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public byte[] decompress(byte[] data) {
		byte[] result = null;
		try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				byte[] properties = new byte[PROPERTIES_SIZE];
				if (is.read(properties, 0, PROPERTIES_SIZE) != PROPERTIES_SIZE) {
					throw new Exception("Input data is too short");
				}
				SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
				if (!decoder.SetDecoderProperties(properties)) {
					throw new Exception("Incorrect stream properties");
				}
				long length = 0;
				for (int i = 0; i < 8; i++) {
					int value = is.read();
					if (value < 0) {
						throw new Exception("Can't read stream size");
					}
					length |= ((long)value) << (8 * i);
				}
				if (!decoder.Code(is, os, length)) {
					throw new Exception("Error in data stream");
				}
				os.flush();
				result = os.toByteArray();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

}
