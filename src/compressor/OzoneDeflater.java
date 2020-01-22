package compressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class OzoneDeflater implements Compressable {

	/*
	 * NOWRAP�� ���+Ǫ�� ���� ����. C#������ NOWRAP=True �����̹Ƿ� Java������ True�� �⺻������ ���
	 */
	static final boolean DEFAULT_NOWRAP = true; 
	static final int DEFAULT_BUFFER_SIZE = 1024;
	int bufferSize = DEFAULT_BUFFER_SIZE;
	Deflater deflater;
	Inflater inflater;

	/*
	 * Deflater ����
	 * @param bufferSize ���� ������. 0�� �ڹ� �⺻ �� ���
	 */
	public OzoneDeflater(int bufferSize) {
		this.bufferSize = bufferSize;
		this.deflater = new Deflater(Deflater.BEST_SPEED, DEFAULT_NOWRAP);
		this.inflater = new Inflater(DEFAULT_NOWRAP);
		
		// TODO: bufferSize ó��
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