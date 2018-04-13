package testcases;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import utils.ExcelDataUtility;
import utils.ReadPropertiesFiles;

public class TC001{	
	
	RemoteWebDriver driver = null;
	WebDriverWait wait;
	
	@BeforeClass
	public void beforeClass() throws IOException{
		ReadPropertiesFiles.loadConfingFile();
		ReadPropertiesFiles.loadConfingFile();
		System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");  
		ChromeOptions options = new ChromeOptions();  
		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");  
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, 10);
		driver.get(ReadPropertiesFiles.Aut);
	}
	
	@Test
	public void checkTC001() throws Exception{
		ExcelDataUtility data = new ExcelDataUtility("./data/"+ReadPropertiesFiles.FileName+".xlsx");
		for (int i = 194; i <= data.getTotalRowNumber(ReadPropertiesFiles.SheetName); i++){			
			System.out.println(i+" record starts running...");			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtOne")));
			String email = data.getCellData(ReadPropertiesFiles.SheetName, 0, i);
			driver.findElementById(ReadPropertiesFiles.MailIdLocate_TC1).sendKeys(email);
			driver.findElementByClassName(ReadPropertiesFiles.NextBut_TC1).click();	
			if(driver.getTitle().equals("500 Internal Server Error")){
				data.setCellData(ReadPropertiesFiles.SheetName, 1, i, "500 Internal Server Error");
				driver.get(ReadPropertiesFiles.Aut);
			}else{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ReadPropertiesFiles.MsgeBox_TC1)));
				String msg = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox_TC1).getText();			
				data.setCellData(ReadPropertiesFiles.SheetName, 1, i, msg);
			}
			
						
		}
						
	}
	
	@AfterClass
	public void afterClass() throws IOException{
		driver.quit();	
	}

}