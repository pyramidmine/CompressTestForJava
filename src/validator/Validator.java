package validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import compressor.Compressable;
import compressor.OzoneDeflater;
import compressor.OzoneGZip;

public class Validator {
	// 검증할 압축 알고리즘 정보
	static class Item {
		String name;				// 이름; ex) deflater
		Compressable compressor;	// 압축기
		boolean enabled;			// 사용 여부

		public Item(String name, Compressable compressor, boolean enabled) {
			this.name = name;
			this.compressor = compressor;
			this.enabled = enabled;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Compressable getCompressor() {
			return this.compressor;
		}
		
		public boolean isEnabled() {
			return this.enabled;
		}
	}
	
	static final int BUFFER_SIZE = 1024;
	static List<Item> items = new ArrayList<Item>();
	
	public static void main(String[] args) {

		// 샘플 텍스트
		String sampleText = "To succeed in Life, you need two things: Ignorance and Confidence - Mark Twain.";
		
		// 압축기 및 검증 여부 리스트 작성
		items.add(new Item("deflater", new OzoneDeflater(BUFFER_SIZE), true));
		items.add(new Item("gzip", new OzoneGZip(BUFFER_SIZE), true));
		
		// 압축기를 순회하면서 검증
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i); 
			
			if (!item.isEnabled()) {
				continue;
			}
			
			// 압축기 정보
			System.out.println("---------- Validate ----------");
			System.out.println("Compressor: " + item.getName());

			// UTF-8 인코딩 & 압축
			String decodedText = null;
			byte[] encodedData = null;
			byte[] compressedData = null;
			byte[] decompressedData = null;
			try {
				encodedData = sampleText.getBytes(StandardCharsets.UTF_8);
				compressedData = item.getCompressor().compress(encodedData);
				decompressedData = item.getCompressor().decompress(compressedData);
				decodedText = new String(decompressedData, StandardCharsets.UTF_8);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// 원본 텍스트 비교
			boolean textEquality = sampleText.equals(decodedText);
			System.out.println("Compare Text: " + textEquality);

			// 인코딩 데이터 비교
			boolean dataEquality = Arrays.equals(encodedData, decompressedData);
			System.out.println("Compare Data: " + dataEquality);
			
			// 다른 언어와의 호환성을 체크하기 위해 압축된 데이터를 Base64 인코딩해서 파일에 저장
			// 다른 언어에서는 이 파일을 읽어서 Base64 디코딩 후 압축을 풀어서 잘 풀리는지 확인
			
			// Base64 인코딩
			byte[] base64EncodedData = Base64.getEncoder().encode(compressedData);
			String base64EncodedText = new String(base64EncodedData, StandardCharsets.UTF_8);
			
			// Base64 인코딩 데이터를 텍스트 파일로 저장
			File dir = new File(System.getProperty("user.dir"));
			String base64FileName = item.getName() + ".base64.java.txt";
			File base64File = new File(dir + File.separator + base64FileName);
			System.out.println("File Path: " + base64File);
			System.out.println("Encoded Base64 Text: " + base64EncodedText);
			try (FileWriter fw = new FileWriter(base64File)) {
				fw.write(base64EncodedText);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		
			// Base64 텍스트가 제대로 저장됐는지 확인
			String base64DecodedText = readBase64File(base64File);
			byte[] base64DecodedData = Base64.getDecoder().decode(base64DecodedText);
			System.out.println("Decoded Base64 Text: " + base64DecodedText);
			System.out.println("Compare Base64 Text: " + base64EncodedText.equals(base64DecodedText));
			System.out.println("Compare Base64 Data: " + Arrays.equals(compressedData, base64DecodedData));
			
			// 다른 언어로 작성된 파일을 읽어서 언어 사이에 호환이 잘 되는지 확인
			File otherFiles[] = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String pattern = item.getName() + "\\.base64\\.(cs|cpp)\\.txt";
					return name.matches(pattern);
				}
			});
			for (var otherFile : otherFiles) {
				String otherBase64DecodedText = readBase64File(otherFile);								// Base64 인코딩 된 텍스트	
				byte[] otherBase64DecodedData = Base64.getDecoder().decode(otherBase64DecodedText);		// 압축됐던 데이터
				byte[] otherDecompressedData = item.getCompressor().decompress(otherBase64DecodedData);	// 압축해제된 UTF-8 인코딩 데이터
				String otherDecodedText = new String(otherDecompressedData, StandardCharsets.UTF_8);	// UTF-8 디코딩 된 텍스트 = 원본 텍스트 
				System.out.println("File: " + otherFile.getName() + ", Compare Text: " + sampleText.equals(otherDecodedText));
			}
		}
	}
	
	static String readBase64File(File file) {
		String result = null;
		
		try (FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr)) {
			StringBuilder sb = new StringBuilder(BUFFER_SIZE);
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			result = sb.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return result;
	}
}