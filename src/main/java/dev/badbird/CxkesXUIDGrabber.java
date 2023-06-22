package dev.badbird;

import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CxkesXUIDGrabber {
    public static boolean useGeyser = true;
    private static final String baseUrl = "https://www.cxkes.me/xbox/xuid";
    private static final OkHttpClient client = new OkHttpClient().newBuilder().build();
    private static final Pattern csrfTokenPattern = Pattern.compile("<input type=\"hidden\" name=\"_token\" value=\"(.*?)\">");
    private static final Pattern xuidPattern = Pattern.compile("<strong>XUID \\(DEC\\)<\\/strong>: (.*?)<br \\/>");

    public static CompletableFuture<Long> getXUID(String gamertag) {
        return getCsrfTokenData().thenApplyAsync(csrfToken -> {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "_token=" + csrfToken.csrfToken + "&gamertag=" + gamertag);
            Request request = new Request.Builder()
                    .url("https://www.cxkes.me/xbox/xuid")
                    .method("POST", body)
                    .addHeader("cookie", "laravel_session=" + csrfToken.laravelCookie)
                    .addHeader("authority", "www.cxkes.me")
                    .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("accept-language", "en-US,en;q=0.9")
                    .addHeader("cache-control", "max-age=0")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("origin", "https://www.cxkes.me")
                    .addHeader("referer", "https://www.cxkes.me/xbox/xuid")
                    .addHeader("sec-ch-ua", "\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Chrome\";v=\"114\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("sec-fetch-dest", "document")
                    .addHeader("sec-fetch-mode", "navigate")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-fetch-user", "?1")
                    .addHeader("sec-gpc", "1")
                    .addHeader("upgrade-insecure-requests", "1")
                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String result = null;
            try {
                result = response.body().string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // System.out.println(result);
            Matcher matcher = xuidPattern.matcher(result);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
            return null;
        });
    }

    public static class CsrfTokenData {
        public String csrfToken;
        public String laravelCookie;

        public CsrfTokenData(String csrfToken, String laravelCookie) {
            this.csrfToken = csrfToken;
            this.laravelCookie = laravelCookie;
        }
    }

    private static CompletableFuture<CsrfTokenData> getCsrfTokenData() {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .addHeader("authority", "www.cxkes.me")
                    .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("accept-language", "en-US,en;q=0.9")
                    .addHeader("cache-control", "max-age=0")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("origin", "https://www.cxkes.me")
                    .addHeader("referer", "https://www.cxkes.me/xbox/xuid")
                    .addHeader("sec-ch-ua", "\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Chrome\";v=\"114\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("sec-fetch-dest", "document")
                    .addHeader("sec-fetch-mode", "navigate")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-fetch-user", "?1")
                    .addHeader("sec-gpc", "1")
                    .addHeader("upgrade-insecure-requests", "1")
                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                // System.out.println(result);
                Matcher matcher = csrfTokenPattern.matcher(result);
                if (matcher.find()) {
                    List<String> cookies = response.headers("Set-Cookie");
                    String laravelCookie = cookies.stream().filter(s -> s.startsWith("laravel_session=")).findFirst().orElseThrow(() -> new RuntimeException("No laravel_session cookie found!"))
                            .split(";")[0].split("=")[1];
                    return new CsrfTokenData(matcher.group(1), laravelCookie);
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
