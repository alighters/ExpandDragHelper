package com.lighters.test.model;

import com.lighters.library.expanddrag.Model.LoadMoreStatus;
import com.lighters.library.expanddrag.Model.ParentListItem;

import java.util.ArrayList;

public class Group extends ParentListItem {

    private String mName;
    private ArrayList<String> mIngredients;
    public LoadMoreStatus mLoadMoreStatus = LoadMoreStatus.INIT;

    public Group(String name, ArrayList<String> ingredients) {
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

    @Override
    public boolean isLoadMore() {
        return true;
    }

    @Override
    public LoadMoreStatus getLoadingStatus() {
        return mLoadMoreStatus;
    }

    @Override
    public void setLoadMoreStatus(LoadMoreStatus status) {
        mLoadMoreStatus = status;
    }
}
