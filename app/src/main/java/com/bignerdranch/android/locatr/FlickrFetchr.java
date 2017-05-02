package com.bignerdranch.android.locatr;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 3/25/2017.
 */

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";

    // Flickr API key (Required in all calls)
    private static final String API_KEY = "b08e9a2cc2bc5e50fd13f805d94f7320";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            // Otherwise everything is working, handle input stream
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(int pageNumber) {
        String url = buildUrl(FETCH_RECENTS_METHOD, null, pageNumber);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query, int pageNumber) {
        String url = buildUrl(SEARCH_METHOD, query, pageNumber);
        return downloadGalleryItems(url);
    }

    // Overloaded method for location search
    public List<GalleryItem> searchPhotos(Location location) {
        String url = buildUrl(location);
        return downloadGalleryItems(url);
    }

    private String buildUrl(String method, String query, int pageNumber) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("page", String.valueOf(pageNumber));

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    // Overloaded method for building using location
    private String buildUrl(Location location) {
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("lat", "" + location.getLatitude())
                .appendQueryParameter("lon", "" + location.getLongitude())
                .build().toString();
    }

    private List<GalleryItem> downloadGalleryItems(String url) {

        List<GalleryItem> items = new ArrayList<>();

        try {
            Log.i(TAG, "downloadGalleryItems: URL = " + url);

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString);

            items = parseItems(jsonBody);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return items;
    }

    // Parses the JSON object into a list of GalleryItems
    private List<GalleryItem> parseItems(JSONObject jsonBody)
            throws IOException, JSONException {

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        Type collectionType = new TypeToken<List<GalleryItem>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(photoJsonArray.toString(), collectionType);
    }
}
