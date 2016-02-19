package com.lighters.test.viewholder;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.lighters.library.expanddrag.ViewHolder.ChildViewHolder;
import com.lighters.test.R;

public class ItemViewHolder extends ChildViewHolder {

    private TextView mIngredientTextView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mIngredientTextView = (TextView) itemView.findViewById(R.id.ingredient_textview);
    }

    public void bind(String ingredient) {
        mIngredientTextView.setText(ingredient.toString());
    }


    /**
     * 显示拖拽的效果
     */
    public void setDragShow() {
        mIngredientTextView.setTextColor(Color.BLUE);
        mIngredientTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIngredientTextView.setTextColor(Color.BLACK);
            }
        }, 3000);
    }


}
