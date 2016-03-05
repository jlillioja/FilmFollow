package io.github.jlillioja.project1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class DiscoverFragment extends AppCompatActivity {



    private final static String LOG_TAG = DiscoverFragment.class.getSimpleName();
    protected List<JSONObject> moviesList;

    protected ImageAdapter mAdapter;
    Context context;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);


        setContentView(R.layout.fragment_main);
        settings = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);


        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DetailsFragment.class);
                /*Convert movie whose click is registered to string in the intent. Could consider parcelable.*/
                intent.putExtra(getString(R.string.key_movie), moviesList.get(position).toString());
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            List<String> savedMoviesStringList;
            /* If we can get a movie list from a savedInstanceState, use that one instead. */
            if ((savedMoviesStringList = savedInstanceState.getStringArrayList(getString(R.string.key_moviesList))) != null) {
                try {
                    moviesList = Utils.toJSONList(savedMoviesStringList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else new populateMoviesTask().execute();
        } else new populateMoviesTask().execute();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (moviesList != null) {
            savedInstanceState.putStringArrayList(getString(R.string.key_moviesList), Utils.toStringArrayList(moviesList));
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh) {
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.popularity_sort) {
            settings.edit().putString("sort", "popularity.desc").apply();
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.rating_sort) {
            settings.edit().putString("sort", "vote_average.desc").apply();
            new populateMoviesTask().execute();
            return true;
        }

        if (id == R.id.favorites_view) {
            new populateFavoritesTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private JSONObject fetchMovieById(String id) throws IOException, JSONException {
        String LOG_TAG = getLocalClassName();

        URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path))
                .buildUpon()
                .appendPath(id)
                .appendQueryParameter(getString(R.string.api_key_query), getString(R.string.api_key))
                .build().toString());

        Log.v(LOG_TAG, url.toString());
        return Utils.readFromUrl(url);
    }

    private class populateMoviesTask extends AsyncTask<Void, Void, JSONObject> {

        private final String LOG_TAG = populateMoviesTask.class.getSimpleName();

        protected JSONObject doInBackground(Void... n) {
            try {
                URL url = new URL(Uri.parse(getString(R.string.tmdb_discover_path)).buildUpon()
                        .appendQueryParameter(getString(R.string.api_key_query), getApplicationContext().getString(R.string.api_key))
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
                GridView gridView = (GridView) findViewById(R.id.gridView);
                mAdapter = new ImageAdapter(context, R.layout.grid_item, moviesList);
                gridView.setAdapter(mAdapter);
            }
        }

    }

    private class populateFavoritesTask extends AsyncTask<Void, Void, List<JSONObject>> {

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
                    favorites.add(fetchMovieById(idIterator.next()));
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
            GridView gridView = (GridView) findViewById(R.id.gridView);
            mAdapter = new ImageAdapter(context, R.layout.grid_item, moviesList);
            gridView.setAdapter(mAdapter);
        }
    }


}
