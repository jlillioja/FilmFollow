package io.github.jlillioja.project1;

import android.content.Context;
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
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DetailsFragment extends AppCompatActivity {

    SharedPreferences settings;
    private Context context;
    private JSONObject movie;

    private final static String LOG_TAG = DetailsFragment.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        Intent intent = getIntent();
        Intent errorIntent = new Intent(context, DiscoverFragment.class);
        String movieString;

        try {
            if (savedInstanceState != null) {
                Log.d(LOG_TAG, "Restoring non-null savedInstanceState");
                movie = new JSONObject(savedInstanceState.getString(getString(R.string.key_movie)));
            } else if ((movieString = intent.getStringExtra(getString(R.string.key_movie))) != null) {
                Log.d(LOG_TAG, "Attempting to restore from intent");
                movie = new JSONObject(movieString);
            } else {
                if ((movieString = intent.getStringExtra(getString(R.string.key_movie))) != null) {
                    Log.d(LOG_TAG, "Attempting to restore from intent");

                    movie = new JSONObject(movieString);
                    if (movie == null) {
                        startActivity(errorIntent);
                    }
                } else {
                    Log.d(LOG_TAG, "No movie from savedInstanceState or intent");
                    startActivity(new Intent(context, DiscoverFragment.class));
                }
            }
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragment_movie_details);

            ImageView image = (ImageView) findViewById(R.id.poster_imageView);
            Utils.loadImage(image, movie, this);

            ToggleButton favorite = (ToggleButton) findViewById(R.id.favorite_button);
            if (isFavorite(movie)) favorite.setChecked(true); /* Set off by default */

            TextView title = (TextView) findViewById(R.id.title_textView);
            title.setText(movie.getString(getString(R.string.key_title)));

            TextView overview = (TextView) findViewById(R.id.overview_textView);
            overview.setText(movie.getString(getString(R.string.key_overview)));

            TextView release = (TextView) findViewById(R.id.release_textView);
            release.setText(getString(R.string.release_date_title) + movie.getString("release_date"));

            TextView rating = (TextView) findViewById(R.id.rating_textView);
            rating.setText(getString(R.string.rating_title) + movie.getString("vote_average"));

            Button trailer = (Button) findViewById(R.id.trailer_button);
            //trailer.setText(R.string.trailer_title); //Changed to hardcoded button text in XML layout. Which is a better design pattern?
        } catch (JSONException e) {
            e.printStackTrace();
            startActivity(errorIntent);
        }
    }

    private boolean isFavorite(JSONObject movie) throws JSONException {
        return settings
                .getStringSet(getString(R.string.key_favorites), Collections.EMPTY_SET)
                .contains(Integer.toString(movie.getInt(getString(R.string.id_key))));
    }

    public void launchTrailer(View view) {
        new launchTrailerTask().execute();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (movie != null) {
            String movieString = movie.toString();
            Log.d(LOG_TAG, "Saving non-null instance state: " + movieString);
            savedInstanceState.putString(getString(R.string.key_movie), movieString);
        }
        super.onSaveInstanceState(savedInstanceState);

    }

    public void onToggleFavorite(View view) throws JSONException {

        ToggleButton button = (ToggleButton) view;
        String id = Integer.toString(movie.getInt(getString(R.string.id_key)));
        String favorite_key = getString(R.string.key_favorites);
        SharedPreferences settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        Set<String> oldFavorites = settings.getStringSet(favorite_key, Collections.EMPTY_SET);
        Set<String> newFavorites = new HashSet<String>(oldFavorites);

        /* If the button is checked, make sure this movie is in the favorites set. Otherwise, make sure it isn't. */
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

    public void viewReviews(View view) {
        Intent intent = new Intent(getApplicationContext(), DetailsFragment.class);
        intent.putExtra(getString(R.string.key_movie), movie.toString());
        startActivity(intent);
    }

    private class launchTrailerTask extends AsyncTask<Void, Void, Intent> {
        private final String LOG_TAG = launchTrailerTask.class.getSimpleName();

        @Override
        protected Intent doInBackground(Void... params) {
            try {
                URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path)).buildUpon()
                        .appendPath(movie.getString("id"))
                        .appendPath("videos")
                        .appendQueryParameter(getString(R.string.api_key_query), getApplicationContext().getString(R.string.api_key))
                        .build().toString());

                String videoKey = Utils.readFromUrl(url)
                        .getJSONArray(getString(R.string.key_results))
                        .getJSONObject(0)
                        .getString(getString(R.string.key_trailer));

                Log.d(LOG_TAG, "videoKey: " + videoKey);

                Uri videoPath = Uri.parse(getString(R.string.youtube_path)).buildUpon()
                        .appendPath("watch")
                        .appendQueryParameter("v", videoKey)
                        .build();

                Log.d(LOG_TAG, "videoPath: " + videoPath.toString());

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(videoPath);
                return intent;
            } catch (JSONException | IOException err) {
                err.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Intent intent) {
            if (intent != null) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        }

        public void onToggleButton(View view) throws JSONException {

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

        public void viewReviews(View view) {
            Intent intent = new Intent(getApplicationContext(), ReviewsFragment.class);
            intent.putExtra(getString(R.string.key_movie), movie.toString());
            startActivity(intent);
        }
    }
}
