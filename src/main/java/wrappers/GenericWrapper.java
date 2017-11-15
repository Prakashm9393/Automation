package wrappers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import utils.Log;
import utils.Reporter;

public class GenericWrapper extends Reporter{

	protected static final ThreadLocal<GenericWrapper> driverThreadLocal = new ThreadLocal<GenericWrapper>();
	public RemoteWebDriver driver;	
	protected Properties prop;
	public String sUrl,primaryWindowHandle,sHubUrl,sHubPort,sWidth,sHeight;
	protected static String os = System.getProperty("os.name");
	
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
			if(bRemote)
				driver = new RemoteWebDriver(new URL("http://"+sHubUrl+":"+sHubPort+"/wd/hub"), dc);
			else{ // this is for local run
				if(browser.equalsIgnoreCase("chrome")){
					driver = new RemoteWebDriver(new URL("http://127.0.0.1:9515"), DesiredCapabilities.chrome());
					driver.manage().window().maximize();					
				}else{
					System.setProperty("webdriver.gecko.driver", "./drivers/geckodriver.exe");
					driver = new FirefoxDriver();					
				}
			}
			GenericWrapper gw = new GenericWrapper();
			gw.driver = driver;
			setDriver(gw);
			
			getDriver().manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
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
			FileUtils.copyFile(getDriver().getScreenshotAs(OutputType.FILE) , new File("./MJN_CA_Reporter/images/"+number+".jpg"));
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
		if(os.contains("Windows")){
			try {
				Runtime.getRuntime().exec("./drivers/chromedriver.exe", null, new File("./drivers"));
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

}
