package io.github.jlillioja.project1;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class ReviewsFragment extends Fragment {

    private final static String LOG_TAG = DetailsFragment.class.getSimpleName();
    Activity activity;
    private Context context;
    private JSONObject movie;
    private ListView reviewsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        context = activity.getApplicationContext();
        String movieString = this.getArguments().getString(context.getString(R.string.key_movie));
        try {
            movie = new JSONObject(movieString);
            return inflater.inflate(R.layout.fragment_movie_reviews, parent);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        reviewsList = (ListView) activity.findViewById(R.id.reviewsListView);
        new populateReviewsTask().execute();
    }

    private class populateReviewsTask extends AsyncTask<Void, Void, JSONArray> {
        public final String LOG_TAG = "populateReviewsTask";

        protected JSONArray doInBackground(Void... params) {

            try {
                URL url = new URL(Uri.parse(getString(R.string.tmdb_movie_path)).buildUpon()
                        .appendPath(movie.getString("id"))
                        .appendPath(getString(R.string.path_reviews))
                        .appendQueryParameter(getString(R.string.api_key_query), context.getString(R.string.api_key))
                        .build().toString());

                JSONObject reviewsJSON = Utils.readFromUrl(url);
                return reviewsJSON.getJSONArray("results");
            } catch (IOException err) {
                Log.d(LOG_TAG, "Failed with IOException");
                err.printStackTrace();
                return null;
            } catch (JSONException err) {
                Log.d(LOG_TAG, "Failed with JSONException");
                err.printStackTrace();
                return null;
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
                ArrayAdapter<String> reviewAdapter = new ArrayAdapter<String>(context, R.layout.review_item, reviewsStringArray);
                reviewsList.setAdapter(reviewAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "reviewsJSONArray null");
            }
        }
    }
}
