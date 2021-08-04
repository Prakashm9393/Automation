package testcases;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import utils.ExcelDataUtility;
import utils.ReadPropertiesFiles;

public class TC002{
		
		RemoteWebDriver driver = null;
		WebDriverWait wait;
		
		@BeforeClass
		public void beforeClass() throws IOException{				
			ReadPropertiesFiles.loadConfingFile();
//			System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");
//			ChromeOptions options = new ChromeOptions();
//			options.setExperimentalOption("debuggerAddress", "localhost:9014");
//			driver = new ChromeDriver(options);
			System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");  
			driver = new ChromeDriver();
			driver.manage().window().maximize();
		}
		
		@Test
		public void checkTC002() throws Exception{
			ExcelDataUtility data = new ExcelDataUtility("./data/"+ReadPropertiesFiles.FileName+".xlsx");
			for (int i =340; i <= data.getTotalRowNumber(ReadPropertiesFiles.SheetName); i++){	
				wait = new WebDriverWait(driver, 10);
				driver.get("https://whatcms.org/");
				driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
				System.out.println(i+" record starts running...");			
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("what-cms-size")));
				String email = data.getCellData(ReadPropertiesFiles.SheetName, 0, i);
				System.out.println("Detect CMS for "+email+" site.");
				driver.findElementById(ReadPropertiesFiles.MailIdLocate_TC2).sendKeys(email);
				driver.findElementByXPath(ReadPropertiesFiles.NextBut_TC2).click();	
				try {
					if (driver.findElementByXPath(ReadPropertiesFiles.MsgeBox_TC2_2).isDisplayed()) {
						driver.findElementByXPath(ReadPropertiesFiles.NextBut_TC2).click();
					}
				}catch (Exception e) {	
				}
				Thread.sleep(10000);		
				if (driver.findElementByXPath(ReadPropertiesFiles.MsgeBox).getText().contains("Technology")){
					String a = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox2_TC2_1).getText();
					data.setCellData(ReadPropertiesFiles.SheetName, 1, i, a);
				}
				else if (driver.findElementByXPath(ReadPropertiesFiles.MsgeBox).getText().contains("Sorry")){
					String msg1 = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox_TC3).getText();									
					data.setCellData(ReadPropertiesFiles.SheetName, 1, i, msg1);	
				}
				else if (driver.findElementByXPath(ReadPropertiesFiles.MsgeBox).getText().contains("Success")){
					try {
						String a = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox_TC2_1).getText();
						String b = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox_TC2_1_1).getText();
						String c = a+" "+b;
						data.setCellData(ReadPropertiesFiles.SheetName, 1, i, c);
					}catch (Exception e)  {
					String msg1 = "No CMS Found";	
					data.setCellData(ReadPropertiesFiles.SheetName, 1, i, msg1);	
					}
				}else{
					String msg1 = "Nothing detected, Try again";			
					data.setCellData(ReadPropertiesFiles.SheetName, 1, i, msg1);	
				}				
			}
		}
		
		@AfterClass
		public void afterClass() throws IOException{			
			driver.quit();
		}

	}

