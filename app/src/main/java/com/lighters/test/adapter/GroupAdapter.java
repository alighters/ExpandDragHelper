package com.lighters.test.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.lighters.library.expanddrag.Adapter.ExpandableRecyclerAdapter;
import com.lighters.library.expanddrag.Model.ParentListItem;
import com.lighters.test.R;
import com.lighters.test.model.Group;
import com.lighters.test.viewholder.GroupViewHolder;
import com.lighters.test.viewholder.ItemViewHolder;
import java.util.List;

public class GroupAdapter extends ExpandableRecyclerAdapter<GroupViewHolder, ItemViewHolder> {

    private LayoutInflater mInflator;

    public GroupAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public GroupViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View recipeView = mInflator.inflate(R.layout.group_view, parentViewGroup, false);
        return new GroupViewHolder(recipeView);
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View ingredientView = mInflator.inflate(R.layout.child_view, childViewGroup, false);
        return new ItemViewHolder(ingredientView);
    }

    @Override
    public void onBindParentViewHolder(GroupViewHolder groupViewHolder, int position, ParentListItem parentListItem) {
        Group group = (Group) parentListItem;
        groupViewHolder.bind(group);
    }

    @Override
    public void onBindChildViewHolder(ItemViewHolder itemViewHolder, int position, Object childListItem) {
        String ingredient = (String) childListItem;
        itemViewHolder.bind(ingredient);
    }
}
