package utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import static config.ConfigurationManager.config;

public enum BrowserFactory {
    CHROME {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.chromium().launch(options());
        }

    },

    CHROME_SYSTEM {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.chromium().launch(options().setChannel("chrome"));
        }
    },

    FIREFOX {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.firefox().launch(options());
        }

    },

    WEBKIT {
        @Override
        public Browser createInstance(final Playwright playwright) {
            return playwright.webkit().launch(options());
        }

    };

    public BrowserType.LaunchOptions options() {
        return new BrowserType.LaunchOptions()
                .setHeadless(config().headless())
                .setSlowMo(config().slowMotion());
    }

    private BrowserType.LaunchOptions systemBrowserOptions(String channel) {
        return options()
                .setChannel(channel);
    }

    public abstract Browser createInstance(final Playwright playwright);
}
