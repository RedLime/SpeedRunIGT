package com.redlimerl.speedrunigt.paceman;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacemanRequestHelper {
    private static final String KEY_BASE_URL = "http://paceman.gg/api/submitrun";
    private static final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    private static void setupConnection(HttpURLConnection connection) {
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "SpeedRunIGT/"+ SpeedRunIGT.MOD_VERSION);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    }

    public static void updateTimerData(InGameTimer timer, boolean reset) {

        // Skip these all things lol
        if (!timer.isStarted() || timer.isOpenedIntegratedServer() || !SpeedRunIGT.IS_CLIENT_SIDE
                || timer.isRTAMode() || timer.getRunType() == RunType.OLD_WORLD
                || !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PACEMAN_GG_LIVE)
                || timer.isCheatAvailable() || timer.getDefaultGameMode() != 0) {
            return;
        }

        JsonObject payloadData = convertJson(timer, reset);

        threadExecutor.submit(() -> {
            try {
                URL url = new URL(KEY_BASE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                setupConnection(connection);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(SpeedRunIGT.GSON.toJson(payloadData));
                bw.flush();
                bw.close();

                int result = connection.getResponseCode();
                SpeedRunIGT.debug("paceman.gg status > " + result);
                InputStream in;
                if (result >= 200 && result < 300) {
                    in = connection.getInputStream();
                    SpeedRunIGT.debug(IOUtils.toString(in, StandardCharsets.UTF_8));
                } else {
                    in = connection.getErrorStream();
                    SpeedRunIGT.error(IOUtils.toString(in, StandardCharsets.UTF_8));
                }
                in.close();
            } catch (SocketTimeoutException e) {
                SpeedRunIGT.error("Failed to upload timer data on therun.gg cause by timeout");
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to upload timer data on therun.gg");
            }
        });

    }

    private static JsonObject convertJson(InGameTimer timer, boolean reset) {
        JsonObject result = new JsonObject();

        if (!reset) {
            result.add("record", InGameTimerUtils.convertTimelineJson(timer));
        }

        JsonArray nicknames = new JsonArray();
        JsonArray uuids = new JsonArray();
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            for (PlayerListEntry playerListEntry : MinecraftClient.getInstance().getNetworkHandler().getPlayerList()) {
                nicknames.add(playerListEntry.getProfile().getName());
                uuids.add(playerListEntry.getProfile().getId().toString());
            }
        }
        result.add("nicknames", nicknames);
        result.add("uuids", uuids);

        JsonArray mods = new JsonArray();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() == ModOrigin.Kind.PATH) {
                mods.add(mod.getMetadata().getId());
            }
        }
        result.add("modlist", mods);

        JsonArray twitch = new JsonArray();
        JsonArray alt = new JsonArray();
        result.add("twitch", twitch);
        result.add("alt", alt);

        System.out.println(SpeedRunIGT.PRETTY_GSON.toJson(result));

        return result;
    }

}
