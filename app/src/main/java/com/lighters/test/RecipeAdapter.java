package com.lighters.test;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lighters.library.expanddrag.Adapter.ExpandableRecyclerAdapter;
import com.lighters.library.expanddrag.Model.ParentListItem;

import java.util.List;

public class RecipeAdapter extends ExpandableRecyclerAdapter<RecipeViewHolder, IngredientViewHolder, RecipeLoadMoreViewHolder> {

    private LayoutInflater mInflator;

    public RecipeAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public RecipeViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View recipeView = mInflator.inflate(R.layout.recipe_view, parentViewGroup, false);
        return new RecipeViewHolder(recipeView);
    }

    @Override
    public IngredientViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View ingredientView = mInflator.inflate(R.layout.ingredient_view, childViewGroup, false);
        return new IngredientViewHolder(ingredientView);
    }

    @Override
    public void onBindParentViewHolder(RecipeViewHolder recipeViewHolder, int position, ParentListItem parentListItem) {
        Recipe recipe = (Recipe) parentListItem;
        recipeViewHolder.bind(recipe);
    }

    @Override
    public void onBindChildViewHolder(IngredientViewHolder ingredientViewHolder, int position, Object childListItem) {
        String ingredient = (String) childListItem;
        ingredientViewHolder.bind(ingredient);
    }

    @Override
    public RecipeLoadMoreViewHolder onCreateLoadMoreViewHolder(ViewGroup viewGroup) {
        View view = mInflator.inflate(R.layout.load_more_view, viewGroup, false);
        return new RecipeLoadMoreViewHolder(view);
    }

    @Override
    public void onBindLoadMoreViewHolder(RecipeLoadMoreViewHolder viewHolder, int position, Object object) {
        String text = object.toString();
        viewHolder.bind(text);
    }
}
