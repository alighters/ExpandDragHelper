package com.lighters.test;

import android.view.View;
import android.widget.TextView;

import com.lighters.library.expanddrag.ViewHolder.LoadMoreViewHolder;

/**
 * Created by david on 16/1/21.
 */
public class RecipeLoadMoreViewHolder extends LoadMoreViewHolder {

    private TextView moreView;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public RecipeLoadMoreViewHolder(View itemView) {
        super(itemView);
        moreView = (TextView) itemView.findViewById(R.id.load_more_textview);
    }

    public void bind(String text) {
        moreView.setText(text);
    }
}
