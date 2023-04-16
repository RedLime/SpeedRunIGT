package com.redlimerl.speedrunigt.therun;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunType;
import org.apache.commons.io.IOUtils;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TheRunRequestHelper {


    private static final String KEY_BASE_URL = "https://therun.gg/api/livesplit";
    private static final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    private static final HashMap<TheRunTimer.PacketType, Long> LEAST_REQ_TIME = Maps.newHashMap();

    private static void setupConnection(HttpURLConnection connection) {
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "SpeedRunIGT/"+SpeedRunIGT.MOD_VERSION);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Sec-Fetch-Site", "cross-site");
        connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        connection.setRequestProperty("Content-Disposition", "attachment");
    }

    public static void updateTimerData(InGameTimer timer, TheRunTimer.PacketType packetType) {

        // Skip these all things lol
        if (!timer.isStarted() || timer.isCoop() || timer.isOpenedIntegratedServer() || !SpeedRunIGT.IS_CLIENT_SIDE
            || TheRunKeyHelper.UPLOAD_KEY.isEmpty() || timer.getCategory().getTheRunCategory() == null
            || !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE) || timer.isRTAMode() || timer.getRunType() == RunType.OLD_WORLD
            || timer.isCheatAvailable() || timer.getDefaultGameMode() != 0) {
            return;
        }

        if (System.currentTimeMillis() - LEAST_REQ_TIME.getOrDefault(packetType, 0L) < 100) return;
        LEAST_REQ_TIME.put(packetType, System.currentTimeMillis());

        TheRunTimer theRunTimer = new TheRunTimer(timer);
        JsonObject payloadData = theRunTimer.convertJson(packetType);
        if (payloadData == null) return;

        threadExecutor.submit(() -> {
            try {
                URL url = new URL(KEY_BASE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                setupConnection(connection);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(payloadData.toString());
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
            } catch (SocketTimeoutException e) {
                SpeedRunIGT.error("Failed to upload timer data on therun.gg cause by timeout");
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to upload timer data on therun.gg");
            }
        });

    }


    public static boolean checkValidUploadKey(String key) {
        try {
            if (System.currentTimeMillis() - LEAST_REQ_TIME.getOrDefault(TheRunTimer.PacketType.RESET, 0L) < 100) return false;
            LEAST_REQ_TIME.put(TheRunTimer.PacketType.RESET, System.currentTimeMillis());

            URL url = new URL("https://2uxp372ks6nwrjnk6t7lqov4zu0solno.lambda-url.eu-west-1.on.aws/?filename=Minecraft%3A+Java+Edition-any%25glitchless.lss&uploadKey=" + key);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            setupConnection(connection);

            connection.connect();

            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static void submitTimerData(InGameTimer timer) {
        // Skip these all things lol
        if (!timer.isStarted() || timer.isCoop() || timer.isOpenedIntegratedServer() || !SpeedRunIGT.IS_CLIENT_SIDE
                || TheRunKeyHelper.UPLOAD_KEY.isEmpty() || timer.getCategory().getTheRunCategory() == null
                || !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE)) {
            return;
        }

        threadExecutor.submit(() -> {
            try {
                TheRunTimer theRunTimer = new TheRunTimer(timer);

                DOMSource xmlData = theRunTimer.convertXml();
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer trans = tf.newTransformer();
                StringWriter sw = new StringWriter();
                trans.transform(xmlData, new StreamResult(sw));
                String resultXml = sw.toString();
                SpeedRunIGT.debug(resultXml);

                URL url = new URL("https://2uxp372ks6nwrjnk6t7lqov4zu0solno.lambda-url.eu-west-1.on.aws/?filename=" +
                        URLEncoder.encode(timer.getCategory().getTheRunCategory().getGameName(), "UTF-8") + "-" +
                        URLEncoder.encode(timer.getCategory().getTheRunCategory().getCategoryName(timer).trim().toLowerCase(Locale.ROOT), "UTF-8") + ".lss&uploadKey=" + TheRunKeyHelper.UPLOAD_KEY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                setupConnection(connection);

                InputStreamReader r = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                String targetUrl = new JsonParser().parse(r).getAsJsonObject().get("url").getAsString();
                SpeedRunIGT.debug(targetUrl);

                URL submitUrl = new URL(targetUrl);
                HttpURLConnection submitConnection = (HttpURLConnection) submitUrl.openConnection();
                setupConnection(submitConnection);
                submitConnection.setRequestMethod("PUT");
                submitConnection.setDoInput(true);
                submitConnection.setDoOutput(true);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(submitConnection.getOutputStream()));
                bw.write(resultXml);
                bw.flush();
                bw.close();

                int result = submitConnection.getResponseCode();
                SpeedRunIGT.debug("therun.gg submit status > " + result);
                InputStream in;
                if (result >= 200 && result < 300) {
                    in = submitConnection.getInputStream();
                    SpeedRunIGT.debug(IOUtils.toString(in, StandardCharsets.UTF_8));
                } else {
                    in = submitConnection.getErrorStream();
                    SpeedRunIGT.error(IOUtils.toString(in, StandardCharsets.UTF_8));
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
