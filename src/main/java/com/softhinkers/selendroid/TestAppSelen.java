package com.softhinkers.selendroid;

import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidConfiguration;
import io.selendroid.SelendroidDriver;
import io.selendroid.SelendroidLauncher;
import io.selendroid.device.DeviceTargetPlatform;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class TestAppSelen {
	// Declare web driver variable
	private WebDriver driver;

	/**
	 * Setup the environment before testing
	 * 
	 * @throws Exception
	 */
	@BeforeSuite
	public void setUp() throws Exception {

		// Start selendroid-standalone during test
		SelendroidConfiguration config = new SelendroidConfiguration();

		// Add the selendroid-test-app to the standalone server
		 config.addSupportedApp("selendroid-test-app-0.12.0.apk");

		// start the standalone server
		SelendroidLauncher selendroidServer = new SelendroidLauncher(config);
		selendroidServer.launchSelendroid();

		// Create the selendroid capabilities
		SelendroidCapabilities capa = new SelendroidCapabilities();

		// Specify to use selendroid's test app
		 capa.setAut("io.selendroid.testapp:0.12.0");

		// Specify to use the Android device API 19
		capa.setPlatformVersion(DeviceTargetPlatform.ANDROID19);

		// Don't request simulator, use real device
		capa.setEmulator(false);

		// capa.wait(10000000);

		// Create instance of Selendroid Driver
		driver = new SelendroidDriver(capa);
	}

	/**
	 * Start execute the test case 01. Enter the text "Selendroid" to the
	 * textfield 02. Press OK button
	 * 
	 * @throws Exception
	 */
	@Test
	public void selendroidTest() throws Exception {

		// Print the log
		System.out.print("Start executing test");

		// Find an element by id
		WebElement inputField = driver.findElement(By.id("my_text_field"));
		//enter a text into the text field
		inputField.sendKeys("Selendroid");
		//check if the text has been entered into the text field
		Assert.assertEquals("Selendroid", inputField.getText());
		// Delay time to take effect
		Thread.sleep(5000);

	}

	/**
	 * Stop the Selendroid driver
	 * 
	 */
	@AfterSuite
	public void tearDown() {
		driver.quit();
	}

}