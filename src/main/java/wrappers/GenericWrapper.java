package wrappers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.Log;
import utils.Reporter;
import utils.TextFileHandler;

public class GenericWrapper extends Reporter{

	protected static final ThreadLocal<GenericWrapper> driverThreadLocal = new ThreadLocal<GenericWrapper>();
	public RemoteWebDriver driver;	
	protected Properties prop;
	public String sUrl,primaryWindowHandle,sHubUrl,sHubPort,sWidth,sHeight;
	protected static String os = System.getProperty("os.name");
	protected static String osBit = System.getProperty("os.arch");
	
	public void setDriver(GenericWrapper wrappers) {
		driverThreadLocal.set(wrappers);
	}

	public RemoteWebDriver getDriver() {
		return driverThreadLocal.get().driver;
	}
	
	public GenericWrapper() {
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./config.properties")));
			sHubUrl = prop.getProperty("HUB");
			sHubPort = prop.getProperty("PORT");
			sUrl = prop.getProperty("URL");
			sWidth = prop.getProperty("WIDTH");
			sHeight = prop.getProperty("HEIGHT");
		} catch (FileNotFoundException e) {
			Log.fatal("Unable to find config.properties file in the project root folder."+e.toString());
			throw new RuntimeException("Unable to find config.properties file in the project root folder."+e.toString());
		} catch (IOException e) {
			Log.fatal("Unable to read config.properties it shows following error."+e.toString());
			throw new RuntimeException("Unable to read config.properties it shows following error."+e.toString());
		}
	}
	
	/**
	 * This method will launch the browser in local machine and maximise the browser and set the
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
	 * @param url - The url with http or https	 
	 * 
	 */
	public RemoteWebDriver invokeApp(String browser) {
		return invokeApp(browser,false);
	}

	/**
	 * This method will launch the mentioned browser in grid node or local and maximise the browser and set the
	 * wait for 30 seconds and load the url 
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM	
	 * 
	 */
	public synchronized RemoteWebDriver invokeApp(String browser, boolean bRemote) {
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
	    
		try {

			DesiredCapabilities dc = new DesiredCapabilities();
			dc.setBrowserName(browser);
			dc.setPlatform(Platform.WINDOWS);
			
			// this is for grid run
			if(bRemote){
				driver = new RemoteWebDriver(new URL("http://"+sHubUrl+":"+sHubPort+"/wd/hub"), dc);
			}else{ // this is for local run
				if(browser.equalsIgnoreCase("CHROME")){	
					if(os.contains("Windows")){
						driver = new RemoteWebDriver(new URL("http://127.0.0.1:9515"), DesiredCapabilities.chrome());
						driver.manage().window().maximize();
					}else if(os.equals("Linux") && osBit.contains("64")){
						System.setProperty("webdriver.chrome.driver", "./drivers/linux64/chromedriver");
						driver = new ChromeDriver();
					}else if(os.equals("Linux") && osBit.contains("86")){
						System.setProperty("webdriver.chrome.driver", "./drivers/linux32/chromedriver");
						driver = new ChromeDriver();
					}else{
						System.setProperty("webdriver.chrome.driver", "./drivers/mac64/chromedriver");
						driver = new ChromeDriver();
					}
										
				}else if(browser.equalsIgnoreCase("FIREFOX")){
					if(os.contains("Windows") && osBit.contains("64")){
						System.setProperty("webdriver.gecko.driver", "./drivers/win64/geckodriver.exe");
						driver = new FirefoxDriver();
					}else if(os.contains("Windows") && osBit.contains("86")){
						System.setProperty("webdriver.gecko.driver", "./drivers/win32/geckodriver.exe");
						driver = new FirefoxDriver();
					}else if(os.equals("Linux") && osBit.contains("64")){
						System.setProperty("webdriver.gecko.driver", "./drivers/linux64/geckodriver");
						driver = new FirefoxDriver();
					}else if(os.equals("Linux") && osBit.contains("86")){
						System.setProperty("webdriver.gecko.driver", "./drivers/linux32/geckodriver");
						driver = new FirefoxDriver();
					}else{
						System.setProperty("webdriver.gecko.driver", "./drivers/mac64/geckodriver");
						driver = new FirefoxDriver();
					}
									
				}
			}
			GenericWrapper gw = new GenericWrapper();
			gw.driver = driver;
			setDriver(gw);
			
			getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			getDriver().get(sUrl);
			Log.info("Successfully launched "+sUrl+" application on the "+browser+" browser.");
		} catch (Exception e) {
			Log.fatal("Unable to launch application "+sUrl+" in the "+browser+" browser."+e.toString());
			throw new RuntimeException("Unable to launch application "+sUrl+" in the "+browser+" browser."+e.toString());	
		}
		return getDriver();
	}
	
	/**
	 * This method is used to take screenshots	 
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
	 */
	public long takeSnap(){
		long number = (long) Math.floor(Math.random() * 900000000L) + 10000000L; 
		try {
			FileUtils.copyFile(getDriver().getScreenshotAs(OutputType.FILE) , new File("./report/images/"+number+".jpg"));
		} catch (WebDriverException e) {
			e.printStackTrace();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {				
				e1.printStackTrace();
			}
			reportStep("The browser has been closed.", "FAIL");
		} catch (IOException e) {
			reportStep("The snapshot could not be taken", "WARN");
		}
		return number;
	}
	
	/**
	 * This method is used to load DOM element object
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
	 */
	public void loadObjects() {
		prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./object.properties")));
		} catch (FileNotFoundException e) {
			Log.fatal("Unable to find object.properties file in the project root folder."+e.toString());
			throw new RuntimeException("Unable to find object.properties file in the project root folder."+e.toString());
		} catch (IOException e) {
			Log.fatal("Unable to find object.properties file in the project root folder."+e.toString());
			throw new RuntimeException("Unable to find object.properties file in the project root folder."+e.toString());
		}

	}
	
	/**
     * This is method is used to resize the window depends on resolution
     * @param resolution - input of resolution
     * @return boolean value
     * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
     * @throws Exception
     */
    public boolean invokeAppInMobileBrowser(){
    	boolean bReturn = false;    
    	int width = Integer.parseInt(sWidth);
    	int height = Integer.parseInt(sHeight);
    	try {    		
			Dimension d = new Dimension(width, height);
			getDriver().manage().window().setSize(d);
			Log.info("The view port size is "+width+"x"+height+".");
			bReturn = true;
		} catch (Exception e) {	
			Log.fatal("Unable resize the window "+e.toString());
			throw new RuntimeException("Unable resize the window "+e.toString());
		}
		return bReturn;
    }
	
	/**
	 * This method will close all the browsers
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
	 */
	public void quitBrowser() {
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
	    
		try {
			getDriver().quit();
			Log.info("Successfully closed the browsers.");
		} catch (Exception e) {
			Log.fatal("Unable to close the browsers."+e.toString());
			reportStep("The browser:"+getDriver().getCapabilities().getBrowserName()+" could not be closed.", "FAIL");
		}
	}
	
	/**
	 * This method is used to start chrome browser for windows OS
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
	 * @throws Exception
	 */
	public void startChromeServer(){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
		if(os.contains("Windows") && osBit.contains("64")){
			try {
				Runtime.getRuntime().exec("./drivers/win64/chromedriver.exe", null, new File("./drivers"));
				System.err.println("Starting ChromeDriver on 9515. Only local connections are allowed.");
				Log.info("Starting ChromeDriver on 9515. Only local connections are allowed.");
			} catch (Exception e) {
				Log.fatal("Unable to start Chrome Server error ===> "+e.getMessage());
				throw new RuntimeException("Unable to start Chrome Server error ===> "+e.getMessage());
			}			
		}else{
			try {
				Runtime.getRuntime().exec("./drivers/win32/chromedriver.exe", null, new File("./drivers"));
				System.err.println("Starting ChromeDriver on 9515. Only local connections are allowed.");
				Log.info("Starting ChromeDriver on 9515. Only local connections are allowed.");
			} catch (Exception e) {
				Log.fatal("Unable to start Chrome Server error ===> "+e.getMessage());
				throw new RuntimeException("Unable to start Chrome Server error ===> "+e.getMessage());
			}			
		}
	}
	
	/**
	 * This method is used to stop chrome browser for windows OS
	 * @author Karthikeyan Rajendran on 15/11/2017:12:30:00PM
	 * @throws Exception
	 */
	public void stopChromeServer(){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
		if(os.contains("Windows")){
			try {			
				Runtime.getRuntime().exec("taskkill /F /IM " + "chromedriver.exe");
				Log.info("Stoping Chrome Server.");
			} catch (Exception e) {
				Log.fatal("Unable to stop Chrome Server error ===> "+e.getMessage());
				throw new RuntimeException("Unable to stop Chrome Server error ===> "+e.getMessage());
			}
		}		
	}
	
	/**
	 * This method is used enter give text into the object based on the locator
	 * and wait for 10 seconds until visibility of the web element
	 * @param locator - find and match the elements of web page
	 * @param input - the text which enter into the object 
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:07:00PM
	 * @throws Exception
	 */
	public boolean enterText(String locator,String input){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
		boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				getDriver().findElementById(value).clear();
				getDriver().findElementById(value).sendKeys(input);
				reportStep("The data: "+input+" entered successfully in field :"+value, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				getDriver().findElementByName(value).clear();
				getDriver().findElementByName(value).sendKeys(input);
				reportStep("The data: "+input+" entered successfully in field :"+value, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				getDriver().findElementByClassName(value).clear();
				getDriver().findElementByClassName(value).sendKeys(input);
				reportStep("The data: "+input+" entered successfully in field :"+value, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}					
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				getDriver().findElementByTagName(value).clear();
				getDriver().findElementByTagName(value).sendKeys(input);
				reportStep("The data: "+input+" entered successfully in field :"+value, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				getDriver().findElementByCssSelector(value).clear();
				getDriver().findElementByCssSelector(value).sendKeys(input);
				reportStep("The data: "+input+" entered successfully in field :"+value, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				getDriver().findElementByXPath(value).clear();
				getDriver().findElementByXPath(value).sendKeys(input);
				reportStep("The data: "+input+" entered successfully in field :"+value, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else{
			Log.error("Kindly, provide correct locator option for enterText.");
			reportStep("Kindly, provide correct locator option for enterText.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for enterText.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used click on the given object based on the locator
	 * and wait for 10 seconds until visibility of the web element
	 * @param locator - find and match the elements of web page
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:07:00PM
	 * @throws Exception
	 */
	public boolean clickOn(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
		boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];		
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				WebElement ele = getDriver().findElementById(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", ele);
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				WebElement ele = getDriver().findElementByName(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", ele);
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("LINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 30);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(value)));
				getDriver().findElementByLinkText(value).click();				
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");			
			}			
		}else if(key.equalsIgnoreCase("PARTIALLINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 30);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(value)));
				getDriver().findElementByPartialLinkText(value).click();
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				WebElement ele = getDriver().findElementByClassName(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", ele);
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				WebElement ele = getDriver().findElementByTagName(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", ele);
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				WebElement ele = getDriver().findElementByCssSelector(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", ele);
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				WebElement ele = getDriver().findElementByXPath(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", ele);
				reportStep("The element : "+value+" is clicked.", "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");				
			}			
		}else{
			Log.error("Kindly, provide correct locator option for clickOn.");
			reportStep("Kindly, provide correct locator option for clickOn.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for clickOn.");
		}		
		return bReturn;
	}
	
	/**
	 * This method used to verify text in the element with expected value
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @param expected - expected value
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:09:00PM
	 * @throws Exception
	 */
	public boolean verifyText(String locator,String expected){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
	    boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				if(getDriver().findElementById(value).getText().trim().equals(expected)){
					reportStep("The text: "+getDriver().findElementById(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementById(value).getText().trim()+" did not match with the value : "+expected, "FAIL");					
				}
			} catch (Exception e) {	
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");				
			}				
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));	
				if(getDriver().findElementByName(value).getText().trim().equals(expected)){
					reportStep("The text: "+getDriver().findElementByName(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByName(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				if(getDriver().findElementByClassName(value).getText().trim().equals(expected)){
					reportStep("The text: "+getDriver().findElementByClassName(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByClassName(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));	
				if(getDriver().findElementByTagName(value).getText().trim().equals(expected)){
					reportStep("The text: "+getDriver().findElementByTagName(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByTagName(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));	
				if(getDriver().findElementByCssSelector(value).getText().trim().equals(expected)){
					reportStep("The text: "+getDriver().findElementByCssSelector(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByCssSelector(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));	
				if(getDriver().findElementByXPath(value).getText().trim().equals(expected)){
					reportStep("The text: "+getDriver().findElementByXPath(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByXPath(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else{
			Log.error("Kindly, provide correct locator option for verifyText.");
			reportStep("Kindly, provide correct locator option for verifyText.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for verifyText.");
		}
		return bReturn;
	}
	
	/**
	 * This method used to verify the url of the page
	 * and wait for 10 seconds to appear title of the web page
	 * @param expected - web page's expected url
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:09:00PM
	 * @throws Exception
	 */
	public boolean verifyUrlOfThePage(String expected){	
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
	    
		boolean bReturn = false;
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), 10);
			wait.until(ExpectedConditions.urlToBe(expected));
			reportStep("The expected "+expected+" url as same as the "+getDriver().getCurrentUrl()+" actual url.", "PASS");
			bReturn = true;
		} catch (Exception e) {			
			Log.fatal("The expected "+expected+" url wasn't same as the "+getDriver().getCurrentUrl()+" actual url."+e.toString());	
			reportStep("The expected "+expected+" url wasn't same as the "+getDriver().getCurrentUrl()+" actual url."+e.toString(), "FAIL");
		}		
		return bReturn;
	}
	
	/**
	 * This method used to verify the title of the page
	 * and wait for 10 seconds to appear title of the web page
	 * @param expected - web page's expected title
	 * @return boolean value
	 * @author Karthikeyan Rajendran 28/11/2017:18:10:00PM
	 * @throws Exception
	 */
	public boolean verifyTitleOfThePage(String expected){	
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
	    
		boolean bReturn = false;
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), 10);
			wait.until(ExpectedConditions.titleContains(expected));
			reportStep("The Title of the page is "+getDriver().getTitle()+" same as the expected "+expected, "PASS");
			bReturn = true;
		} catch (Exception e) {	
			Log.fatal("Unable to find "+expected+" title in the page "+e.toString());		
			reportStep("Unable to find "+expected+" title in the page "+e.toString(), "FAIL");
		}		
		return bReturn;
	}
	
	/**
	 * This method used to verify contain text in the element with expected value
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @param expected - expected value
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:11:00PM
	 * @throws Exception
	 */
	public boolean verifyTextContains(String locator,String expected){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
	    boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				if(getDriver().findElementById(value).getText().trim().contains(expected)){
					reportStep("The text: "+getDriver().findElementById(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementById(value).getText().trim()+" did not match with the value : "+expected, "FAIL");					
				}
			} catch (Exception e) {	
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");				
			}				
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));	
				if(getDriver().findElementByName(value).getText().trim().contains(expected)){
					reportStep("The text: "+getDriver().findElementByName(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByName(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				if(getDriver().findElementByClassName(value).getText().trim().contains(expected)){
					reportStep("The text: "+getDriver().findElementByClassName(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByClassName(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));	
				if(getDriver().findElementByTagName(value).getText().trim().contains(expected)){
					reportStep("The text: "+getDriver().findElementByTagName(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByTagName(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));	
				if(getDriver().findElementByCssSelector(value).getText().trim().contains(expected)){
					reportStep("The text: "+getDriver().findElementByCssSelector(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByCssSelector(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));	
				if(getDriver().findElementByXPath(value).getText().trim().contains(expected)){
					reportStep("The text: "+getDriver().findElementByXPath(value).getText().trim()+" matches with the value : "+expected, "PASS");
					bReturn = true;
				}else{
					reportStep("The text: "+getDriver().findElementByXPath(value).getText().trim()+" did not match with the value : "+expected, "FAIL");
				}
			} catch (Exception e) {				
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else{
			Log.fatal("Kindly, provide correct locator option for verifyText.");
			reportStep("Kindly, provide correct locator option for verifyText.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for verifyText.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to select value in the dropdown by visible text
	 * @param locator - find and match the elements of web page
	 * @param visibleText - visible text of the dropdown
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:25:00PM
	 * @throws Exception
	 */
	public boolean selectByVisibleTextInDropdown(String locator,String visibleText){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
	    boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];		
		if(key.equalsIgnoreCase("ID")){
			try {				
				new Select(getDriver().findElementById(value)).selectByVisibleText(visibleText);
				reportStep("The element with id: "+value+" is selected with visible text: "+visibleText, "PASS");
				bReturn = true;
			} catch (Exception e) {	
				Log.fatal("The visible text: "+visibleText+" could not be selected. "+e.toString());	
				reportStep("The visible text: "+visibleText+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("NAME")){
			try {				
				new Select(getDriver().findElementByName(value)).selectByVisibleText(visibleText);
				reportStep("The element with name: "+value+" is selected with visible text: "+visibleText, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The visible text: "+visibleText+" could not be selected. "+e.toString());	
				reportStep("The visible text: "+visibleText+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {					
				new Select(getDriver().findElementByClassName(value)).selectByVisibleText(visibleText);
				reportStep("The element with classname: "+value+" is selected with visible text: "+visibleText, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The visible text: "+visibleText+" could not be selected. "+e.toString());	
				reportStep("The visible text: "+visibleText+" could not be selected. "+e.toString(), "FAIL");
			}					
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {				
				new Select(getDriver().findElementByTagName(value)).selectByVisibleText(visibleText);
				reportStep("The element with tagname: "+value+" is selected with visible text: "+visibleText, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The visible text: "+visibleText+" could not be selected. "+e.toString());	
				reportStep("The visible text: "+visibleText+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {				
				new Select(getDriver().findElementByCssSelector(value)).selectByVisibleText(visibleText);
				reportStep("The element with cssselector: "+value+" is selected with visible text: "+visibleText, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The visible text: "+visibleText+" could not be selected. "+e.toString());	
				reportStep("The visible text: "+visibleText+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {					
				new Select(getDriver().findElementByXPath(value)).selectByVisibleText(visibleText);
				reportStep("The element with xpath: "+value+" is selected with visible text: "+visibleText, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The visible text: "+visibleText+" could not be selected. "+e.toString());	
				reportStep("The visible text: "+visibleText+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else{
			Log.error("Kindly, provide correct locator option for selectByVisibleTextInDropdown.");
			reportStep("Kindly, provide correct locator option for selectByVisibleTextInDropdown.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for selectByVisibleTextInDropdown.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to select value in the dropdown by value
	 * @param locator - find and match the elements of web page
	 * @param dValue - value of the dropdown
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:25:00PM
	 * @throws Exception
	 */
	public boolean selectByValueInDropdown(String locator,String dValue){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
	    boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];		
		if(key.equalsIgnoreCase("ID")){
			try {				
				new Select(getDriver().findElementById(value)).selectByValue(dValue);
				reportStep("The element with id: "+value+" is selected with value: "+dValue, "PASS");
				bReturn = true;
			} catch (Exception e) {	
				Log.fatal("The value: "+dValue+" could not be selected. "+e.toString());	
				reportStep("The value: "+dValue+" could not be selected. "+e.toString(), "FAIL");
			}							
		}else if(key.equalsIgnoreCase("NAME")){
			try {				
				new Select(getDriver().findElementByName(value)).selectByValue(dValue);
				reportStep("The element with name: "+value+" is selected with value: "+dValue, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The value: "+dValue+" could not be selected. "+e.toString());	
				reportStep("The value: "+dValue+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {				
				new Select(getDriver().findElementByClassName(value)).selectByValue(dValue);	
				reportStep("The element with classname: "+value+" is selected with value: "+dValue, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The value: "+dValue+" could not be selected. "+e.toString());	
				reportStep("The value: "+dValue+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {				
				new Select(getDriver().findElementByTagName(value)).selectByValue(dValue);
				reportStep("The element with tagname: "+value+" is selected with value: "+dValue, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The value: "+dValue+" could not be selected. "+e.toString());	
				reportStep("The value: "+dValue+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {				
				new Select(getDriver().findElementByCssSelector(value)).selectByValue(dValue);
				reportStep("The element with cssselector: "+value+" is selected with value: "+dValue, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The value: "+dValue+" could not be selected. "+e.toString());	
				reportStep("The value: "+dValue+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {				
				new Select(getDriver().findElementByXPath(value)).selectByValue(dValue);
				reportStep("The element with xpath: "+value+" is selected with value: "+dValue, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The value: "+dValue+" could not be selected. "+e.toString());	
				reportStep("The value: "+dValue+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else{
			Log.error("Kindly, provide correct locator option for selectByValueInDropdown.");
			reportStep("Kindly, provide correct locator option for selectByValueInDropdown.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for selectByValueInDropdown.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to select value in the dropdown by index
	 * @param locator - find and match the elements of web page
	 * @param index - index of the dropdown
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:25:00PM
	 * @throws Exception
	 */
	public boolean selectByIndexInDropdown(String locator,int index){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");
		
	    boolean bReturn = false;
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];		
		if(key.equalsIgnoreCase("ID")){
			try {				
				new Select(getDriver().findElementById(value)).selectByIndex(index);
				reportStep("The element with id: "+value+" is selected with index: "+index, "PASS");
				bReturn = true;
			} catch (Exception e) {	
				Log.fatal("The index: "+index+" could not be selected. "+e.toString());	
				reportStep("The index: "+index+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("NAME")){
			try {				
				new Select(getDriver().findElementByName(value)).selectByIndex(index);
				reportStep("The element with name: "+value+" is selected with index: "+index, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The index: "+index+" could not be selected. "+e.toString());	
				reportStep("The index: "+index+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {				
				new Select(getDriver().findElementByClassName(value)).selectByIndex(index);	
				reportStep("The element with classname: "+value+" is selected with index: "+index, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The index: "+index+" could not be selected. "+e.toString());	
				reportStep("The index: "+index+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {				
				new Select(getDriver().findElementByTagName(value)).selectByIndex(index);
				reportStep("The element with tagname: "+value+" is selected with index: "+index, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The index: "+index+" could not be selected. "+e.toString());	
				reportStep("The index: "+index+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {				
				new Select(getDriver().findElementByCssSelector(value)).selectByIndex(index);
				reportStep("The element with cssselector: "+value+" is selected with index: "+index, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The index: "+index+" could not be selected. "+e.toString());	
				reportStep("The index: "+index+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {				
				new Select(getDriver().findElementByXPath(value)).selectByIndex(index);
				reportStep("The element with xpath: "+value+" is selected with index: "+index, "PASS");
				bReturn = true;
			} catch (Exception e) {				
				Log.fatal("The index: "+index+" could not be selected. "+e.toString());	
				reportStep("The index: "+index+" could not be selected. "+e.toString(), "FAIL");
			}			
		}else{
			Log.error("Kindly, provide correct locator option for selectByIndexInDropdown.");
			reportStep("Kindly, provide correct locator option for selectByIndexInDropdown.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for selectByIndexInDropdown.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to pause the execution of current thread for given time.
	 * @param ms - time in milliseconds
	 * @author Karthikeyan Rajendran on 28/11/2017:18:25:00PM
	 */
	public void waitTime(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {	
			Log.fatal(e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to find given element in the DOM
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:30:00PM
	 */
	public boolean findElement(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");		
	    
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		boolean bReturn = false;
		if (key.equalsIgnoreCase("ID")) {
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				reportStep(value+" : id is avaliable in the DOM elements.", "PASS");
				bReturn = true;
			} catch (Exception e) {
				Log.info("Unable to find the id : "+value+" in the DOM elements. "+e.toString());				
			} 
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				reportStep(value+" : name is avaliable in the DOM elements.", "PASS");
				bReturn = true;
			} catch (Exception e) {
				Log.info("Unable to find the name : "+value+" in the DOM elements. "+e.toString());					
			}
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				reportStep(value+" : class name is avaliable in the DOM elements.", "PASS");
				bReturn = true;
			} catch (Exception e) {
				Log.info("Unable to find the class name : "+value+" in the DOM elements. "+e.toString());				
			}
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				reportStep(value+" : tag name is avaliable in the DOM elements.", "PASS");
				bReturn = true;
			} catch (Exception e) {
				Log.info("Unable to find the tag name : "+value+" in the DOM elements. "+e.toString());				
			}
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				reportStep(value+" : css selector is avaliable in the DOM elements.", "PASS");
				bReturn = true;
			} catch (Exception e) {
				Log.info("Unable to find the css selector : "+value+" in the DOM elements. "+e.toString());				
			}
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				reportStep(value+" : Xpath is avaliable in the DOM elements.", "PASS");
				bReturn = true;
			} catch (Exception e) {
				Log.info("Unable to find the Xpath : "+value+" in the DOM elements. "+e.toString());				
			}
		}else{
			Log.fatal("Kindly, provide correct locator option for element.");
			reportStep("Kindly, provide correct locator option for element.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for element.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to get web element
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @return WebElement value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:30:00PM
	 */
	public WebElement getElement(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");		
	    
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		WebElement wReturn = null;
		if (key.equalsIgnoreCase("ID")) {
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));	
				wReturn = getDriver().findElementById(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");				
			}			
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));		
				wReturn = getDriver().findElementByName(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));	
				wReturn = getDriver().findElementByClassName(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));	
				wReturn = getDriver().findElementByTagName(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				wReturn = getDriver().findElementByCssSelector(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));	
				wReturn = getDriver().findElementByXPath(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");	
			}			
		}else{
			Log.fatal("Kindly, provide correct locator option for element.");
			reportStep("Kindly, provide correct locator option for element.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for element.");
		}
		return wReturn;
	}
	
	/**
	 * This method is used to perform right click action on the clickable element
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:30:00PM
	 */
	public boolean rightClickOnElementAndClickOnNewTab(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");		
	    
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		WebElement ele;
		Actions builder = new Actions(getDriver());
		boolean bReturn = false;
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				ele = getDriver().findElementById(value);			
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}		    
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				ele = getDriver().findElementByName(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("LINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(value)));
				ele = getDriver().findElementByLinkText(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("PARTIALLINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(value)));
				ele = getDriver().findElementByPartialLinkText(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				ele = getDriver().findElementByClassName(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				ele = getDriver().findElementByTagName(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				ele = getDriver().findElementByCssSelector(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				ele = getDriver().findElementByXPath(value);
				builder.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(ele).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
				bReturn = true;
			} catch (Exception e) {				
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else{
			Log.error("Kindly, provide correct locator option for rightClickOnElementAndClickOnNewTab.");
			reportStep("Kindly, provide correct locator option for rightClickOnElementAndClickOnNewTab.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option forrightClickOnElementAndClickOnNewTab.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to perform mouse over action
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:30:00PM
	 */
	public boolean mouseOverAction(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");		
	    
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		WebElement ele;
		Actions builder = new Actions(getDriver());
		boolean bReturn = false;
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				ele = getDriver().findElementById(value);			
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by id : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}		    
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				ele = getDriver().findElementByName(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by name : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("LINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(value)));
				ele = getDriver().findElementByLinkText(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by linktext : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("PARTIALLINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(value)));
				ele = getDriver().findElementByPartialLinkText(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by partialLinkText : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				ele = getDriver().findElementByClassName(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by classname : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				ele = getDriver().findElementByTagName(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by tagname : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				ele = getDriver().findElementByCssSelector(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by cssSelector : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				ele = getDriver().findElementByXPath(value);
				builder.moveToElement(ele).build().perform();
				bReturn = true;
				reportStep("The mouse over by xpath : "+value+" is performed.", "PASS");
			} catch (Exception e) {				
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else{
			Log.error("Kindly, provide correct locator option for mouseOverAction.");
			reportStep("Kindly, provide correct locator option for mouseOverAction.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for mouseOverAction.");
		}
		return bReturn;
	}
	
	/**
	 * This method is used to get list of web elements
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator - find and match the elements of web page
	 * @return WebElement value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:30:00PM
	 * @throws Exception
	 */
	public List<WebElement> getElements(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");		
	    
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		List<WebElement> wReturn = null;
		if (key.equalsIgnoreCase("ID")) {
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));	
				wReturn = getDriver().findElementsById(value);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));		
				wReturn = getDriver().findElementsByName(value);
			} catch (Exception e) {
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));	
				wReturn = getDriver().findElementsByClassName(value);
			} catch (Exception e) {
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("LINKTEXT")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));	
				wReturn = getDriver().findElementsByLinkText(value);
			} catch (Exception e) {
				Log.error("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));	
				wReturn = getDriver().findElementsByTagName(value);
			} catch (Exception e) {
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				wReturn = getDriver().findElementsByCssSelector(value);
			} catch (Exception e) {
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));	
				wReturn = getDriver().findElementsByXPath(value);
			} catch (Exception e) {
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}			
		}else{
			Log.fatal("Kindly, provide correct locator option for getElements.");
			reportStep("Kindly, provide correct locator option for getElements.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for getElements.");
		}
		return wReturn;
	}
	
	/**
	 * This method is used check the check box is checked default
	 * and wait for 10 seconds to appear title of the web page
	 * @param locator- find and match the elements of web page
	 * @return boolean value
	 * @author Karthikeyan Rajendran on 28/11/2017:18:55:00PM
	 * @throws Exception
	 */
	public boolean checkTheCheckBoxIsCheckedDefault(String locator){
		
		//Log4j Configuration XML file 	
	    DOMConfigurator.configure("log4j.xml");		
	    
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		boolean bReturn = false;
		if (key.equalsIgnoreCase("ID")) {			
			try {					
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				bReturn = getDriver().findElementById(value).isSelected();				
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("NAME")){
			try {	
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				bReturn = getDriver().findElementByName(value).isSelected();				
			} catch (Exception e) {
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("CLASSNAME")){			
			try {					
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				bReturn = getDriver().findElementByClassName(value).isSelected();				
			} catch (Exception e) {
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {		
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				bReturn = getDriver().findElementByTagName(value).isSelected();				
			} catch (Exception e) {
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {					
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				bReturn = getDriver().findElementByCssSelector(value).isSelected();				
			} catch (Exception e) {
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("XPATH")){
			try {	
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				bReturn = getDriver().findElementByXPath(value).isSelected();				
			} catch (Exception e) {
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else{
			Log.fatal("Kindly, provide correct locator option for checkTheCheckBoxIsCheckedDefault.");
			reportStep("Kindly, provide correct locator option for checkTheCheckBoxIsCheckedDefault.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for checkTheCheckBoxIsCheckedDefault.");
		}
		return bReturn;	
	}
	
	/**
     * This method is used to create red color border around the given web element
     * @param element - web element of the page
     * @param duration -time duration for the border
     * @author Karthikeyan Rajendran on 28/11/2017:19:06:00PM
     * @throws InterruptedException
     */
    public void highlightElement(WebElement element, int duration){
		
		JavascriptExecutor js = (JavascriptExecutor) getDriver();		
        String original_style = element.getAttribute("style");
        js.executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2])",
                element,
                "style",
                "border: 3px solid red; border-style: solid;");
        if (duration > 0) {
            try {
				Thread.sleep(duration * 1000);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
            js.executeScript(
                    "arguments[0].setAttribute(arguments[1], arguments[2])",
                    element,
                    "style",
                    original_style);
        }
    }
    
    /**
     * This method is used to scroll down the page untill given webelement
     * and wait for 10 seconds to appear title of the web page
     * @param locator - find and match the elements of web page
     * @author Karthikeyan Rajendran on 28/11/2017:19:06:00PM
	 * @throws Exception
     */
    public void scrollToTheGivenWebElement(String locator){
    	String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
    	WebElement element = null;
    	if(key.equalsIgnoreCase("ID")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				element = getDriver().findElementById(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("NAME")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				element = getDriver().findElementByName(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("CLASSNAME")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				element = getDriver().findElementByClassName(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("TAGNAME")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				element = getDriver().findElementByTagName(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("LINKTEXT")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(value)));
				element = getDriver().findElementByLinkText(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the linkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("PARTIALLINKTEXT")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(value)));
				element = getDriver().findElementByPartialLinkText(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the partialLinkText : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("CSSSELECTOR")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				element = getDriver().findElementByCssSelector(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else if(key.equalsIgnoreCase("XPATH")){
    		try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				element = getDriver().findElementByXPath(value);
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", element);
			} catch (Exception e) {
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
    	}else{
    		Log.fatal("Kindly, provide correct locator option for scrollToTheGivenWebElement.");
			reportStep("Kindly, provide correct locator option for scrollToTheGivenWebElement.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for scrollToTheGivenWebElement.");
		}    	 
    }
    
    /**
     * This method is used to select radio button option
     * and wait for 10 seconds to appear title of the web page
     * @param locator - find and match the elements of web page
     * @param text - value to be clicked
     * @return boolean value
     * @author Karthikeyan Rajendran on 28/11/2017:19:06:00PM
     * @throws Exception
     */
    public boolean autoCompleteTextField(String locator,String text){	
    	boolean bReturn = false;
    	
		String[] data = locator.split("&");
		String key = data[0];
		String value = data[1];
		List<WebElement> listOfName = null;
		if(key.equalsIgnoreCase("ID")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(value)));
				listOfName = getDriver().findElementsById(value);
				int sizeOfList = listOfName.size();
				for (int i = 0; i < sizeOfList; i++) {				
					if (listOfName.get(i).getText().trim().equalsIgnoreCase(text)) {
						listOfName.get(i).click();	
						Log.info(text+" is the selected.");					
						break;
					}
				}
			} catch (Exception e) {
				Log.error("Unable to find the id : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the id : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("NAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(value)));
				listOfName = getDriver().findElementsByName(value);
				int sizeOfList = listOfName.size();
				for (int i = 0; i < sizeOfList; i++) {				
					if (listOfName.get(i).getText().trim().equalsIgnoreCase(text)) {
						listOfName.get(i).click();	
						Log.info(text+" is the selected.");					
						break;
					}
				}
			} catch (Exception e) {
				Log.error("Unable to find the name : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the name : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("CLASSNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(value)));
				listOfName = getDriver().findElementsByClassName(value);
				int sizeOfList = listOfName.size();
				for (int i = 0; i < sizeOfList; i++) {				
					if (listOfName.get(i).getText().trim().equalsIgnoreCase(text)) {
						listOfName.get(i).click();	
						Log.info(text+" is the selected.");					
						break;
					}
				}
			} catch (Exception e) {
				Log.error("Unable to find the className : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the className : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("TAGNAME")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(value)));
				listOfName = getDriver().findElementsByTagName(value);
				int sizeOfList = listOfName.size();
				for (int i = 0; i < sizeOfList; i++) {				
					if (listOfName.get(i).getText().trim().equalsIgnoreCase(text)) {
						listOfName.get(i).click();	
						Log.info(text+" is the selected.");					
						break;
					}
				}
			} catch (Exception e) {
				Log.error("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the tagName : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("CSSSELECTOR")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(value)));
				listOfName = getDriver().findElementsByCssSelector(value);
				int sizeOfList = listOfName.size();
				for (int i = 0; i < sizeOfList; i++) {				
					if (listOfName.get(i).getText().trim().equalsIgnoreCase(text)) {
						listOfName.get(i).click();	
						Log.info(text+" is the selected.");					
						break;
					}
				}
			} catch (Exception e) {
				Log.error("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the cssSelector : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else if(key.equalsIgnoreCase("XPATH")){
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(value)));
				listOfName = getDriver().findElementsByXPath(value);
				int sizeOfList = listOfName.size();
				for (int i = 0; i < sizeOfList; i++) {				
					if (listOfName.get(i).getText().trim().equalsIgnoreCase(text)) {
						listOfName.get(i).click();	
						Log.info(text+" is the selected.");					
						break;
					}
				}
			} catch(Exception e){
				Log.error("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString());
				reportStep("Unable to find the xpath : "+value+" in the DOM elements. "+e.toString(), "FAIL");
			}
		}else{
			Log.fatal("Kindly, provide correct locator option for autoCompleteTextField.");
			reportStep("Kindly, provide correct locator option for autoCompleteTextField.", "WARN");
			throw new RuntimeException("Kindly, provide correct locator option for autoCompleteTextField.");
		} 
		return bReturn;
	}
        
    /**
     * This method is used to get all links in the given site
     * @param driver - RemoteWebDriver
     * @return - List<WebElement> value
     * @author Karthikeyan Rajendran on 16/01/2018:14:45:00PM
     */
    public List<WebElement> findAllLinks(RemoteWebDriver driver){    	
    	List<WebElement> elementList = driver.findElements(By.tagName("a"));
    	elementList.addAll(driver.findElements(By.tagName("img")));
    	List<WebElement> finalList = new ArrayList<WebElement>();
    	for (WebElement element : elementList){
   		  if(element.getAttribute("href") != null){
   			  finalList.add(element);
   		  }
   	  }
   	  return finalList;
    }
    
    /**
     * This method is used to send response of the given link
     * @param url - link
     * @return - String value
     * @author Karthikeyan Rajendran on 16/01/2018:14:45:00PM
     * @throws Exception
     */
    public String isLinkBroken(URL url){
		String response = "";		
		try{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.connect();
		    response = connection.getResponseMessage();    
		    connection.disconnect();
		    return response;
		}catch(Exception exp){
			return exp.getMessage();
		}
	}
    
    /**
     * This method is used to find the broken links in the given site
     * @author Karthikeyan Rajendran on 16/01/2018:14:45:00PM 
     * @throws Exception
     */
    public void findBrokenLinks(){
    	List<String> brokenLinks = new ArrayList<String>();
    	List<WebElement> elements = findAllLinks(getDriver());
		int tLinks = elements.size();
		int link = 0;
		System.out.println("Total number of links to scan: " + tLinks);
		System.out.println("Scanning For Broken Links....");
		for (WebElement element : elements) {
			try {
				//System.out.println("URL: " + element.getAttribute("href")+ " returned " + isLinkBroken(new URL(element.getAttribute("href"))));				
				String response = isLinkBroken(new URL(element.getAttribute("href")));
				if(response.equals("OK")){
					link++;
				}else{
					brokenLinks.add("URL: " + element.getAttribute("href")+ " returned " + isLinkBroken(new URL(element.getAttribute("href"))));					
				}
			} catch (Exception exp) {
				brokenLinks.add("At " + element.getAttribute("innerHTML") + " Exception occured -&gt; " + exp.getMessage());							
			}
		}		
		if(link == tLinks){
			System.out.println("There is no broken link in this site: "+sUrl);
		}else{
			int bLinks = tLinks - link;			
			TextFileHandler.writeListOfDataIntoTheTextFile("BrokenLinks", brokenLinks);			
			System.err.println("There are "+bLinks+" broken links in this site: "+sUrl+" . Kindly find broken links in BrokenLinks.txt file with reasons.");
		}	
    }

}
