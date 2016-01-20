package com.lighters.test;

import com.lighters.library.expanddrag.Model.ParentListItem;

import java.util.ArrayList;

public class Recipe implements ParentListItem {

    private String mName;
    private ArrayList<String> mIngredients;

    public Recipe(String name, ArrayList<String> ingredients) {
        mName = name;
        mIngredients = ingredients;
    }

    public String getName() {
        return mName;
    }

    @Override
    public ArrayList<String> getChildItemList() {
        return mIngredients;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
