package io.github.jlillioja.project1;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jlillioja on 11/29/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private JSONObject moviesJSON = null;

    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    public ImageAdapter(Context c, JSONObject movies) {
        super();
        mContext = c;
        moviesJSON = movies;
    }




    public int getCount() {
        if (moviesJSON == null) return 0;
        try {
            return moviesJSON.getJSONArray("results").length();
        } catch (JSONException err) {
            return 0;
        }
    }

    public JSONObject getItem(int position) {
        if (moviesJSON == null) return null;
        try {
            return (JSONObject) moviesJSON.getJSONArray("results").get(position);
        } catch (JSONException err) {
            return null;
        }
    }

    public long getItemId(int position) {

        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(LOG_TAG, "entered getView");

        if (moviesJSON == null) {
            Toast.makeText(mContext, "Movies not yet loaded.", Toast.LENGTH_LONG).show();
            return null;
        }

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0,0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        try {
            String imagePath = moviesJSON.getJSONArray("results").getJSONObject(position).getString("poster_path");
            Log.d(LOG_TAG, "imagePath = "+imagePath);
            Uri imageURL = Uri.parse("http://image.tmdb.org/t/p/").buildUpon()
                    .appendPath(mContext.getString(R.string.imageSize))
                    .appendEncodedPath(imagePath)
                    .build();

            Picasso.with(mContext).load(imageURL).into(imageView);

            Log.d(LOG_TAG, "returning imageView for image "+imageURL.toString());

            return imageView;
        } catch (JSONException err) {
            Log.e(LOG_TAG, "Couldn't parse moviesJSON", err);
            return null;
        }
    }
}
