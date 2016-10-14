package com.softhinkers.galsen.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.softhinkers.galsen.GalsenConfiguration;
import com.softhinkers.galsen.GalsenLauncher;

public class GalsenTest {
	// Declare web driver variable
	private WebDriver driver;

	@BeforeSuite
	public void setUp() throws Exception {

		// Start galsen-standalone during test
		GalsenConfiguration config = new GalsenConfiguration();

		// Add the selendroid-test-app to the standalone server
		config.addSupportedApp("selendroid-test-app-0.12.0.apk");

		// start the standalone server
		GalsenLauncher galsenServer = new GalsenLauncher(config);
		galsenServer.launchGalsen();
	}

	@Test
	public void galsenTest() throws Exception {

		// Print the log
		System.out.print("Start executing test");

		// Find an element by id
		WebElement inputField = driver.findElement(By.id("my_text_field"));
		// enter a text into the text field
		inputField.sendKeys("Galsen");
		// check if the text has been entered into the text field
		Assert.assertEquals("Galsen", inputField.getText());
		// Delay time to take effect
		Thread.sleep(5000);

	}

	@AfterSuite
	public void tearDown() {
		driver.quit();
	}
}
