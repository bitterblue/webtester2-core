package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import info.novatec.testit.webtester.browser.Browser;
import info.novatec.testit.webtester.browser.WebDriverBrowser;
import info.novatec.testit.webtester.browser.BrowserFactory;
import info.novatec.testit.webtester.browser.proxy.ProxyConfiguration;
import info.novatec.testit.webtester.config.Configuration;
import info.novatec.testit.webtester.config.adapters.DefaultFileConfigurationAdapter;
import info.novatec.testit.webtester.config.adapters.LocalFileConfigurationAdapter;
import info.novatec.testit.webtester.config.builders.BaseConfigurationBuilder;


public class TestBrowserFactory implements BrowserFactory {

    @Override
    public Browser createBrowser() {
        // TODO: default to local FirefoxDriver, if no property specified
        String targetBrowserProperty = System.getProperty("test.targetBrowser", "firefox");
        if (TestTargetBrowser.CHROME.matches(targetBrowserProperty)) {
            return createRemoteChromeBrowser();
        } else if (TestTargetBrowser.FIREFOX.matches(targetBrowserProperty)) {
            return createRemoteFirefoxBrowser();
        } else {
            throw new IllegalArgumentException("Unknown target browser type: " + targetBrowserProperty);
        }
    }

    private Browser createLocalFirefoxBrowser() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setAcceptUntrustedCertificates(true);
        profile.setEnableNativeEvents(true);
        return createBrowser(new FirefoxDriver(profile));
    }

    private Browser createRemoteFirefoxBrowser() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        FirefoxProfile profile = new FirefoxProfile();
        profile.setAcceptUntrustedCertificates(true);
        profile.setEnableNativeEvents(true);
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        return createBrowser(new RemoteWebDriver(capabilities));
    }

    private Browser createRemoteChromeBrowser() {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        return createBrowser(new RemoteWebDriver(capabilities));
    }

    @Override
    public Browser createBrowser(DesiredCapabilities capabilities) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public Browser createBrowser(WebDriver webDriver) {
        Configuration config = new BaseConfigurationBuilder()
            .withAdapter(new DefaultFileConfigurationAdapter())
            .withAdapter(new LocalFileConfigurationAdapter())
            .build();
        Browser browser = WebDriverBrowser.forWebDriver(webDriver).withConfiguration(config).build();
        Runtime.getRuntime().addShutdownHook(new Thread(browser::close));
        return browser;
    }

    @Override
    public BrowserFactory withProxyConfiguration(ProxyConfiguration configuration) {
        // proxies are ignored for tests
        return this;
    }

    private enum TestTargetBrowser {
        FIREFOX,
        CHROME;

        public boolean matches(String target) {
            return this.name().toLowerCase().matches(target.toLowerCase());
        }
    }

}
