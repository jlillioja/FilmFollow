package io.github.jlillioja.project1;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MovieReviewsActivity extends AppCompatActivity {

    public final static String LOG_TAG = "MovieReviewsActivity";
    JSONArray reviewsJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Entered MovieReviewsActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_reviews);

        Intent intent = getIntent();
    }

    /*

    private class ReviewAdapter extends ListAdapter<JSONArray> {
        JSONArray reviews;
        Context context;
        int layoutResourceID;

        public ReviewAdapter(Context mContext, int mLayoutResourceID, JSONArray mReviews) {
            super(mContext, mLayoutResourceID);
            layoutResourceID = mLayoutResourceID;
            context = mContext;
            reviews = mReviews;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (reviews == null) {
                Toast.makeText(context, R.string.reviews_not_loaded, Toast.LENGTH_LONG).show();
                return null;
            }

            View itemView;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                itemView = inflater.inflate(layoutResourceID, parent, false);
            } else {
                itemView = convertView;
            }

            try {
                JSONObject reviewJSON = reviews.getJSONObject(position);

                ((TextView) itemView.findViewById(R.id.review)).setText(reviewJSON.getString("key_content"));

                return itemView;
            } catch (JSONException err) {
                err.printStackTrace();
                return null;
            }
        }
    }

    */
}
