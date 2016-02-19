package com.lighters.library.expanddrag.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lighters.library.expanddrag.Model.LoadMoreStatus;

/**
 * ViewHolder for loading more in the child list
 * item.
 * <p/>
 * The user should extend this class and implement as they wish for their
 * child list item.
 *
 * @author david
 * @version 1.0
 * @since 1/21/2016
 */
public abstract class LoadMoreViewHolder extends RecyclerView.ViewHolder {

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public LoadMoreViewHolder(final View itemView) {
        super(itemView);
    }

    public abstract void update(LoadMoreStatus status);
}