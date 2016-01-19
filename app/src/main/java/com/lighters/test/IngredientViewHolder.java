package com.lighters.test;

import android.view.View;
import android.widget.TextView;

import com.lighters.library.expanddrag.Adapter.ExpandableRecyclerAdapter;
import com.lighters.library.expanddrag.ViewHolder.ChildViewHolder;

public class IngredientViewHolder extends ChildViewHolder {

    private TextView mIngredientTextView;

    public IngredientViewHolder(View itemView, ExpandableRecyclerAdapter adapter) {
        super(itemView, adapter);
        mIngredientTextView = (TextView) itemView.findViewById(R.id.ingredient_textview);
    }

    public void bind(Ingredient ingredient) {
        mIngredientTextView.setText(ingredient.getName());
    }
}
