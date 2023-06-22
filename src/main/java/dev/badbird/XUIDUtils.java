package dev.badbird;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class XUIDUtils {
    private static final  OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
    public static CompletableFuture<Long> getXUID(String gamertag) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder().url("https://api.geysermc.org/v2/xbox/xuid/" + gamertag).build();
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
            return Long.parseLong(result.split(":")[1].replace("}", "")); // parsing json is for nerds
        });
    }

    public static CompletableFuture<String> getGamerTag(long xuid) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url("https://api.geysermc.org/v2/xbox/gamertag/" + xuid).build();
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
            String s = result.split(":")[1].replace("}", "");
            return s.substring(1, s.length() - 1);
        });
    }
}
