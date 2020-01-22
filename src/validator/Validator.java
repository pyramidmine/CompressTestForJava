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
	// ������ ���� �˰��� ����
	static class Item {
		String name;				// �̸�; ex) deflater
		Compressable compressor;	// �����
		boolean enabled;			// ��� ����

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

		// ���� �ؽ�Ʈ
		String sampleText = "To succeed in Life, you need two things: Ignorance and Confidence - Mark Twain.";
		
		// ����� �� ���� ���� ����Ʈ �ۼ�
		items.add(new Item("deflater", new OzoneDeflater(BUFFER_SIZE), true));
		items.add(new Item("gzip", new OzoneGZip(BUFFER_SIZE), true));
		
		// ����⸦ ��ȸ�ϸ鼭 ����
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i); 
			
			if (!item.isEnabled()) {
				continue;
			}
			
			// ����� ����
			System.out.println("---------- Validate ----------");
			System.out.println("Compressor: " + item.getName());

			// UTF-8 ���ڵ� & ����
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
			
			// ���� �ؽ�Ʈ ��
			boolean textEquality = sampleText.equals(decodedText);
			System.out.println("Compare Text: " + textEquality);

			// ���ڵ� ������ ��
			boolean dataEquality = Arrays.equals(encodedData, decompressedData);
			System.out.println("Compare Data: " + dataEquality);
			
			// �ٸ� ������ ȣȯ���� üũ�ϱ� ���� ����� �����͸� Base64 ���ڵ��ؼ� ���Ͽ� ����
			// �ٸ� ������ �� ������ �о Base64 ���ڵ� �� ������ Ǯ� �� Ǯ������ Ȯ��
			
			// Base64 ���ڵ�
			byte[] base64EncodedData = Base64.getEncoder().encode(compressedData);
			String base64EncodedText = new String(base64EncodedData, StandardCharsets.UTF_8);
			
			// Base64 ���ڵ� �����͸� �ؽ�Ʈ ���Ϸ� ����
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
		
			// Base64 �ؽ�Ʈ�� ����� ����ƴ��� Ȯ��
			String base64DecodedText = readBase64File(base64File);
			byte[] base64DecodedData = Base64.getDecoder().decode(base64DecodedText);
			System.out.println("Decoded Base64 Text: " + base64DecodedText);
			System.out.println("Compare Base64 Text: " + base64EncodedText.equals(base64DecodedText));
			System.out.println("Compare Base64 Data: " + Arrays.equals(compressedData, base64DecodedData));
			
			// �ٸ� ���� �ۼ��� ������ �о ��� ���̿� ȣȯ�� �� �Ǵ��� Ȯ��
			File otherFiles[] = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String pattern = item.getName() + "\\.base64\\.(cs|cpp)\\.txt";
					return name.matches(pattern);
				}
			});
			for (var otherFile : otherFiles) {
				String otherBase64DecodedText = readBase64File(otherFile);								// Base64 ���ڵ� �� �ؽ�Ʈ	
				byte[] otherBase64DecodedData = Base64.getDecoder().decode(otherBase64DecodedText);		// ����ƴ� ������
				byte[] otherDecompressedData = item.getCompressor().decompress(otherBase64DecodedData);	// ���������� UTF-8 ���ڵ� ������
				String otherDecodedText = new String(otherDecompressedData, StandardCharsets.UTF_8);	// UTF-8 ���ڵ� �� �ؽ�Ʈ = ���� �ؽ�Ʈ 
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