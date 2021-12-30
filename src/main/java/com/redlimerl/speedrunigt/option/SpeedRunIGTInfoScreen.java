package com.redlimerl.speedrunigt.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SpeedRunIGTInfoScreen extends Screen {

    enum UpdateStatus { NONE, UNKNOWN, UPDATED, OUTDATED }
    static UpdateStatus UPDATE_STATUS = UpdateStatus.NONE;
    static String UPDATE_URL = "";
    static String UPDATE_VERSION = "";

    private final Screen parent;

    private ButtonWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        checkUpdate();
        assert client != null;
        update = new ButtonWidget(8001, width / 2 - 155, height - 104, 150, 20, SpeedRunIGT.translate("speedrunigt.menu.download_update", "Download New Update").getString());
        update.active = false;
        buttons.add(update);
        buttons.add(new ButtonWidget(8002, width / 2 + 5, height - 104, 150, 20, SpeedRunIGT.translate("speedrunigt.menu.latest_change_log", "Check Latest Changelog").getString()));

        buttons.add(new ButtonWidget(8003, width / 2 - 155, height - 80, 150, 20, SpeedRunIGT.translate("speedrunigt.menu.open_github_repo", "Open Github Repository").getString()));
        buttons.add(new ButtonWidget(8004, width / 2 + 5, height - 80, 150, 20, SpeedRunIGT.translate("speedrunigt.menu.open_support_page", "Support to me!").getString()));
        buttons.add(new ButtonWidget(8005, width / 2 - 100, height - 40, 200, 20, ScreenTexts.BACK));
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 8001) {
            setUrl(UPDATE_URL);
        } else if (button.id == 8002) {
            setUrl("https://github.com/RedLime/SpeedRunIGT/releases/latest");
        } else if (button.id == 8003) {
            setUrl("https://github.com/RedLime/SpeedRunIGT/");
        } else if (button.id == 8004) {
            setUrl("https://ko-fi.com/redlimerl");
        } else if (button.id == 8005) {
            client.openScreen(parent);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.5F, 1.5F, 1.5F);
        drawCenteredString(this.textRenderer, "SpeedRunIGT", this.width / 3, 15, 16777215);
        GlStateManager.popMatrix();
        drawCenteredString(this.textRenderer, "Made by RedLime", this.width / 2, 50, 16777215);
        drawCenteredString(this.textRenderer, "Discord : RedLime#0817", this.width / 2, 62, 16777215);
        drawCenteredString(this.textRenderer, "Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0], this.width / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                drawCenteredString(this.textRenderer, "§eUpdated Version : "+ UPDATE_VERSION, this.width / 2, 88, 16777215);
            }
            Text text;
            if (UPDATE_STATUS == UpdateStatus.UNKNOWN) {
                text = SpeedRunIGT.translate("speedrunigt.message.update.unknown", "Failed to get latest version of information.");
            } else if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                text = SpeedRunIGT.translate("speedrunigt.message.update.outdated", "A new version has been found.");
            } else {
                text = SpeedRunIGT.translate("speedrunigt.message.update.updated", "You are using the latest version.");
            }
            drawCenteredString(this.textRenderer, text.getString(), this.width / 2, 116, 16777215);
        }

        super.render(mouseX, mouseY, delta);
    }



    static void checkUpdate() {
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
                        String target = versionData.get("tag_name").getAsString().split("\\+")[0];
                        if (compareVersion(SpeedRunIGT.MOD_VERSION.split("\\+")[0], target) <= 0 && !versionData.get("prerelease").getAsBoolean()) {
                            for (JsonElement asset : versionData.get("assets").getAsJsonArray()) {
                                JsonObject assetData = asset.getAsJsonObject();
                                String versionName = assetData.get("name").getAsString();
                                if (versionName.endsWith(SpeedRunIGT.MOD_VERSION.split("\\+")[1] + ".jar") &&
                                        compareVersion(versionName.split("\\+")[0].split("-")[1], SpeedRunIGT.MOD_VERSION.split("\\+")[0]) > 0) {
                                    UPDATE_STATUS = UpdateStatus.OUTDATED;
                                    UPDATE_URL = assetData.get("browser_download_url").getAsString();
                                    UPDATE_VERSION = assetData.get("name").getAsString().split("\\+")[0].split("-")[1];
                                    break;
                                }
                            }
                        }
                    }

                    if (UPDATE_STATUS == UpdateStatus.NONE) {
                        UPDATE_STATUS = UpdateStatus.UPDATED;
                    }
                }
            } catch (IOException e) {
                UPDATE_STATUS = UpdateStatus.UNKNOWN;
            }
        }).start();
    }

    public static int compareVersion(String left, String right) {
        if (left.equals(right)) {
            return 0;
        }
        int leftStart = 0, rightStart = 0, result;
        do {
            int leftEnd = left.indexOf('.', leftStart);
            int rightEnd = right.indexOf('.', rightStart);
            Integer leftValue = Integer.parseInt(leftEnd < 0
                    ? left.substring(leftStart)
                    : left.substring(leftStart, leftEnd));
            Integer rightValue = Integer.parseInt(rightEnd < 0
                    ? right.substring(rightStart)
                    : right.substring(rightStart, rightEnd));
            result = leftValue.compareTo(rightValue);
            leftStart = leftEnd + 1;
            rightStart = rightEnd + 1;
        } while (result == 0 && leftStart > 0 && rightStart > 0);
        if (result == 0) {
            if (leftStart > rightStart) {
                return containsNonZeroValue(left, leftStart) ? 1 : 0;
            }
            if (leftStart < rightStart) {
                return containsNonZeroValue(right, rightStart) ? -1 : 0;
            }
        }
        return result;
    }
    private static boolean containsNonZeroValue(String str, int beginIndex) {
        for (int i = beginIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != '0' && c != '.') {
                return true;
            }
        }
        return false;
    }


    private void setUrl(String url) {
        try {
            Class<?> class_ = Class.forName("java.awt.Desktop");
            Object object = class_.getMethod("getDesktop").invoke(null);
            class_.getMethod("browse", URI.class).invoke(object, new URI(url));
        } catch (Throwable var4) {
            var4.printStackTrace();
        }
    }
}
