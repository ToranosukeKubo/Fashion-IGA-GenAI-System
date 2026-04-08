package com.example.stabilityai;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.stream.Collectors;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

public class StabilityAIImageGenerator {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("STABILITY_AI_API_KEY");
    private static final String API_URL_BASE = "https://api.stability.ai/v1/generation/";
    private static final String ENGINE_ID = "stable-diffusion-xl-1024-v1-0";

    /**
     * 指定されたプロンプトに基づいて画像を生成し、指定されたファイル名で保存します。
     * 成功した場合は true、失敗した場合は false を返します。
     *
     * @param prompt                 画像生成のためのテキストプロンプト
     * @param seed                     画像生成のランダム性を決定するためのシード値
     * @param outputPath 保存する画像のパスとファイル名 (例: "images/fashion_gen1_0.png")
     * @return 画像生成と保存が成功した場合は true、それ以外の場合は false
     */
    // ★修正: int seed を引数に追加
    public static boolean generateImage(String prompt, int seed, String outputPath) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("エラー: STABILITY_AI_API_KEY が設定されていません。");
            return false;
        }

        int retries = 10;
        int initialDelayMs = 2000;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                URI uri = new URI(API_URL_BASE + ENGINE_ID + "/text-to-image");
                URL url = uri.toURL();

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject data = new JSONObject();
                JSONArray textPrompts = new JSONArray();
                JSONObject promptObject = new JSONObject();
                promptObject.put("text", prompt);
                promptObject.put("weight", 1.0);
                textPrompts.put(promptObject);
                data.put("text_prompts", textPrompts);

                // ★★★ 追加: ネガティブプロンプト ★★★
                JSONArray negativePrompts = new JSONArray();
                negativePrompts.put(new JSONObject().put("text",
                        "multiple views, multiple angles, collage, grid, watermark, text, signature, blurry, low quality, bad anatomy, deformed, extra limbs, ugly, disfigured"));
                data.put("negative_prompts", negativePrompts);

                data.put("cfg_scale", 7);
                data.put("clip_guidance_preset", "FAST_BLUE");
                data.put("height", 1024);
                data.put("width", 1024);
                data.put("samples", 1);
                data.put("steps", 30);

                data.put("seed", seed);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = data.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray artifacts = jsonResponse.getJSONArray("artifacts");

                    if (artifacts.length() > 0) {
                        JSONObject firstArtifact = artifacts.getJSONObject(0);
                        String base64Image = firstArtifact.getString("base64");

                        File outputFile = new File(outputPath);
                        File parentDir = outputFile.getParentFile();
                        if (parentDir != null && !parentDir.exists()) {
                            boolean created = parentDir.mkdirs();
                            if (!created) {
                                System.err.println("ディレクトリ作成失敗: " + parentDir.getAbsolutePath());
                                return false;
                            }
                        }

                        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(decodedBytes);
                        }
                        System.out.println("画像保存成功 (Stability AI): " + outputPath);
                        return true;
                    } else {
                        System.err.println("Stability AIから画像データが返されませんでした。");
                        return false;
                    }

                } else if (responseCode == 429) {
                    System.err.println("429エラー (Stability AI): レート制限に達しました。リトライ中（" + attempt + "回目）...");
                    long sleepTime = (long) (initialDelayMs * Math.pow(2, attempt - 1));
                    System.err.println("待機時間: " + sleepTime + "ms");
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("スレッドが中断されました。");
                        return false;
                    }
                } else {
                    System.err.println("Stability AI APIエラー (HTTP " + responseCode + "): " + conn.getResponseMessage());
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                        String errorResponse = br.lines().collect(Collectors.joining("\n"));
                        System.err.println("エラー詳細: " + errorResponse);
                    }
                    return false;
                }
            } catch (URISyntaxException e) {
                System.err.println("URL構文エラー (Stability AI): " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (MalformedURLException e) {
                System.err.println("URL変換エラー (Stability AI): " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                System.err.println("Stability AI画像生成中にIOエラーが発生しました: " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                System.err.println("Stability AI画像生成中に予期せぬエラーが発生しました: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        System.err.println("Stability AI画像生成失敗: " + outputPath);
        return false;
    }
}