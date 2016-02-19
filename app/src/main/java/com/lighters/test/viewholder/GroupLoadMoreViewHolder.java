package com.lighters.test.viewholder;

import android.view.View;
import android.widget.TextView;
import com.lighters.library.expanddrag.Model.LoadMoreStatus;
import com.lighters.library.expanddrag.ViewHolder.LoadMoreViewHolder;
import com.lighters.test.R;

/**
 * Created by david on 16/1/21.
 */
public class GroupLoadMoreViewHolder extends LoadMoreViewHolder {

    private TextView moreView;
    private String data;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public GroupLoadMoreViewHolder(View itemView) {
        super(itemView);
        moreView = (TextView) itemView.findViewById(R.id.load_more_textview);
    }

    @Override
    public void update(LoadMoreStatus status) {
        if (status.equals(LoadMoreStatus.INIT)) {
            moreView.setText("加载更多");
        } else if (status.equals(LoadMoreStatus.LOADING)) {
            moreView.setText("正在加载...");
        } else {
            moreView.setText("加载完成");
        }
    }

    public void bind(String text) {
        data = text;
    }
}
