package com.lighters.test.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lighters.library.expanddrag.Adapter.ExpandDragRecyclerAdapter;
import com.lighters.library.expanddrag.Model.ParentListItem;
import com.lighters.test.R;
import com.lighters.test.model.GroupDrag;
import com.lighters.test.viewholder.GroupDragViewHolder;
import com.lighters.test.viewholder.GroupLoadMoreViewHolder;
import com.lighters.test.viewholder.ItemViewHolder;

import java.util.List;

public class GroupDragAdapter extends ExpandDragRecyclerAdapter<GroupDragViewHolder, ItemViewHolder,
        GroupLoadMoreViewHolder> {

    private LayoutInflater mInflator;

    public GroupDragAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public GroupDragViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View recipeView = mInflator.inflate(R.layout.group_view, parentViewGroup, false);
        return new GroupDragViewHolder(recipeView);
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View ingredientView = mInflator.inflate(R.layout.child_view, childViewGroup, false);
        return new ItemViewHolder(ingredientView);
    }

    @Override
    public void onBindParentViewHolder(GroupDragViewHolder groupViewHolder, int position, ParentListItem parentListItem) {
        GroupDrag group = (GroupDrag) parentListItem;
        groupViewHolder.bind(group);
    }

    @Override
    public void onBindChildViewHolder(ItemViewHolder itemViewHolder, int position, Object childListItem) {
        String ingredient = (String) childListItem;
        itemViewHolder.bind(ingredient);
    }

    @Override
    public GroupLoadMoreViewHolder onCreateLoadMoreViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onBindLoadMoreViewHolder(GroupLoadMoreViewHolder viewHolder, int position, int parentIndex, Object object) {

    }
}
