package io.github.jlillioja.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MovieDetailsActivity extends AppCompatActivity {

    JSONObject movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();

        try {
            movie = new JSONObject(intent.getStringExtra(getString(R.string.key_movie)));

            ImageView image = (ImageView) findViewById(R.id.poster_imageView);
            ImageAdapter.loadImage(image, movie, this);

            ToggleButton favorite = (ToggleButton) findViewById(R.id.favorite_button);
            if (isFavorite(movie)) favorite.setChecked(true); /* Set off by default */

            TextView title = (TextView) findViewById(R.id.title_textView);
            title.setText(movie.getString("original_title"));

            TextView overview = (TextView) findViewById(R.id.overview_textView);
            overview.setText(movie.getString("overview"));

            TextView release = (TextView) findViewById(R.id.release_textView);
            release.setText(getString(R.string.release_date_title) + movie.getString("release_date"));

            TextView rating = (TextView) findViewById(R.id.rating_textView);
            rating.setText(getString(R.string.rating_title) + movie.getString("vote_average"));

            Button trailer = (Button) findViewById(R.id.trailer_button);
            //trailer.setText(R.string.trailer_title); //Changed to hardcoded button text in XML layout. Which is a better design pattern?

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isFavorite(JSONObject movie) throws JSONException {
        return getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE)
                .getStringSet(getString(R.string.key_favorites), Collections.EMPTY_SET)
                .contains(Integer.toString(movie.getInt(getString(R.string.id_key))));
    }

    public void launchTrailer(View view) {
        new launchTrailerTask().execute();

    }

    private class launchTrailerTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = launchTrailerTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {

            //Find Trailer
        /* Declare urlConnection outside try/catch block so it can be closed in finally. */
            HttpURLConnection urlConnection = null;

            try {

                URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path)).buildUpon()
                        .appendPath(movie.getString("id"))
                        .appendPath("videos")
                        .appendQueryParameter(getString(R.string.api_key_query), getApplicationContext().getString(R.string.api_key))
                        .build().toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                StringBuilder inStringBuilder = new StringBuilder();
                String line;

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while (((line = reader.readLine()) != null)) {
                    inStringBuilder.append(line);
                }

                String inString = inStringBuilder.toString();
                Log.v(LOG_TAG, inString);

                JSONObject videosJSON = new JSONObject(inString);
                String videoKey = videosJSON.getJSONArray(getString(R.string.results_key)).getJSONObject(0).getString(getString(R.string.key_trailer));

                Log.d(LOG_TAG, "videoKey: " + videoKey);

                Uri videoPath = Uri.parse(getString(R.string.youtube_path)).buildUpon()
                        .appendPath("watch")
                        .appendQueryParameter("v", videoKey)
                        .build();

                Log.d(LOG_TAG, "videoPath: "+videoPath.toString());

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(videoPath);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            } catch (MalformedURLException err) {
                err.printStackTrace();
            } catch (IOException err) {
                err.printStackTrace();
            } catch (JSONException err) {
                err.printStackTrace();
            } finally {
                urlConnection.disconnect();
                return null;
            }
        }
    }

    public void onToggleStar(View view) throws JSONException {

        ToggleButton button = (ToggleButton) view;
        String id = Integer.toString(movie.getInt(getString(R.string.id_key)));
        String favorite_key = getString(R.string.key_favorites);
        SharedPreferences settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        Set<String> oldFavorites = settings.getStringSet(favorite_key, Collections.EMPTY_SET);
        Set<String> newFavorites = new HashSet<String>(oldFavorites);

        /* If the button is checked, make sure this movie is in the favorites set.
           Otherwise, make sure it isn't. */
        if (button.isChecked()) {
            newFavorites.add(id);
        } else {
            newFavorites.remove(id);
        }
        settings.edit()
                .remove(favorite_key)
                .putStringSet(favorite_key, newFavorites)
                .apply();
    }

    public void viewReviews (View view) {
        Toast.makeText(getApplicationContext(), getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
    }
}
