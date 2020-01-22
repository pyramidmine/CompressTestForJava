package sampledata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SampleData {
	public List<String> Texts = new ArrayList<String>();
	
	public SampleData()
	{
		String currentDirectory = System.getProperty("user.dir");
		System.out.println("SampleData.SampleData(), Current Directory: " + currentDirectory);
		String sampleDirectory = currentDirectory + "\\samples";
		System.out.println("SampleData.SampleData(), Sample Directory: " + sampleDirectory);
		
		try {
			for (File file : new File(sampleDirectory).listFiles((dir, name) -> name.matches(".*\\.txt"))) {
				System.out.println(file.getName());
				
				FileReader fr = new FileReader(file, StandardCharsets.UTF_8);
				BufferedReader br = new BufferedReader(fr);
				StringBuilder sb = new StringBuilder((int)file.length());
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				
				this.Texts.add(sb.toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
