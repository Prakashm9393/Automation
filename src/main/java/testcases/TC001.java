package testcases;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utils.ExcelDataUtility;

public class TC001{
	
	RemoteWebDriver driver = null;
	
	@BeforeClass
	public void beforeClass() throws IOException{
		Runtime.getRuntime().exec("./driver/chromedriver.exe", null, new File("./driver"));
		System.err.println("Starting ChromeDriver on 9515. Only local connections are allowed.");		
	}
	
	@Test
	public void checkTC001() throws Exception{
		ExcelDataUtility data = new ExcelDataUtility("./data/Mail_Campaign_Two.xlsx");
		for (int i = 1; i < data.getTotalRowNumber("Sheet1"); i++){
			driver = new RemoteWebDriver(new URL("http://127.0.0.1:9515"), DesiredCapabilities.chrome());
			driver.manage().window().maximize();
			driver.get("http://www.ip-tracker.org/checker/email-lookup.php");
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
			String email = data.getCellData("Sheet1", 0, i);
			driver.findElementById("txtOne").sendKeys(email);
			driver.findElementByClassName("inputinyext").click();		
			Thread.sleep(3000);
			String msg = driver.findElementByXPath("//div[@id='maincontent']/table/tbody/tr[8]/td//div[@class]").getText();			
			data.setCellData("Sheet1", 1, i, msg);
			driver.close();
		}		
	}
	
	@AfterClass
	public void afterClass() throws IOException{
		Runtime.getRuntime().exec("taskkill /F /IM " + "chromedriver.exe");
	}

}
