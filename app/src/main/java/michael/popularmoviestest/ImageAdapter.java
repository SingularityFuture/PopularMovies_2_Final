package michael.popularmoviestest;

/**
 * Created by Michael on 4/2/2016.
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<MovieClass> {
    private Context mContext;
    private int layoutResourceId;
    private ArrayList<MovieClass> mGridData = new ArrayList<MovieClass>();

    public ImageAdapter(Context mContext, int layoutResourceId, ArrayList<MovieClass> mGridData) {
        super(mContext, layoutResourceId, mGridData);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.mGridData = mGridData;
    }

    public void setGridData(ArrayList<MovieClass> mGridData) {
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    public int getCount(ArrayList<MovieClass> mGridData) {
        return mGridData.size();
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(300, 500));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        MovieClass item = mGridData.get(position);
        Picasso.with(mContext).load(item.getPoster()).into(imageView);

        return imageView;
    }
}
