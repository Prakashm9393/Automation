package testcases;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utils.ReadPropertiesFiles;

public class TC003 {
	
	@BeforeClass
	public void beforeClass(){
		ReadPropertiesFiles.loadConfingFile();
	}
	
	@Test
	public void checkTC001(){		
		System.out.println(ReadPropertiesFiles.Aut);
		System.out.println(ReadPropertiesFiles.FileName);
		System.out.println(ReadPropertiesFiles.SheetName);
	}

}
