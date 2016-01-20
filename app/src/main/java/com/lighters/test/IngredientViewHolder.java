package com.lighters.test;

import android.view.View;
import android.widget.TextView;

import com.lighters.library.expanddrag.ViewHolder.ChildViewHolder;

public class IngredientViewHolder extends ChildViewHolder {

    private TextView mIngredientTextView;

    public IngredientViewHolder(View itemView) {
        super(itemView);
        mIngredientTextView = (TextView) itemView.findViewById(R.id.ingredient_textview);
    }

    public void bind(String ingredient) {
        mIngredientTextView.setText(ingredient.toString());
    }
}
