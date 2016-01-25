package com.lighters.library.expanddrag.Adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lighters.library.expanddrag.Model.LoadMoreStatus;
import com.lighters.library.expanddrag.Model.ParentListItem;
import com.lighters.library.expanddrag.Model.ParentWrapper;
import com.lighters.library.expanddrag.ViewHolder.ChildViewHolder;
import com.lighters.library.expanddrag.ViewHolder.LoadMoreViewHolder;
import com.lighters.library.expanddrag.ViewHolder.ParentViewHolder;
import com.lighters.library.expanddrag.callback.DragSelectCallback;
import com.lighters.library.expanddrag.callback.LoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RecyclerView.Adapter implementation that
 * adds the ability to expand and collapse list items.
 * <p>
 * Changes should be notified through:
 * {@link #notifyParentItemInserted(int)}
 * {@link #notifyParentItemRemoved(int)}
 * {@link #notifyParentItemChanged(int)}
 * {@link #notifyParentItemRangeInserted(int, int)}
 * {@link #notifyChildItemInserted(int, int)}
 * {@link #notifyChildItemRemoved(int, int)}
 * {@link #notifyChildItemChanged(int, int)}
 * methods and not the notify methods of RecyclerView.Adapter.
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 5/27/2015
 */
public abstract class ExpandDragRecyclerAdapter<PVH extends ParentViewHolder, CVH extends ChildViewHolder, LVH
        extends LoadMoreViewHolder>
        extends ExpandableRecyclerAdapter<PVH, CVH> implements View.OnDragListener, View.OnLongClickListener, View
        .OnTouchListener,
        View.OnClickListener {

    private static final String TAG = ExpandDragRecyclerAdapter.class.getName() + "_tag";

    private static final int TYPE_LOAD_MORE = 2;

    private static final String FROM_POSITION = "from_position";
    private static final String FROM_PARENT_POSITION = "from_parent_position";
    private static final String FROM_CHILD_POSITION = "from_child_position";
    private static final String FROM_POSITION_DATA = "from_position_data";

    private DragSelectCallback mDragSelectCallback;
    private LoadMoreListener mLoadMoreListener;

    /**
     * Primary constructor. Sets up {@link #mParentItemList} and {@link #mItemList}.
     * <p>
     * Changes to {@link #mParentItemList} should be made through add/remove methods in
     * {@link ExpandableRecyclerAdapter}
     *
     * @param parentItemList List of all {@link ParentListItem} objects to be
     *                       displayed in the RecyclerView that this
     *                       adapter is linked to
     */
    public ExpandDragRecyclerAdapter(@NonNull List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
    }

    /**
     * Implementation of Adapter.onCreateViewHolder(ViewGroup, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either {@link #onCreateParentViewHolder(ViewGroup)}
     * or {@link #onCreateChildViewHolder(ViewGroup)}.
     *
     * @param viewGroup The {@link ViewGroup} into which the new {@link View}
     *                  will be added after it is bound to an adapter position.
     * @param viewType  The view type of the new {@code android.view.View}.
     * @return A new RecyclerView.ViewHolder
     * that holds a {@code android.view.View} of the given view type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            return onCreateLoadMoreViewHolder(viewGroup);
        } else {
            return super.onCreateViewHolder(viewGroup, viewType);
        }
    }

    /**
     * Implementation of Adapter.onBindViewHolder(RecyclerView.ViewHolder, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either
     * {@link #onBindParentViewHolder(ParentViewHolder, int, ParentListItem)}
     * or {@link #onBindChildViewHolder(ChildViewHolder, int, Object)}.
     *
     * @param holder   The RecyclerView.ViewHolder to bind data to
     * @param position The index in the list at which to bind
     * @throws IllegalStateException if the item in the list is either null or
     *                               not of type {@link ParentListItem}
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            PVH parentViewHolder = (PVH) holder;

            if (parentViewHolder.shouldItemViewClickToggleExpansion()) {
                parentViewHolder.setMainItemClickToExpand();
            }
            parentViewHolder.itemView.setTag(position);
            parentViewHolder.itemView.setOnDragListener(this);
            ParentWrapper parentWrapper = (ParentWrapper) listItem;
            parentViewHolder.setExpanded(parentWrapper.isExpanded());
            onBindParentViewHolder(parentViewHolder, position, parentWrapper.getParentListItem());
        } else if (listItem != null && listItem instanceof LoadMoreStatus) {
            LVH loadMoreViewHolder = (LVH) holder;
            loadMoreViewHolder.itemView.setTag(position);
            loadMoreViewHolder.itemView.setOnClickListener(this);
            loadMoreViewHolder.update((LoadMoreStatus) listItem);
            onBindLoadMoreViewHolder((LVH) holder, position, getParentPosition(position), listItem);

        } else if (listItem == null) {
            throw new IllegalStateException("Incorrect ViewHolder found");
        } else {
            onBindChildViewHolder((CVH) holder, position, listItem);
            holder.itemView.setTag(position);
            holder.itemView.setOnLongClickListener(this);
            holder.itemView.setOnTouchListener(this);
        }
    }

    /**
     * Create the load more view holder
     *
     * @param viewGroup
     * @return
     */
    public abstract LVH onCreateLoadMoreViewHolder(ViewGroup viewGroup);

    /**
     * Bind the load more view holder
     *
     * @param viewHolder
     * @param position
     * @param parentIndex
     * @param object
     */
    public abstract void onBindLoadMoreViewHolder(LVH viewHolder, int position, int parentIndex, Object object);

    /**
     * Gets the view type of the item at the given position.
     *
     * @param position The index in the list to get the view type of
     * @return {@value #TYPE_PARENT} for {@link ParentListItem} and {@value #TYPE_CHILD}
     * for child list items
     * @throws IllegalStateException if the item at the given position in the list is null
     */
    @Override
    public int getItemViewType(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof LoadMoreStatus) {
            return TYPE_LOAD_MORE;
        } else
            return super.getItemViewType(position);
    }


    /**
     * 设置拖拽选中回调
     *
     * @param dragSelectCallback
     */
    public void setDragSelectCallback(DragSelectCallback dragSelectCallback) {
        mDragSelectCallback = dragSelectCallback;
    }

    /**
     * 设置加载更多地回调
     *
     * @param loadMoreListener
     */
    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }


    /**
     * Expands all parents in the list.
     */
    public void expandAllParents(int fromParenIndex) {
        if (fromParenIndex >= 0 && fromParenIndex < mParentItemList.size()) {
            expandParent(fromParenIndex);
            Log.d(TAG, "expand=" + fromParenIndex);
        }
        int scrollPosition = 0;
        ParentListItem parentListItem = null;
        for (int i = 0; i < mParentItemList.size(); i++) {
            if (i < fromParenIndex) {
                expandParent(i);
                Log.d(TAG, "expand=" + fromParenIndex);
                parentListItem = mParentItemList.get(i);
                int childCount = parentListItem.getChildItemList().size();
                if (parentListItem.isLoadMore()) {
                    childCount += 1;
                }
                scrollPosition += childCount + i;
                Log.d(TAG, "scrollTo=" + scrollPosition);
                scrollToPosition(scrollPosition);
            } else {
                Log.d(TAG, "expand=" + fromParenIndex);
                expandParent(i);
            }
        }
    }

    private void scrollToPosition(int position) {
        RecyclerView.LayoutManager layoutManager = null;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, 0);
            }
        }
    }



    /**
     * Fetches the expandable state map from the saved instance state {@link Bundle}
     * and restores the expanded states of all of the list items.
     * <p>
     * Should be called from {@link Activity#onRestoreInstanceState(Bundle)} in
     * the {@link Activity} that hosts the RecyclerView that this
     * {@link ExpandDragRecyclerAdapter} is attached to.
     * <p>
     * Assumes that the list of parent list items is the same as when the saved
     * instance state was stored.
     *
     * @param savedInstanceState The {@code Bundle} from which the expanded
     *                           state map is loaded
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null
                || !savedInstanceState.containsKey(EXPANDED_STATE_MAP)) {
            return;
        }

        HashMap<Integer, Boolean> expandedStateMap = (HashMap<Integer, Boolean>) savedInstanceState.getSerializable
                (EXPANDED_STATE_MAP);
        if (expandedStateMap == null) {
            return;
        }

        List<Object> parentWrapperList = new ArrayList<>();
        ParentListItem parentListItem;
        ParentWrapper parentWrapper;

        int parentListItemCount = mParentItemList.size();
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = mParentItemList.get(i);
            parentWrapper = new ParentWrapper(parentListItem);
            parentWrapperList.add(parentWrapper);

            if (expandedStateMap.containsKey(i)) {
                boolean expanded = expandedStateMap.get(i);
                if (expanded) {
                    parentWrapper.setExpanded(true);

                    int childListItemCount = parentWrapper.getChildItemList().size();
                    for (int j = 0; j < childListItemCount; j++) {
                        parentWrapperList.add(parentWrapper.getChildItemList().get(j));
                    }

                    if (parentListItem.isLoadMore())
                        parentWrapperList.add(parentListItem.getLoadingStatus());
                }
            }

        }

        mItemList = parentWrapperList;

        notifyDataSetChanged();
    }


    /**
     * Calls through to the ParentViewHolder to expand views for each
     * RecyclerView the specified parent is a child of.
     * <p>
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to expand
     */
    private void expandViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && !viewHolder.isExpanded()) {
                viewHolder.setExpanded(true);
                viewHolder.itemView.setTag(parentIndex);
                viewHolder.onExpansionToggled(false);
            }

            expandParentListItem(parentWrapper, parentIndex, false);

        }
    }

    /**
     * Calls through to the ParentViewHolder to collapse views for each
     * RecyclerView a specified parent is a child of.
     * <p>
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to collapse
     */
    private void collapseViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && viewHolder.isExpanded()) {
                viewHolder.setExpanded(false);
                viewHolder.itemView.setTag(parentIndex);
                viewHolder.onExpansionToggled(true);
            }

            collapseParentListItem(parentWrapper, parentIndex, false);
        }
    }

    /**
     * Calls through to the ParentViewHolder to collapse views for each
     * RecyclerView a specified parent is a child of.
     * <p>
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to collapse
     */
    private void collapseParentViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && viewHolder.isExpanded()) {
                viewHolder.setExpanded(false);
                viewHolder.itemView.setTag(parentIndex);
                viewHolder.onExpansionToggled(true);
            }
        }
    }

    /**
     * Expands a specified parent item. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * total list of items.
     *
     * @param parentWrapper                     The ParentWrapper of the parent to expand
     * @param parentIndex                       The index of the parent to expand
     * @param expansionTriggeredByListItemClick true if expansion was triggered
     *                                          by a click event, false otherwise.
     */

    public void expandParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean
            expansionTriggeredByListItemClick) {
        if (!parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(true);

            List<?> childItemList = parentWrapper.getChildItemList();
            if (childItemList != null) {
                int childListItemCount = childItemList.size();
                for (int i = 0; i < childListItemCount; i++) {
                    mItemList.add(parentIndex + i + 1, childItemList.get(i));
                }
                if (parentWrapper.getParentListItem() != null && parentWrapper.getParentListItem().isLoadMore()) {
                    childListItemCount += 1;
                    mItemList.add(parentIndex + childListItemCount, parentWrapper.getParentListItem()
                            .getLoadingStatus());
                }

                notifyItemRangeInserted(parentIndex + 1, childListItemCount);
            }

            if (expansionTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemExpanded(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    /**
     * Collapses a specified parent item. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * total list of items.
     *
     * @param parentWrapper                    The ParentWrapper of the parent to collapse
     * @param parentIndex                      The index of the parent to collapse
     * @param collapseTriggeredByListItemClick true if expansion was triggered
     *                                         by a click event, false otherwise.
     */
    private void collapseParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean
            collapseTriggeredByListItemClick) {
        if (parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(false);

            List<?> childItemList = parentWrapper.getChildItemList();
            if (childItemList != null) {
                int childListItemCount = childItemList.size();


                for (int i = childListItemCount - 1; i >= 0; i--) {
                    mItemList.remove(parentIndex + i + 1);
                }
                if (parentWrapper.getParentListItem() != null && parentWrapper.getParentListItem().isLoadMore()) {
                    mItemList.remove(parentIndex + 1);
                    childListItemCount += 1;
                }

                notifyItemRangeRemoved(parentIndex + 1, childListItemCount);
            }

            if (collapseTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemCollapsed(parentIndex - expandedCountBeforePosition);
            }
        }
    }



    // region Data Manipulation


    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has a child list item that has been newly inserted at {@code childPosition}.
     * The child list item previously at {@code childPosition} is now at
     * position {@code childPosition + 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has been added a child, relative
     *                       to list of ParentListItems only.
     * @param childPosition  Position of the child object that has been inserted, relative to children
     *                       of the ParentListItem specified by {@code parentPosition} only.
     */
    public void notifyChildItemInserted(int parentPosition, int childPosition) {
        notifyChildItemRangeInserted(parentPosition, childPosition, 1);
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has {@code itemCount} child list items that have been newly inserted at {@code childPositionStart}.
     * The child list item previously at {@code childPositionStart} and beyond are now at
     * position {@code childPositionStart + itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition     Position of the ParentListItem which has been added a child, relative
     *                           to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been inserted,
     *                           relative to children of the ParentListItem specified by
     *                           {@code parentPosition} only.
     * @param itemCount          number of children inserted
     */
    public void notifyChildItemRangeInserted(int parentPosition, int childPositionStart, int itemCount) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);


        if (parentWrapper.isExpanded()) {
            ParentListItem parentListItem = mParentItemList.get(parentPosition);
            List<?> childList = parentListItem.getChildItemList();
            Object child;
            for (int i = 0; i < itemCount; i++) {
                child = childList.get(childPositionStart + i);
                mItemList.add(parentWrapperIndex + childPositionStart + i + 1, child);
            }
            // check this parent item did or not have the
            int count = itemCount;
            if (parentListItem.isLoadMore()) {
                int p = parentWrapperIndex + childPositionStart + 1 + itemCount;
                if (p >= 0 && p < mItemList.size() && mItemList.get(p) instanceof LoadMoreStatus) {
                    // do nothing
                } else {
                    mItemList.add(p, parentListItem.getLoadingStatus());
                    count += 1;
                }
            }
            notifyItemRangeInserted(parentWrapperIndex + childPositionStart + 1, count);

            if (parentWrapper.getParentListItem() != null && parentWrapper.getParentListItem().isLoadMore()) {
                int p = parentWrapperIndex + childPositionStart + 1 + itemCount;
                if (p >= 0 && p < mItemList.size() && mItemList.get(p) instanceof LoadMoreStatus) {
                    mItemList.set(p, parentWrapper.getParentListItem().getLoadingStatus());
                    notifyItemChanged(p);
                }
            }
        }

    }



    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * {@code itemCount} child Objects starting at {@code childPositionStart} that have changed.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * The ParentListItem at {@code childPositionStart} retains the same identity.
     * reflection of the set of {@code itemCount} child objects starting at {@code childPositionStart}
     * are out of date and should be updated.
     *
     * @param parentPosition     Position of the ParentListItem who has a child that has changed
     * @param childPositionStart Position of the first child object that has changed
     * @param itemCount          number of child objects changed
     */
    public void notifyChildItemRangeChanged(int parentPosition, int childPositionStart, int itemCount) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            int listChildPosition = parentWrapperIndex + childPositionStart + 1;
            for (int i = 0; i < itemCount; i++) {
                Object child = parentWrapper.getChildItemList().get(childPositionStart + i);
                mItemList.set(listChildPosition + i, child);

            }
            notifyItemRangeChanged(listChildPosition, itemCount);
        }
    }

    // endregion

    /**
     * Get the parent position of the child.
     *
     * @param position
     * @return
     */
    private int getParentPosition(int position) {
        int parent = -1;
        for (int i = 0; i < mItemList.size(); i++) {
            Object listItem = mItemList.get(i);
            if (listItem instanceof ParentWrapper) {
                parent++;
            }
            if (i >= position)
                break;
        }
        return parent;
    }

    /**
     * Gets the ParentWrapper for a specified ParentListItem from the list of
     * parents.
     *
     * @param parentListItem A ParentListItem in the list of parents
     * @return If the parent exists on the list, returns its ParentWrapper.
     * Otherwise, returns null.
     */
    private ParentWrapper getParentWrapper(ParentListItem parentListItem) {
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            Object listItem = mItemList.get(i);
            if (listItem instanceof ParentWrapper) {
                if (((ParentWrapper) listItem).getParentListItem().equals(parentListItem)) {
                    return (ParentWrapper) listItem;
                }
            }
        }

        return null;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        int toPosition = Integer.valueOf(v.getTag().toString());
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
                    return true;
                }
            case DragEvent.ACTION_DRAG_ENTERED:
                if (mDragSelectCallback != null) {
                    mDragSelectCallback.onListItemSelected(v, toPosition);
                }
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                if (mDragSelectCallback != null) {
                    mDragSelectCallback.onListItemUnSelected(v, toPosition);
                }
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;
            case DragEvent.ACTION_DROP:
                if (mDragSelectCallback != null) {
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    Intent intent = item.getIntent();
                    if (intent != null && intent.hasExtra(FROM_POSITION)) {
                        int fromPosition = intent.getIntExtra(FROM_POSITION, 0);
                        int fromParentPosition = intent.getIntExtra(FROM_PARENT_POSITION, 0);
                        int fromChildPositionOfParent = intent.getIntExtra(FROM_CHILD_POSITION, 0);
                        mDragSelectCallback.onListItemDrop(fromPosition, fromParentPosition, fromChildPositionOfParent,
                                toPosition);
                    }
                    mDragSelectCallback.onListItemUnSelected(v, toPosition);

                    expandAllParents(Integer.valueOf(v
                            .getTag().toString()));
                    Log.d(TAG, "toPosition" + "= " + Integer.valueOf(v
                            .getTag().toString()));
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d(TAG, "DRAG_ENdED" + Integer.valueOf(v
                        .getTag().toString()));
                return true;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {

        // 获取到相应的位置, 并将其保存在对应的intent中.
        int fromPosition = Integer.valueOf(v.getTag().toString());

        int fromParentPosition = 0;
        int fromChildPositionOfParent = 0;
        int parent = -1;

        ParentWrapper parentWrapper = null;
        for (int i = 0; i < mItemList.size(); i++) {
            Object listItem = mItemList.get(i);
            if (listItem instanceof ParentWrapper) {
                parent++;
                parentWrapper = (ParentWrapper) listItem;
                if (parentWrapper.isExpanded()) {
                    if (fromPosition - i > 0 && parentWrapper.getChildItemList() != null && fromPosition
                            - i <= parentWrapper.getChildItemList().size()) {
                        fromParentPosition = parent;
                        fromChildPositionOfParent = fromPosition - i - 1;
                        break;
                    }
                }
            }
        }
        Intent intent = new Intent();
        intent.putExtra(FROM_POSITION, fromPosition);
        intent.putExtra(FROM_PARENT_POSITION, fromParentPosition);
        intent.putExtra(FROM_CHILD_POSITION, fromChildPositionOfParent);
        Log.d(TAG, FROM_POSITION + "= " + fromPosition);
        Log.d(TAG, FROM_PARENT_POSITION + "= " + fromParentPosition);
        Log.d(TAG, FROM_CHILD_POSITION + "= " + fromChildPositionOfParent);


        // 收起所有的View
        collapseAllParents();
        // 延时更新未更新的parentWrapper, 仅仅更新其收起的状态
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getItemCount(); i++) {
                    if (mItemList.get(i) instanceof ParentWrapper) {
                        collapseParentViews(((ParentWrapper) mItemList.get(i)), i);
                    }
                }
            }
        }, 100);

        // 传递相应的数据, 设置开始脱宅所需要的数据, 并开始拖拽.
        ClipData dragData = ClipData.newIntent(FROM_POSITION_DATA, intent);

        // Instantiates the drag shadow builder.
        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v, mTouchX, mTouchY);

        // Starts the drag

        v.startDrag(dragData,  // the data to be dragged
                myShadow,  // the drag shadow builder
                null,      // no need to use local data
                0          // flags (not currently used, set to 0)
        );
        return true;
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private Drawable shadow;
        private float mTouchX;
        private float mTouchY;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v, float touchX, float touchY) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
//            shadow = new ColorDrawable(Color.LTGRAY);z
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
            shadow = new BitmapDrawable(bitmap);
            v.setDrawingCacheEnabled(false);

            mTouchX = touchX;
            mTouchY = touchY;
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {

            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth();

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight();

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set((int) (mTouchX - 20 > 0 ? mTouchX - 20 : 0), (int) (mTouchY - 20 > 0 ? mTouchY - 20 : 0));
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }

    private float mTouchX = 0;
    private float mTouchY = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                mTouchY = event.getY();
                break;
        }
        return false;
    }

    /**
     * The load more item view click event.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getTag() != null && mLoadMoreListener != null) {
            int position = Integer.valueOf(v.getTag().toString());
            int parentPosition = getParentPosition(position);
            if (parentPosition > -1 && parentPosition < mParentItemList.size()) {
                if (mParentItemList.get(parentPosition).getLoadingStatus() != LoadMoreStatus.FINISH) {
                    mParentItemList.get(parentPosition).setLoadMoreStatus(LoadMoreStatus.LOADING);
                    mItemList.set(position, LoadMoreStatus.LOADING);
                    notifyItemChanged(position);
                    mLoadMoreListener.loadMore(parentPosition);
                }
            }

        }
    }
}
