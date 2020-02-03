package main;

import compressor.Compressable;
import compressor.OzoneDeflater;
import compressor.OzoneGZip;
import compressor.OzoneLZ4;
import compressor.OzoneLZ4Frame;
import sampledata.SampleData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class main {
	
	enum StatColumn {
		TrialCount,
		EncodedSize,
		CompressedSize,
		Encoding,
		Compression,
		Decompression,
		Decoding,
		MAX_VALUE
	}
	
	static final long TRIAL_FACTOR = 1;
	static final long TIME_RESOLUTION = 1000000;	// nano second to milliseconds
	static final int DEFLATE_BUFFER_SIZE = 8192;
	static final int GZIP_BUFFER_SIZE = 8192;
	
	public static void main(String[] args) {
		
		System.out.println("Start JAVA performance test...");
		
		// 샘플데이터
		SampleData sampleData = new SampleData();
		
		// 압축기 리스트
		List<Compressable> compressors = Arrays.asList(
			new OzoneDeflater(DEFLATE_BUFFER_SIZE),
			new OzoneGZip(GZIP_BUFFER_SIZE),
			new OzoneLZ4Frame()
		);
		
		for (var compressor: compressors) {
			// 통계
			long[][] stat = new long[sampleData.Texts.size()][StatColumn.MAX_VALUE.ordinal()];

			int maxDataSize = 0;
			for (var s : sampleData.Texts) {
				if (maxDataSize < s.length()) {
					maxDataSize = s.length();
				}
			}
			long timeBegin, timeEnd;
			
			for (int i = 0; i < sampleData.Texts.size(); i++) {
				System.out.println("Sample data index: " + i);
				
				// 수행횟수 계산
				double basicCount = (double)maxDataSize / sampleData.Texts.get(i).length();
				double scaledCount = Math.sqrt(basicCount);
				double scaledFactor = Math.max(1.0d, Math.log(basicCount));
				
				stat[i][StatColumn.TrialCount.ordinal()] = (long)(scaledCount * scaledFactor * scaledFactor * TRIAL_FACTOR);
				boolean equality = false;
				
				for (int j = 0; j < stat[i][StatColumn.TrialCount.ordinal()]; j++) {
					// 인코딩
					timeBegin = System.nanoTime();
					byte[] encodedData = sampleData.Texts.get(i).getBytes(StandardCharsets.UTF_8);
					timeEnd = System.nanoTime();
					stat[i][StatColumn.Encoding.ordinal()] += (timeEnd - timeBegin);
					stat[i][StatColumn.EncodedSize.ordinal()] += encodedData.length;
					
					// 압축
					timeBegin = System.nanoTime();
					byte[] compressedData = compressor.compress(encodedData);
					timeEnd = System.nanoTime();
					stat[i][StatColumn.Compression.ordinal()] += (timeEnd - timeBegin);
					stat[i][StatColumn.CompressedSize.ordinal()] += compressedData.length;
					
					// 압축해제
					timeBegin = System.nanoTime();
					byte[] decompressedData = compressor.decompress(compressedData);
					timeEnd = System.nanoTime();
					stat[i][StatColumn.Decompression.ordinal()] += (timeEnd - timeBegin);
					
					// 디코딩
					timeBegin = System.nanoTime();
					String decodedText = new String(decompressedData, StandardCharsets.UTF_8);
					timeEnd = System.nanoTime();
					stat[i][StatColumn.Decoding.ordinal()] += (timeEnd - timeBegin);
					
					// 전/후 데이터 비교 (한 번이라도 실패하면 중지)
					if (!equality) {
						// 원본 데이터 비교
						System.out.println("Original text length : " + sampleData.Texts.get(i).length());
						System.out.println("Processed text length: " + decodedText.length());
						equality = sampleData.Texts.get(i).equals(decodedText);
						if (!equality) {
							System.out.println("Two texts are mismatch!");
							return;
						}
						
						// 인코딩 데이터 비교
						System.out.println("Encoded data length  : " + encodedData.length);
						System.out.println("Processed data length: " + decompressedData.length);
						equality = Arrays.equals(encodedData, decompressedData);
						if (!equality) {
							System.out.println("Two are are mismatch!");
							return;
						}
					}
				}
			}
			
			// 스탯 출력
			System.out.println("---------- " + compressor.toString() + " Statistics ----------");
			System.out.println("Original Length, Encoded Size, Compressed Size, Compression Ratio, Trial Count, Encoding Time, Compression Time, Decompression Time, Decoding Time");
			for (int i = 0; i < sampleData.Texts.size(); i++) {
				System.out.println(
					String.format("%d, ", sampleData.Texts.get(i).length()) +
					String.format("%d, ", stat[i][StatColumn.EncodedSize.ordinal()] / stat[i][StatColumn.TrialCount.ordinal()]) +
					String.format("%d, ", stat[i][StatColumn.CompressedSize.ordinal()] / stat[i][StatColumn.TrialCount.ordinal()]) +
					", " +
					String.format("%d, ", stat[i][StatColumn.TrialCount.ordinal()]) +
					String.format("%.4f, ", (double)stat[i][StatColumn.Encoding.ordinal()] / stat[i][StatColumn.TrialCount.ordinal()] / TIME_RESOLUTION) +
					String.format("%.4f, ", (double)stat[i][StatColumn.Compression.ordinal()] / stat[i][StatColumn.TrialCount.ordinal()] / TIME_RESOLUTION) +
					String.format("%.4f, ", (double)stat[i][StatColumn.Decompression.ordinal()] / stat[i][StatColumn.TrialCount.ordinal()] / TIME_RESOLUTION) +
					String.format("%.4f, ", (double)stat[i][StatColumn.Decoding.ordinal()] / stat[i][StatColumn.TrialCount.ordinal()] / TIME_RESOLUTION));
			}
		}
	}
}
