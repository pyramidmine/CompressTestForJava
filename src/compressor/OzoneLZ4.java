package compressor;

import java.util.Arrays;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

public class OzoneLZ4 implements Compressable {
	
	final LZ4Factory factory = LZ4Factory.fastestInstance();
	final LZ4Compressor compressor;
	final LZ4SafeDecompressor decompressor; 
	
	public OzoneLZ4(boolean useInstantCompressor) {
		this.compressor = useInstantCompressor ? null : this.factory.fastCompressor();
		this.decompressor = useInstantCompressor ? null : this.factory.safeDecompressor();
	}
	
	@Override
	public byte[] compress(byte[] data) {
		LZ4Compressor compressor = getCompressor();
		int maxCompressedLength = compressor.maxCompressedLength(data.length);
		byte[] compressedData = new byte[maxCompressedLength];
		int compressedLength = compressor.compress(data, 0, data.length, compressedData, 0, compressedData.length);
		byte[] result = Arrays.copyOf(compressedData, compressedLength);
		return result;
	}

	@Override
	public byte[] decompress(byte[] data) {
		LZ4SafeDecompressor decompressor = getDecompressor();
		int decompressedLength = getDecompressedLength(data);
		return decompressor.decompress(data, 0, data.length, decompressedLength);
	}
	
	LZ4Compressor getCompressor() {
		return (this.compressor != null) ? this.compressor : this.factory.fastCompressor(); 
	}
	
	LZ4SafeDecompressor getDecompressor() {
		return (this.decompressor != null) ? this.decompressor : this.factory.safeDecompressor();
	}
	
	int getDecompressedLength(byte[] data) {
		return data.length;
	}
}
