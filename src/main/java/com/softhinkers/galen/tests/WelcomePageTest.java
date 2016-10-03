package com.softhinkers.galen.tests;

import java.io.IOException;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.softhinkers.galen.components.GalenTestBase;

public class WelcomePageTest extends GalenTestBase {

	@Test(dataProvider = "devices")
	public void welcomePage_shouldLookGood_onDevice(TestDevice device)
			throws IOException {
		load("/");
		checkLayout("./src/main/resources/specs/welcomePage.spec", device.getTags());
	}

	@Test(dataProvider = "devices")
	public void loginPage_shouldLookGood_onDevice(TestDevice device)
			throws IOException {
		load("/");
		getDriver().findElement(By.xpath("//button[.='Login']")).click();
		checkLayout("./src/main/resources/specs/loginPage.spec", device.getTags());
	}

	

}
