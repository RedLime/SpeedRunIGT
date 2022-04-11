package com.redlimerl.speedrunigt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SpeedRunIGTUpdateChecker {
    public enum UpdateStatus { NONE, UNKNOWN, UPDATED, OUTDATED }
    public static UpdateStatus UPDATE_STATUS = UpdateStatus.NONE;
    public static String UPDATE_URL = "";
    public static String UPDATE_VERSION = "0.0";

    public static void checkUpdate() {
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            return;
        }
        new Thread(() -> {
            try {
                URL u = new URL("https://api.github.com/repos/RedLime/SpeedRunIGT/releases");
                HttpURLConnection c = (HttpURLConnection) u.openConnection();

                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);

                InputStreamReader r = new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8);
                JsonElement jsonElement = new JsonParser().parse(r);
                if (jsonElement.getAsJsonArray().size() == 0) {
                    UPDATE_STATUS = UpdateStatus.UNKNOWN;
                } else {
                    for (JsonElement element : jsonElement.getAsJsonArray()) {
                        JsonObject versionData = element.getAsJsonObject();
                        if (!versionData.get("prerelease").getAsBoolean()) {
                            for (JsonElement asset : versionData.get("assets").getAsJsonArray()) {
                                JsonObject assetData = asset.getAsJsonObject();
                                String versionName = assetData.get("name").getAsString();
                                String targetVersionName = versionName.split("\\+")[0].split("-")[1];
                                String currentVersionName = SpeedRunIGT.MOD_VERSION.split("\\+")[0];
                                String currentMCVersionName = SpeedRunIGT.MOD_VERSION.split("\\+")[1];
                                if (versionName.endsWith(currentMCVersionName + ".jar") &&
                                        isOutdatedVersion(targetVersionName, currentVersionName) && isOutdatedVersion(targetVersionName, UPDATE_VERSION)) {
                                    UPDATE_STATUS = UpdateStatus.OUTDATED;
                                    UPDATE_URL = assetData.get("browser_download_url").getAsString();
                                    UPDATE_VERSION = assetData.get("name").getAsString().split("\\+")[0].split("-")[1];
                                }
                            }
                        }
                    }

                    if (UPDATE_STATUS == UpdateStatus.NONE) {
                        UPDATE_STATUS = UpdateStatus.UPDATED;
                    }
                }
            } catch (Exception e) {
                UPDATE_STATUS = UpdateStatus.UNKNOWN;
            }
        }).start();
    }

    private static boolean isOutdatedVersion(String foundVersion, String currentVersion) throws VersionParsingException {
        return VersionPredicateParser.parse("<"+foundVersion).test(SemanticVersion.parse(currentVersion));
    }
}
