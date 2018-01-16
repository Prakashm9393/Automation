package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TextFileHandler{
	
	public static void writeListOfDataIntoTheTextFile(String filename,List<String> contentList){
		String FILENAME = "./"+filename+".txt";
		BufferedWriter bw = null;
		FileWriter fw = null;
		try{
			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);
			for (String content : contentList) {
				bw.write(content+"\n");
			}			
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();

			} catch (IOException ex){
				ex.printStackTrace();
			}
	  }		
  }		

}
