package io.github.jlillioja.project1;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by jlillioja on 2/11/2016.
 */
public class FavoritesAdapter extends ImageAdapter {
    public FavoritesAdapter(Context context, int layoutResourceID, List<JSONObject> movies) {
        super(context, layoutResourceID, movies);
    }


    /*
    Similar to superclass method, allows static loading of movie posters into imageviews.
    Unlike it's superclass version, this pulls from the stored image database.
     */
    @Override
    public static Uri loadImage(ImageView imageView, JSONObject movie, Context context) throws JSONException {
        
    }
}
