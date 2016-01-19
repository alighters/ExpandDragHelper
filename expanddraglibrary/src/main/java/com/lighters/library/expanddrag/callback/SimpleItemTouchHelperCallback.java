/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lighters.library.expanddrag.callback;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.lighters.library.expanddrag.Adapter.ExpandableRecyclerAdapter;
import com.lighters.library.expanddrag.ViewHolder.ParentViewHolder;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    public static final float ALPHA_FULL = 1.0f;

    private ParentViewHolder mLastSelectViewHolder;

    private ExpandableRecyclerAdapter mAdapter;

    private boolean selected = false;

    public SimpleItemTouchHelperCallback(ExpandableRecyclerAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags based on the layout manager
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
            target) {
        if (target instanceof ParentViewHolder) {
            ParentViewHolder parentViewHolder = (ParentViewHolder) target;
            parentViewHolder.onSelected();
            if (mLastSelectViewHolder != null && mLastSelectViewHolder != parentViewHolder) {
                mLastSelectViewHolder.onUnSelected();
            }
            mLastSelectViewHolder = (ParentViewHolder) target;
            selected = true;
        } else {
            selected = false;
        }
        return false;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float
            dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        mAdapter.collapseAllParents();
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(ALPHA_FULL);
        if (mLastSelectViewHolder != null) {
            mLastSelectViewHolder.onUnSelected();
        }
        if (selected) {
            int position = mLastSelectViewHolder.getAdapterPosition();
            mAdapter.expandAllParents(position);
        } else
            mAdapter.expandAllParents();
    }
}
