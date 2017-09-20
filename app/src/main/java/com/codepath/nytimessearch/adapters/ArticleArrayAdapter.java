package com.codepath.nytimessearch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.model.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by vidhya on 9/19/17.
 */

public class ArticleArrayAdapter extends ArrayAdapter<Article> {

    public ArticleArrayAdapter(@NonNull Context context, @NonNull List<Article> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // get data from the current position
        Article article = this.getItem(position);

        // Check if there is a view to reuse
        if (convertView == null) {
            // Inflate new one
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_article, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ivImage);
        imageView.setImageResource(0);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(article.getHeadLine());

        String url = article.getThumbNail();
        if (!url.isEmpty()) {
            Picasso.with(getContext()).load(url).into(imageView);
        }
        return convertView;
    }
}
