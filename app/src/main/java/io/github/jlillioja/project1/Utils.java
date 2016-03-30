package io.github.jlillioja.project1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacob on 3/3/2016.
 */
public class Utils {

    /* Static method to allow loading of image into an ImageView given a JSONObject movie. Used on details screen without the class. */
    public static Uri loadImage(ImageView imageView, JSONObject movie, Context context) throws JSONException {
        String imagePath = movie.getString(context.getString(R.string.poster_key));
        Uri imageURL = Uri.parse(context.getString(R.string.tmdb_image_path)).buildUpon()
                .appendPath(context.getString(R.string.imageSize))
                .appendEncodedPath(imagePath)
                .build();

        Picasso.with(context)
                .load(imageURL)
                .into(imageView);

        return imageURL;
    }

    protected static JSONObject readFromUrl(URL url) throws IOException, JSONException {
        String LOG_TAG = "readFromURL";
        HttpURLConnection urlConnection;
        urlConnection = (HttpURLConnection) url.openConnection();

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        StringBuilder inStringBuilder = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (((line = reader.readLine()) != null)) {
            inStringBuilder.append(line);
        }
        String inString = inStringBuilder.toString();
        urlConnection.disconnect();
        Log.v(LOG_TAG, inString);

        return new JSONObject(inString);
    }

    /*Static functions to allow (de)serialization of the list of movies */
    public static ArrayList<String> toStringArrayList(List<JSONObject> array) {
        ArrayList<String> stringList = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            stringList.add(i, array.get(i).toString());
        }
        return stringList;
    }

    public static List<JSONObject> toJSONList(List<String> stringList) throws JSONException {
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i = 0; i < stringList.size(); i++) {
            jsonList.add(i, new JSONObject(stringList.get(i)));
        }
        return jsonList;
    }

    protected static JSONObject fetchMovieById(String id, Context context) throws IOException, JSONException {
        URL url = new URL(Uri.parse(context.getString(R.string.tmdb_movie_path))
                .buildUpon()
                .appendPath(id)
                .appendQueryParameter(context.getString(R.string.api_key_query), context.getString(R.string.api_key))
                .build().toString());
        return readFromUrl(url);
    }

    public static void detailFragmentSwap(Fragment fragment, FragmentManager manager, JSONObject movie, Boolean isTablet, String backStackString) {
        Bundle movieArgument = new Bundle();
        movieArgument.putString("movie", movie.toString());
        fragment.setArguments(movieArgument);
        if (isTablet) {
            manager.beginTransaction()
                    .replace(R.id.detail_fragment, fragment)
                    .addToBackStack(backStackString)
                    .commit();
        } else {
            manager.beginTransaction()
                    .replace(R.id.master_fragment, fragment)
                    .addToBackStack(backStackString)
                    .commit();
        }
    }
}
