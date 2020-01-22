package compressor;

public interface Compressable {
	byte[] compress(byte[] data);
	byte[] decompress(byte[] data);
}
