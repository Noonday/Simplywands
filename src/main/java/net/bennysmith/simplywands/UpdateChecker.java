package net.bennysmith.simplywands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@EventBusSubscriber(modid = "simplywands", bus = EventBusSubscriber.Bus.GAME)
public class UpdateChecker {
    private static final String CURSEFORGE_API_URL = "https://api.curseforge.com/v1/mods/simplywands/files";
    private static final String CURRENT_VERSION = "1.1";
    private static final String API_KEY = loadApiKey();
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Check if it's enabled or disabled
        if (!Config.enableUpdateChecker) {
            return;
        }

        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            return;
        }

        new Thread(() -> {
            try {
                String latestVersion = getLatestVersion();
                if (isUpdateAvailable(latestVersion)) {
                    player.sendSystemMessage(Component.literal("An update is available for Simply Wands: " + latestVersion));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to check for updates", e);
            }
        }).start();
    }

    private static String getLatestVersion() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CURSEFORGE_API_URL)
                .addHeader("X-API-Key", API_KEY)
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            return jsonObject.getAsJsonArray("data").get(0).getAsJsonObject().get("fileName").getAsString();
        }
    }

    private static boolean isUpdateAvailable(String latestVersion) {
        return !latestVersion.equals(CURRENT_VERSION);
    }

    private static String loadApiKey() {
        try (InputStream input = UpdateChecker.class.getClassLoader().getResourceAsStream("apikey.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new IOException("Unable to find apikey.properties");
            }
            prop.load(input);
            return prop.getProperty("curseforge.apikey");
        } catch (IOException ex) {
            LOGGER.error("Failed to load API key from properties file", ex);
            return null;
        }
    }
}
