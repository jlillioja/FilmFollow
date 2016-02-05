package io.github.jlillioja.project1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MovieReviewsActivity extends AppCompatActivity {

    public final static String LOG_TAG = "MovieReviewsActivity";
    JSONObject movie;
    private ListView reviewsList;
    private ArrayAdapter<String> reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Entered MovieReviewsActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_reviews);

        Intent intent = getIntent();

        try {
            movie = new JSONObject(intent.getStringExtra(getString(R.string.key_movie)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reviewsList = (ListView) findViewById(R.id.reviewsListView);

        new populateReviewsTask().execute();

    }

    private class populateReviewsTask extends AsyncTask<Void, Void, JSONArray> {
        public final String LOG_TAG = "populateReviewsTask";

        protected JSONArray doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            try {

                URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path)).buildUpon()
                        .appendPath(movie.getString("id"))
                        .appendPath(getString(R.string.path_reviews))
                        .appendQueryParameter(getString(R.string.api_key_query), getApplicationContext().getString(R.string.api_key))
                        .build().toString());

                Log.d(LOG_TAG, "Accessing: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                StringBuilder inStringBuilder = new StringBuilder();
                String line;

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while (((line = reader.readLine()) != null)) {
                    inStringBuilder.append(line);
                }

                String inString = inStringBuilder.toString();
                Log.v(LOG_TAG, "Raw input: " + inString);

                JSONObject reviewsJSON = new JSONObject(inString);
                Log.v(LOG_TAG, "After JSON passthrough: " + reviewsJSON.toString());

                JSONArray reviewsArray = reviewsJSON.getJSONArray("results");

                Log.v(LOG_TAG, "First item in reviewsArray: " + reviewsArray.getJSONObject(0).getString("content"));

                return reviewsArray;


            } catch (IOException err) {
                Log.d(LOG_TAG, "Failed with IOException");
                err.printStackTrace();
                return null;
            } catch (JSONException err) {
                Log.d(LOG_TAG, "Failed with JSONException");
                err.printStackTrace();
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        protected void onPostExecute(JSONArray reviewsJSONArray) {
            Log.d(LOG_TAG, "entered onPostExecute");
            try {

                Log.d(LOG_TAG, "reviewsJSONArray not null");
                int length = reviewsJSONArray.length();
                String[] reviewsStringArray = new String[length];
                for (int i = 0; i < length; i++) {

                    reviewsStringArray[i] = reviewsJSONArray.getJSONObject(i).getString("content");
                }

                reviewAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.review_item, reviewsStringArray);
                reviewsList.setAdapter(reviewAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "reviewsJSONArray null");
            }
        }
    }
}
