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
import utils.ReadPropertiesFiles;

public class TC002{
		
		RemoteWebDriver driver = null;
		
		@BeforeClass
		public void beforeClass() throws IOException{
			Runtime.getRuntime().exec("./driver/chromedriver.exe", null, new File("./driver"));
			System.err.println("Starting ChromeDriver on 9515. Only local connections are allowed.");
			
			ReadPropertiesFiles.loadConfingFile();			
		}
		
		@Test
		public void checkTC001() throws Exception{
			ExcelDataUtility data = new ExcelDataUtility("./data/"+ReadPropertiesFiles.FileName+".xlsx");
			for (int i = 70; i <= data.getTotalRowNumber(ReadPropertiesFiles.SheetName); i++){				
				driver = new RemoteWebDriver(new URL("http://127.0.0.1:9515"), DesiredCapabilities.chrome());
				driver.manage().window().maximize();
				driver.get("https://whatcms.org/");
				driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
				String email = data.getCellData(ReadPropertiesFiles.SheetName, 0, i);
				driver.findElementById(ReadPropertiesFiles.MailIdLocate_TC2).sendKeys(email);
				driver.findElementByClassName(ReadPropertiesFiles.NextBut_TC2).click();		
				Thread.sleep(3000);				
				if (driver.findElementByXPath(ReadPropertiesFiles.MsgeBox).getText().contains("Success")){
					String msg1 = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox_TC2_1).getText();			
					data.setCellData(ReadPropertiesFiles.SheetName, 1, i, msg1);	
				}else{
					String a = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox2_TC2_1).getText();
					String b = driver.findElementByXPath(ReadPropertiesFiles.MsgeBox2_TC2_2).getText();
					String c = a+" "+b;
					data.setCellData(ReadPropertiesFiles.SheetName, 1, i, c);
				}				
				// driver.findElementById(ReadPropertiesFiles.MailIdLocate_TC2).clear();
				driver.close();
			}
			
		}
		
		@AfterClass
		public void afterClass() throws IOException{
			
			Runtime.getRuntime().exec("taskkill /F /IM " + "chromedriver.exe");	
		}

	}

