package io.github.jlillioja.project1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private JSONObject moviesJSON;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridView = (GridView) findViewById(R.id.gridView);

        new populateMoviesTask().execute();

        mAdapter = new ImageAdapter(this, moviesJSON);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Not just yet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private class populateMoviesTask extends AsyncTask<Void, Void, JSONObject> {

        private final String LOG_TAG = populateMoviesTask.class.getSimpleName();

        protected JSONObject doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?api_key=" + getApplicationContext().getString(R.string.api_key)); //TODO - softcode

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                return new JSONObject(in.toString());
            } catch (MalformedURLException err) {
                Log.e(LOG_TAG, "MalformedURLException", err);
                return null;
            } catch (IOException err) {
                Log.e(LOG_TAG, "IOException", err);
                return null;
            } catch (JSONException err) {
                Log.e(LOG_TAG, "JSONException", err);
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        protected void onPostExecute(JSONObject result) {
            if (result != null) {

            }
        }
    }
}
