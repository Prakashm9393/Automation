package testcases;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utils.ExcelDataUtility;

public class TC002{
	
RemoteWebDriver driver = null;
	
	@BeforeClass
	public void beforeClass(){
		System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("http://www.ip-tracker.org/checker/email-lookup.php");
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}
	
	@Test
	public void checkTC001() throws Exception{
		ExcelDataUtility data = new ExcelDataUtility("./data/Mail_Campaign_One.xlsx");
		for (int i = 1; i < data.getTotalRowNumber("Sheet1"); i++){			
			String email = data.getCellData("Sheet1", 0, i);
			driver.findElementById("txtOne").sendKeys(email);
			driver.findElementByClassName("inputinyext").click();		
			Thread.sleep(3000);
			String msg = driver.findElementByXPath("//div[@id='maincontent']/table/tbody/tr[8]/td//div[@class]").getText();			
			data.setCellData("Sheet1", 1, i, msg);			
		}		
	}
	
	@AfterClass
	public void afterClass(){
		driver.close();
	}

}
