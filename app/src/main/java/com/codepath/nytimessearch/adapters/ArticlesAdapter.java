package com.codepath.nytimessearch.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.model.Article;
import com.codepath.nytimessearch.utils.DynamicHeightImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import static com.codepath.nytimessearch.R.id.ivImage;

/**
 * Created by vidhya on 9/22/17.
 */

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    private List<Article> mArticles;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public ArticlesAdapter(Context mContext, List<Article> mArticles) {
        this.mArticles = mArticles;
        this.mContext = mContext;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View articleView = inflater.inflate(R.layout.item_article, parent, false);

        ViewHolder viewHolder = new ViewHolder(articleView);
        return viewHolder;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = mArticles.get(position);

//        ImageView articleImageView = holder.imageView;
//        articleImageView.setImageResource(0);
//        String url = article.getThumbNail();
//        if (!url.isEmpty()) {
//            Picasso.with(getContext()).load(url).into(articleImageView);
//        }

        DynamicHeightImageView articleImageView = holder.imageView;
        articleImageView.setImageResource(0);
        String url = article.getThumbNail();
        if (!url.isEmpty()) {
            Picasso.with(getContext()).load(url).into(articleImageView);
        }

        TextView textView = holder.tvTitle;
        textView.setText(article.getHeadLine());
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Target {
        //public ImageView imageView;
        public TextView tvTitle;
        public DynamicHeightImageView imageView;

        public ViewHolder(final View itemView) {
            super(itemView);
            //this.imageView = (ImageView) itemView.findViewById(R.id.ivImage);
            this.imageView = (DynamicHeightImageView) itemView.findViewById(ivImage);
            this.tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(itemView,position);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            imageView.setHeightRatio(ratio);
            // Load the image into the view
            imageView.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }
}
