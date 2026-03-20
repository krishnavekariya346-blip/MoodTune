package com.moodtune.app.music.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.moodtune.app.R;
import com.moodtune.app.models.Song;

public class AudiusApiService {

    private static final String TAG = "AudiusApiService";
    private static final String DISCOVERY_ENDPOINT = "https://api.audius.co";
    private static final int DEFAULT_LIMIT = 40;
    private static volatile String cachedDiscoveryHost;

    public interface TracksCallback {
        void onSuccess(List<Song> songs);
        void onError(String error);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String appName;

    public AudiusApiService(Context context) {
        this.appName = context.getString(R.string.audius_app_name);
    }

    public void fetchTracksForMood(String mood, TracksCallback callback) {
        if (TextUtils.isEmpty(appName) || appName.startsWith("YOUR_")) {
            postError(callback, "Audius app name missing. Set 'audius_app_name' in strings.xml");
            return;
        }

        executor.execute(() -> {
            try {
                String host = ensureDiscoveryHost();
                if (host == null) {
                    postError(callback, "Unable to reach Audius discovery nodes");
                    return;
                }

                List<String> candidateUrls = buildSearchUrls(host, mood);
                List<Song> songs = new ArrayList<>();
                List<String> errors = new ArrayList<>();

                for (String url : candidateUrls) {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);

                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            errors.add("HTTP " + responseCode + " for " + url);
                            continue;
                        }

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject json = new JSONObject(response.toString());
                        JSONArray data = json.optJSONArray("data");
                        if (data == null || data.length() == 0) {
                            errors.add("Empty result for " + url);
                            continue;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String trackId = item.optString("id");
                            String title = item.optString("title", "Unknown Title").trim();
                            JSONObject artistObj = item.optJSONObject("user");
                            String artist = artistObj != null ? artistObj.optString("name", "Unknown Artist") : "Unknown Artist";
                            JSONObject artwork = item.optJSONObject("artwork");
                            String artworkUrl = artwork != null ? artwork.optString("1000x1000", artwork.optString("480x480", "")) : "";
                            int duration = item.optInt("duration", 0);
                            boolean streamable = item.optBoolean("is_streamable", true);

                            if (TextUtils.isEmpty(trackId) || !streamable) {
                                continue;
                            }

                            String streamUrl = host + "/v1/tracks/" + trackId + "/stream?app_name=" + URLEncoder.encode(appName, StandardCharsets.UTF_8.name());

                            songs.add(new Song(
                                    trackId,
                                    title,
                                    artist,
                                    artworkUrl,
                                    duration,
                                    streamUrl
                            ));
                        }

                        if (!songs.isEmpty()) {
                            break;
                        }
                    } catch (Exception inner) {
                        errors.add(inner.getMessage());
                    }
                }

                if (songs.isEmpty()) {
                    String combinedError = errors.isEmpty()
                            ? "Audius returned no playable tracks for mood: " + mood
                            : String.join(" | ", errors);
                    postError(callback, combinedError);
                } else {
                    mainHandler.post(() -> callback.onSuccess(songs));
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch Audius tracks", e);
                postError(callback, "Failed to load Audius tracks: " + e.getMessage());
            }
        });
    }

    private void postError(TracksCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }

    private String ensureDiscoveryHost() {
        if (!TextUtils.isEmpty(cachedDiscoveryHost)) {
            return cachedDiscoveryHost;
        }
        synchronized (AudiusApiService.class) {
            if (!TextUtils.isEmpty(cachedDiscoveryHost)) {
                return cachedDiscoveryHost;
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(DISCOVERY_ENDPOINT).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Discovery endpoint failed: " + responseCode);
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray data = json.optJSONArray("data");
                if (data != null && data.length() > 0) {
                    cachedDiscoveryHost = data.getString(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch discovery host", e);
                return null;
            }
        }
        return cachedDiscoveryHost;
    }

    private List<String> buildSearchUrls(String host, String mood) throws Exception {
        String normalizedMood = mood == null ? "" : mood.toLowerCase(Locale.US);

        Map<String, String> moodQueries = new HashMap<>();
        moodQueries.put("happy", "bollywood hindi party");
        moodQueries.put("sad", "bollywood hindi romantic");
        moodQueries.put("angry", "punjabi rap energetic");
        moodQueries.put("neutral", "hindi lofi chill");
        moodQueries.put("surprise", "bollywood remix mashup");

        String query = moodQueries.getOrDefault(normalizedMood, "bollywood hindi mix");

        List<String> urls = new ArrayList<>();

        urls.add(host + "/v1/tracks/search?query=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8.name()) +
                "&limit=" + DEFAULT_LIMIT +
                "&app_name=" + URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) +
                "&only_downloadable=false");

        urls.add(host + "/v1/tracks/trending?time=week&genre=international&limit=" +
                DEFAULT_LIMIT + "&app_name=" + URLEncoder.encode(appName, StandardCharsets.UTF_8.name()));

        urls.add(host + "/v1/tracks/trending?time=week&limit=" +
                DEFAULT_LIMIT + "&app_name=" + URLEncoder.encode(appName, StandardCharsets.UTF_8.name()));

        urls.add(host + "/v1/tracks/trending?time=month&limit=" +
                DEFAULT_LIMIT + "&app_name=" + URLEncoder.encode(appName, StandardCharsets.UTF_8.name()));

        return urls;
    }
}

