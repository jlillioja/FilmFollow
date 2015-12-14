package io.github.jlillioja.project1;

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

public class MainActivity extends AppCompatActivity {

    protected JSONObject moviesJSON;
    protected ImageAdapter mAdapter;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        settings = getPreferences(MODE_PRIVATE);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
                try {
                    /*Convert movie whose click is registered to string in the intent. Could consider parcelable.*/
                    intent.putExtra(getString(R.string.key_movie), moviesJSON.getJSONArray("results").getJSONObject(position).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            String savedMoviesJSON;
            if ((savedMoviesJSON = savedInstanceState.getString(getString(R.string.key_movesJSON))) != null)
            {
                try {
                    moviesJSON = new JSONObject(savedMoviesJSON);
                    gridView.setAdapter(mAdapter = new ImageAdapter(this, R.layout.grid_item, moviesJSON));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else new populateMoviesTask().execute();
        } else new populateMoviesTask().execute();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (moviesJSON != null) savedInstanceState.putString(getString(R.string.key_movesJSON), moviesJSON.toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    public boolean onCreateOptionsMenu (Menu menu) {
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

        return super.onOptionsItemSelected(item);
    }

    private class populateMoviesTask extends AsyncTask<Void, Void, JSONObject> {

        private final String LOG_TAG = populateMoviesTask.class.getSimpleName();

        protected JSONObject doInBackground(Void... n) {

            /* Declare urlConnection outside try/catch block so it can be closed in finally. */
            HttpURLConnection urlConnection = null;

            try {

                URL url = new URL(Uri.parse(getString(R.string.tmdb_discover_path)).buildUpon()
                        .appendQueryParameter(getString(R.string.api_key_query), getApplicationContext().getString(R.string.api_key))
                        .appendQueryParameter(getString(R.string.sort_query), settings.getString("sort", "popularity.desc"))
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

                return new JSONObject(inString);

            } catch (MalformedURLException err) {
                err.printStackTrace();
                return null;
            } catch (IOException err) {
                err.printStackTrace();
                return null;
            } catch (JSONException err) {
                err.printStackTrace();
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        /* Assign internal moviesJSON object and create and bind ImageAdapter to grid. */
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                moviesJSON = result;
                GridView gridView = (GridView) findViewById(R.id.gridView);
                mAdapter = new ImageAdapter(getApplicationContext(), R.layout.grid_item, moviesJSON);
                gridView.setAdapter(mAdapter);
            }
        }

    }

}
