package utils;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class TestRunStatusReports implements ISuiteListener{
	
	public void onFinish(ISuite suite) {		
		UploadFile.execute();		
	}

	public void onStart(ISuite suite) {		
		System.out.println("About to begin executing Suite " + suite.getName());
	}	

}
