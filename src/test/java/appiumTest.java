import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;

public class appiumTest {

    private AndroidDriver driver;

    @Test
    public void resetIpFlow() throws TimeoutException {
        setUpDriver();
        try {
            generateDeviceInfo();
        }
        catch ( NoSuchElementException e ) {
            System.out.println( "Element not found" );
            e.printStackTrace();
        }
    }

    private void generateDeviceInfo() {
        WebDriverWait wait = (WebDriverWait) new WebDriverWait( driver, 20 )
                .pollingEvery( 20, TimeUnit.MILLISECONDS );
        try {
            wait.until( ExpectedConditions.presenceOfElementLocated( By.id( "com.sdex.deviceinfochanger:id/btn_apply" ) ) )
            .click();
            Thread.sleep(2000);
            wait.until( ExpectedConditions.presenceOfElementLocated( By.id( "android:id/button1" ) ) )
                    .click();
            Thread.sleep(1000);
        } catch ( NoSuchElementException | InterruptedException ex ){
            ex.printStackTrace();
        }
    }

    private void setUpDriver() {
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability( CapabilityType.BROWSER_NAME, "Android" );
            capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "ANDROID");
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);

            capabilities.setCapability( MobileCapabilityType.PLATFORM_VERSION, System.getProperty("androidVersion" ) );
            capabilities.setCapability( MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName" ) );
            capabilities.setCapability(MobileCapabilityType.UDID, System.getProperty("deviceId" )  );
            capabilities.setCapability( AndroidMobileCapabilityType.SYSTEM_PORT, System.getProperty("port" ));
            capabilities.setCapability( "appPackage", "com.sdex.deviceinfochanger" );
            capabilities.setCapability( "appActivity", "com.sdex.deviceinfochanger.MainActivity" );
            capabilities.setCapability( "newCommandTimeout", "30" );
            final String APPIUM_SERVER_URL = "http://0.0.0.0:4723/wd/hub";
            driver = new AndroidDriver<MobileElement>( new URL(APPIUM_SERVER_URL), capabilities);
            driver.manage().timeouts().implicitlyWait( 2, TimeUnit.MINUTES );
            if(System.getProperty("fullReset").equals( "true" ) )
                driver.resetApp();

        } catch ( MalformedURLException e ){
            System.out.println( "Could not create the android driver" );
            e.printStackTrace();
            end();
        }
    }

    private static void runCommandAndWaitToComplete( String[] command ) {
        String completeCommand = String.join(" ", command);
//        System.out.println("Executing command: " + completeCommand);
        String line;
        String returnValue = "";

        try {
            Process processCommand = Runtime.getRuntime().exec(command);
            BufferedReader response = new BufferedReader(new InputStreamReader(processCommand.getInputStream()));

            try {
                processCommand.waitFor();
            } catch (InterruptedException commandInterrupted) {
                System.out.println("Were waiting for process to end but something interrupted it" + commandInterrupted.getMessage());
            }

            while ((line = response.readLine()) != null) {
                returnValue = returnValue + line + "\n";
            }
            response.close();

        } catch (Exception e) {
            System.out.println("Unable to run command: " + completeCommand + ". Error: " + e.getMessage());
        }
//        System.out.println("Response : runCMDAndWaitToComplete(" + completeCommand + ") : " + returnValue);
    }

    private static void uninstallApps(String deviceId) {
        String uninstallCommand1[] = new String[]{"sh", "-c", String.format("adb -s %s uninstall io.appium.uiautomator2.server.test", deviceId)};
        runCommandAndWaitToComplete(uninstallCommand1);
        String uninstallCommand2[] = new String[]{"sh", "-c", String.format("adb -s %s uninstall io.appium.uiautomator2.server", deviceId)};
        runCommandAndWaitToComplete(uninstallCommand2);
        String uninstallCommand3[] = new String[]{"sh", "-c", String.format("adb -s %s uninstall io.appium.unlock", deviceId)};
        runCommandAndWaitToComplete(uninstallCommand3);
        String uninstallCommand4[] = new String[]{"sh", "-c", String.format("adb -s %s uninstall io.appium.settings", deviceId)};
        runCommandAndWaitToComplete(uninstallCommand4);
    }

    @AfterTest
    public void end() {
        uninstallApps( System.getProperty("deviceId" ) );
        driver.quit();
        System.out.println( "Stopped the hma driver" );
    }
}
