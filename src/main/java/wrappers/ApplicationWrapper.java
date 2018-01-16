package wrappers;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public class ApplicationWrapper extends GenericWrapper{
	
	@BeforeSuite
	public void beforeSuite(){
		startResult();
		startChromeServer();
	}

	@BeforeTest
	public void beforeTest(){
		
	}
	
	@BeforeMethod
	public void beforeMethod(){

	}
		
	@AfterSuite
	public void afterSuite(){
		endResult();		
		stopChromeServer();
	}

	@AfterTest
	public void afterTest(){
		
	}
	
	@AfterMethod
	public void afterMethod(){
		endTestcase();		
	}

}
