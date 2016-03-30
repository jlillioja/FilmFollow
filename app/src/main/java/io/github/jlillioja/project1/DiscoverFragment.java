package io.github.jlillioja.project1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DiscoverFragment extends Fragment implements AdapterView.OnItemClickListener {

    private final static String LOG_TAG = DiscoverFragment.class.getSimpleName();
    protected List<JSONObject> moviesList;

    protected ImageAdapter mAdapter;
    Activity activity;

    Context context;
    SharedPreferences settings;
    OnMovieClickListener callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        return inflater.inflate(R.layout.fragment_discover, parent, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        context = activity.getApplicationContext();
        settings = activity.getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        try {
            callback = (OnMovieClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
        GridView gridView = (GridView) activity.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(this);
        populateMovies();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        callback.onMovieClick(moviesList.get(position));
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (moviesList != null) {
            savedInstanceState.putStringArrayList(getString(R.string.key_moviesList), Utils.toStringArrayList(moviesList));
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void populateMovies() {
        new populateMoviesTask().execute();
    }

    protected void populateFavorites() {
        new PopulateFavoritesTask().execute();
    }

    public interface OnMovieClickListener {
        void onMovieClick(JSONObject movie);
    }

    protected class populateMoviesTask extends AsyncTask<Void, Void, JSONObject> {

        private final String LOG_TAG = populateMoviesTask.class.getSimpleName();

        protected JSONObject doInBackground(Void... n) {
            try {
                URL url = new URL(Uri.parse(getString(R.string.tmdb_discover_path)).buildUpon()
                        .appendQueryParameter(getString(R.string.api_key_query), context.getString(R.string.api_key))
                        .appendQueryParameter(getString(R.string.sort_query), settings.getString("sort", "popularity.desc"))
                        .build().toString());

                return Utils.readFromUrl(url);
            } catch (MalformedURLException err) {
                err.printStackTrace();
                return null;
            } catch (IOException err) {
                err.printStackTrace();
                return null;
            } catch (JSONException err) {
                err.printStackTrace();
                return null;
            }
        }

        /* Create internal moviesList object and create and bind ImageAdapter to grid. */
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                moviesList = new ArrayList<>();
                try {
                    JSONArray moviesArray = result.getJSONArray(context.getString(R.string.key_results));
                    for (int i = 0; i < moviesArray.length(); i++) {
                        moviesList.add(moviesArray.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                GridView gridView = (GridView) activity.findViewById(R.id.gridView);
                mAdapter = new ImageAdapter(context, R.layout.grid_item, moviesList);
                gridView.setAdapter(mAdapter);
            }
        }
    }

    protected class PopulateFavoritesTask extends AsyncTask<Void, Void, List<JSONObject>> {

        @Override
        protected List<JSONObject> doInBackground(Void... params) {

            /* Fetch list of favorite IDs */
            Set<String> favoriteIDs = settings.getStringSet(getString(R.string.key_favorites), Collections.EMPTY_SET);
            if (favoriteIDs.isEmpty()) return null;

            /* Turn a set of movie IDs into a list of movies. */
            /* Could this be parallelized? */
            Iterator<String> idIterator = favoriteIDs.iterator();
            List<JSONObject> favorites = new ArrayList<>();
            try {
                while (idIterator.hasNext()) {
                    favorites.add(Utils.fetchMovieById(idIterator.next(), context));
                }
                return favorites;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List<JSONObject> result) {
            moviesList = result;
            GridView gridView = (GridView) activity.findViewById(R.id.gridView);
            mAdapter = new ImageAdapter(context, R.layout.grid_item, moviesList);
            gridView.setAdapter(mAdapter);
        }
    }
}
