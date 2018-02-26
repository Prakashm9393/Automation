package testcases;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pages.EmailLookUpPage;
import utils.ExcelDataUtility;
import wrappers.ApplicationWrapper;

public class TC001 extends ApplicationWrapper{
	
	@BeforeClass
	public void beforeClass(){
		invokeApp("Chrome");
		startTestCase("TC001", "Compare Mail Address");
	}
	
	@Test
	public void checkTC001() throws Exception{
		ExcelDataUtility data = new ExcelDataUtility("./data/Mail_Campaign_One.xlsx");
		for (int i = 1; i < data.getTotalRowNumber("Sheet1"); i++) {
			String email = data.getCellData("Sheet1", 0, i);
			new EmailLookUpPage()
			.enterEmailAddress(email)
			.clickOnTheVerifyEmailButton();
			waitTime(2500);
			scrollToTheGivenWebElement("Xpath&//div[@id='maincontent']/table/tbody/tr[8]/td//div[2]");
			String msg = getDriver().findElementByXPath("//div[@id='maincontent']/table/tbody/tr[8]/td//div[2]").getText();			
			data.setCellData("Sheet1", 1, i, msg);
		}		
	}
	
	@AfterClass
	public void afterClass(){
		quitBrowser();
	}

}
