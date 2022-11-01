package com.redlimerl.speedrunigt.therun;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
            || timer.isGlitched() || TheRunKeyHelper.UPLOAD_KEY.isEmpty() || timer.getCategory().getTheRunCategory() == null
            || !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE)) {
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


    public static boolean checkValidUploadKey(String key) {
        try {
            URL url = new URL("https://2uxp372ks6nwrjnk6t7lqov4zu0solno.lambda-url.eu-west-1.on.aws/?filename=Minecraft%3A+Java+Edition-any%25glitchless.lss&uploadKey=" + key);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            setupConnection(connection);

            connection.connect();

            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

}
