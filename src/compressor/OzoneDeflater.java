package compressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class OzoneDeflater implements Compressable {

	/*
	 * NOWRAP은 헤더+푸터 여부 결정. C#에서는 NOWRAP=True 설정이므로 Java에서도 True를 기본값으로 사용
	 */
	static final boolean DEFAULT_NOWRAP = true; 
	static final int DEFAULT_BUFFER_SIZE = 1024;
	int bufferSize = DEFAULT_BUFFER_SIZE;
	Deflater deflater;
	Inflater inflater;

	/*
	 * Deflater 압축
	 * @param bufferSize 버퍼 사이즈. 0은 자바 기본 값 사용
	 */
	public OzoneDeflater(int bufferSize) {
		this.bufferSize = bufferSize;
		this.deflater = new Deflater(Deflater.BEST_SPEED, DEFAULT_NOWRAP);
		this.inflater = new Inflater(DEFAULT_NOWRAP);
		
		// TODO: bufferSize 처리
	}

	@Override
	public byte[] compress(byte[] data){
		this.deflater.reset();
		byte[] result = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			try (DeflaterOutputStream ds = new DeflaterOutputStream(os, this.deflater, true)) {
				ds.write(data, 0, data.length);
			}
			result = os.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public byte[] decompress(byte[] data) {
		this.inflater.reset();
		byte[] result = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			try (InflaterOutputStream is = new InflaterOutputStream(os, this.inflater)) {
				is.write(data, 0, data.length);
			}
			result = os.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}
}