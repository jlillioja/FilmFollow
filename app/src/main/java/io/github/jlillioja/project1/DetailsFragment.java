package io.github.jlillioja.project1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailsFragment extends Fragment implements View.OnClickListener {

    private final static String LOG_TAG = DetailsFragment.class.getSimpleName();
    SharedPreferences settings;
    DetailsListener callback;
    @InjectView(R.id.favorite_button)
    ToggleButton favorite;
    @InjectView(R.id.poster_imageView)
    ImageView image;
    @InjectView(R.id.title_textView)
    TextView title;
    @InjectView(R.id.overview_textView)
    TextView overview;
    @InjectView(R.id.release_textView)
    TextView release;
    @InjectView(R.id.rating_textView)
    TextView rating;
    @InjectView(R.id.trailer_button)
    Button trailer;
    @InjectView(R.id.reviews_button)
    Button reviews;
    private Context context;
    private JSONObject movie;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        settings = getActivity().getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        String movieString = this.getArguments().getString(context.getString(R.string.key_movie));
        try {
            callback = (DetailsListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString());
        }
        try {
            movie = new JSONObject(movieString);
            View view = inflater.inflate(R.layout.fragment_movie_details, parent, false);
            ButterKnife.inject(this, view);
            return view;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            Utils.loadImage(image, movie, context);
            if (isFavorite(movie)) {
                favorite.setChecked(true);
            } /* Set off by default */
            favorite.setOnClickListener(this);
            title.setText(movie.getString(getString(R.string.key_title)));
            overview.setText(movie.getString(getString(R.string.key_overview)));
            release.setText(getString(R.string.release_date_title) + movie.getString("release_date"));
            rating.setText(getString(R.string.rating_title) + movie.getString("vote_average"));
            trailer.setOnClickListener(this);
            reviews.setOnClickListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isFavorite(JSONObject movie) throws JSONException {
        return settings
                .getStringSet(getString(R.string.key_favorites), Collections.EMPTY_SET)
                .contains(Integer.toString(movie.getInt(getString(R.string.id_key))));
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

    public void launchTrailer() {
        new LaunchTrailerTask().execute();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.favorite_button) {
            try {
                onToggleFavorite(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (id == R.id.reviews_button) {
            callback.viewReviews(movie);
        }

        if (id == R.id.trailer_button) {
            launchTrailer();
        }
    }

    public interface DetailsListener {
        void viewReviews(JSONObject movie);
    }

    private class LaunchTrailerTask extends AsyncTask<Void, Void, Intent> {
        private final String LOG_TAG = LaunchTrailerTask.class.getSimpleName();

        @Override
        protected Intent doInBackground(Void... params) {
            try {
                URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path)).buildUpon()
                        .appendPath(movie.getString("id"))
                        .appendPath("videos")
                        .appendQueryParameter(getString(R.string.api_key_query), context.getString(R.string.api_key))
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
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        }
    }
}
