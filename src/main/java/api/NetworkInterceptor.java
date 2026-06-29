package api;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkInterceptor {
    private final Page page;

    public NetworkInterceptor(Page page) {this.page = page;}

    public Response interceptResponse(String urlPattern, Runnable action) {
        Response interceptedResponse = page.waitForResponse(
                response -> {
                    String url = response.url();
                    return url.contains(urlPattern);
                },
                action
        );
        return interceptedResponse;
    }
}
