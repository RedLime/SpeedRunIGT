package com.redlimerl.speedrunigt.therun;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TheRunRequestHelper {


    private static final String KEY_BASE_URL = "https://therun.gg/api/livesplit";
    private static final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    private static void setupConnection(HttpURLConnection connection) {
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "SpeedRunIGT/"+SpeedRunIGT.MOD_VERSION);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Sec-Fetch-Site", "cross-site");
        connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
    }


    public static void updateTimerData(InGameTimer timer) {

        // Skip these all things lol
        if (!timer.isStarted() || timer.isCoop() || timer.isOpenedIntegratedServer() || !SpeedRunIGT.IS_CLIENT_SIDE
            || timer.isGlitched() || TheRunKeyHelper.UPLOAD_KEY.isEmpty() || timer.getCategory().getTheRunCategory() == null) {
            return;
        }

        TheRunTimer theRunTimer = new TheRunTimer(timer);

        threadExecutor.submit(() -> {
            try {
                URL url = new URL(KEY_BASE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                setupConnection(connection);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(theRunTimer.convertJson().toString());
                FileUtils.writeStringToFile(new File("C:/Temp/MyTest2.txt"), theRunTimer.convertJson().toString(), StandardCharsets.UTF_8);
                bw.flush();
                bw.close();

                int result = connection.getResponseCode();
                SpeedRunIGT.debug("therun.gg status > " + result);
                InputStream in;
                if (result >= 200 && result < 300) {
                    in = connection.getInputStream();
                    SpeedRunIGT.debug(IOUtils.toString(in, StandardCharsets.UTF_8));
                } else {
                    in = connection.getErrorStream();
                    SpeedRunIGT.error(IOUtils.toString(in, StandardCharsets.UTF_8));
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to upload timer data on therun.gg");
            }
        });

    }


    public static void uploadTimerData(InGameTimer timer) {

        // Skip these all things lol
        if (timer.getStatus() == TimerStatus.NONE || timer.isCoop() || timer.isOpenedIntegratedServer() || !SpeedRunIGT.IS_CLIENT_SIDE
                || timer.isGlitched() || timer.isCompleted() || timer.getCategory() != RunCategories.ANY || timer.getRunType() != RunType.RANDOM_SEED
                || TheRunKeyHelper.UPLOAD_KEY.isEmpty() || timer.getCategory().getTheRunCategory() == null) {
            return;
        }



        TheRunTimer theRunTimer = new TheRunTimer(timer);
        TheRunCategory theRunCategory = timer.getCategory().getTheRunCategory();

        threadExecutor.submit(() -> {
            try {
                URL url = new URL("https://2uxp372ks6nwrjnk6t7lqov4zu0solno.lambda-url.eu-west-1.on.aws/?filename=" + (URLEncoder.encode(theRunCategory.getGameName(), "UTF-8") + "-" + URLEncoder.encode(theRunCategory.getCategoryName(), "UTF-8") + ".lss") + "&uploadKey=" + TheRunKeyHelper.UPLOAD_KEY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                setupConnection(connection);

                connection.connect();

                InputStreamReader r = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                JsonElement jsonElement = new JsonParser().parse(r);

                r.close();

                String targetUrl = jsonElement.getAsJsonObject().get("url").getAsString();
                SpeedRunIGT.debug(targetUrl);
                URL newUrl = new URL(targetUrl);
                HttpURLConnection newConnection = (HttpURLConnection) newUrl.openConnection();
                newConnection.setRequestMethod("PUT");
                newConnection.setDoInput(true);
                newConnection.setDoOutput(true);
                setupConnection(newConnection);

                newConnection.connect();

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(newConnection.getOutputStream()));
                bw.write(SpeedRunIGT.GSON.toJson(theRunTimer.convertJson()));
                bw.close();

                int result = newConnection.getResponseCode();
                SpeedRunIGT.debug("therun.gg status > " + result);
                InputStream in;
                if (result >= 200 && result < 300) {
                    in = newConnection.getInputStream();
                    SpeedRunIGT.debug(IOUtils.toString(in, StandardCharsets.UTF_8));
                } else {
                    in = newConnection.getErrorStream();
                    SpeedRunIGT.error(IOUtils.toString(in, StandardCharsets.UTF_8));
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to upload timer data on therun.gg");
            }
        });
    }

}
