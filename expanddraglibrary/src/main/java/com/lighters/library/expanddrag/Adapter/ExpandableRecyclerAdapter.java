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
import com.lighters.library.expanddrag.callback.ExpandCollapseListener;
import com.lighters.library.expanddrag.callback.LoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RecyclerView.Adapter implementation that
 * adds the ability to expand and collapse list items.
 * <p/>
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
public abstract class ExpandableRecyclerAdapter<PVH extends ParentViewHolder, CVH extends ChildViewHolder, LVH
        extends LoadMoreViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ParentViewHolder
        .ParentListItemExpandCollapseListener, View.OnDragListener, View.OnLongClickListener, View.OnTouchListener,
        View.OnClickListener {

    private static final String TAG = ExpandableRecyclerAdapter.class.getName() + "_tag";

    private static final String EXPANDED_STATE_MAP = "ExpandableRecyclerAdapter.ExpandedStateMap";
    private static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;
    private static final int TYPE_LOAD_MORE = 2;

    private static final String FROM_POSITION = "from_position";
    private static final String FROM_PARENT_POSITION = "from_parent_position";
    private static final String FROM_CHILD_POSITION = "from_child_position";
    private static final String FROM_POSITION_DATA = "from_position_data";

    /**
     * A {@link List} of all currently expanded {@link ParentListItem} objects
     * and their children, in order. Changes to this list should be made through the add/remove methods
     * available in {@link ExpandableRecyclerAdapter}
     */
    protected List<Object> mItemList;

    private List<? extends ParentListItem> mParentItemList;
    private ExpandCollapseListener mExpandCollapseListener;
    private DragSelectCallback mDragSelectCallback;
    private LoadMoreListener mLoadMoreListener;
    private List<RecyclerView> mAttachedRecyclerViewPool;

    /**
     * Primary constructor. Sets up {@link #mParentItemList} and {@link #mItemList}.
     * <p/>
     * Changes to {@link #mParentItemList} should be made through add/remove methods in
     * {@link ExpandableRecyclerAdapter}
     *
     * @param parentItemList List of all {@link ParentListItem} objects to be
     *                       displayed in the RecyclerView that this
     *                       adapter is linked to
     */
    public ExpandableRecyclerAdapter(@NonNull List<? extends ParentListItem> parentItemList) {
        super();
        mParentItemList = parentItemList;
        mItemList = ExpandableRecyclerAdapterHelper.generateParentChildItemList(parentItemList);
        mAttachedRecyclerViewPool = new ArrayList<>();
    }

    /**
     * Implementation of Adapter.onCreateViewHolder(ViewGroup, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either {@link #onCreateParentViewHolder(ViewGroup)}
     * or {@link #onCreateChildViewHolder(ViewGroup)}.
     *
     * @param viewGroup The {@link ViewGroup} into which the new {@link android.view.View}
     *                  will be added after it is bound to an adapter position.
     * @param viewType  The view type of the new {@code android.view.View}.
     * @return A new RecyclerView.ViewHolder
     * that holds a {@code android.view.View} of the given view type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_PARENT) {
            PVH pvh = onCreateParentViewHolder(viewGroup);
            pvh.setParentListItemExpandCollapseListener(this);
            return pvh;
        } else if (viewType == TYPE_CHILD) {
            return onCreateChildViewHolder(viewGroup);
        } else if (viewType == TYPE_LOAD_MORE) {
            return onCreateLoadMoreViewHolder(viewGroup);
        } else {
            throw new IllegalStateException("Incorrect ViewType found");
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
     * Callback called from {@link #onCreateViewHolder(ViewGroup, int)} when
     * the list item created is a parent.
     *
     * @param parentViewGroup The {@link ViewGroup} in the list for which a {@link PVH}
     *                        is being created
     * @return A {@code PVH} corresponding to the {@link ParentListItem} with
     * the {@code ViewGroup} parentViewGroup
     */
    public abstract PVH onCreateParentViewHolder(ViewGroup parentViewGroup);

    /**
     * Callback called from {@link #onCreateViewHolder(ViewGroup, int)} when
     * the list item created is a child.
     *
     * @param childViewGroup The {@link ViewGroup} in the list for which a {@link CVH}
     *                       is being created
     * @return A {@code CVH} corresponding to the child list item with the
     * {@code ViewGroup} childViewGroup
     */
    public abstract CVH onCreateChildViewHolder(ViewGroup childViewGroup);

    /**
     * Create the load more view holder
     *
     * @param viewGroup
     * @return
     */
    public LVH onCreateLoadMoreViewHolder(ViewGroup viewGroup) {
        return null;
    }

    /**
     * Bind the load more view holder
     *
     * @param viewHolder
     * @param position
     * @param parentIndex
     * @param object
     */
    public void onBindLoadMoreViewHolder(LVH viewHolder, int position, int parentIndex, Object object) {

    }

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a parent.
     * <p/>
     * Bind data to the {@link PVH} here.
     *
     * @param parentViewHolder The {@code PVH} to bind data to
     * @param position         The index in the list at which to bind
     * @param parentListItem   The {@link ParentListItem} which holds the data to
     *                         be bound to the {@code PVH}
     */
    public abstract void onBindParentViewHolder(PVH parentViewHolder, int position, ParentListItem parentListItem);

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a child.
     * <p/>
     * Bind data to the {@link CVH} here.
     *
     * @param childViewHolder The {@code CVH} to bind data to
     * @param position        The index in the list at which to bind
     * @param childListItem   The child list item which holds that data to be
     *                        bound to the {@code CVH}
     */
    public abstract void onBindChildViewHolder(CVH childViewHolder, int position, Object childListItem);

    /**
     * Gets the number of parent and child objects currently expanded.
     *
     * @return The size of {@link #mItemList}
     */
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

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
        if (listItem instanceof ParentWrapper) {
            return TYPE_PARENT;
        } else if (listItem instanceof LoadMoreStatus) {
            return TYPE_LOAD_MORE;
        } else if (listItem == null) {
            throw new IllegalStateException("Null object added");
        } else {
            return TYPE_CHILD;
        }
    }

    /**
     * Gets the list of ParentItems that is backing this adapter.
     * Changes can be made to the list and the adapter notified via the
     * {@link #notifyParentItemInserted(int)}
     * {@link #notifyParentItemRemoved(int)}
     * {@link #notifyParentItemChanged(int)}
     * {@link #notifyParentItemRangeInserted(int, int)}
     * {@link #notifyChildItemInserted(int, int)}
     * {@link #notifyChildItemRemoved(int, int)}
     * {@link #notifyChildItemChanged(int, int)}
     * methods.
     *
     * @return The list of ParentListItems that this adapter represents
     */
    public List<? extends ParentListItem> getParentItemList() {
        return mParentItemList;
    }

    /**
     * Implementation of
     * <p/>
     * Called when a {@link ParentListItem} is triggered to expand.
     *
     * @param position The index of the item in the list being expanded
     */
    @Override
    public void onParentListItemExpanded(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            expandParentListItem((ParentWrapper) listItem, position, true);
        }
    }

    /**
     * Implementation of
     * <p/>
     * Called when a {@link ParentListItem} is triggered to collapse.
     *
     * @param position The index of the item in the list being collapsed
     */
    @Override
    public void onParentListItemCollapsed(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            collapseParentListItem((ParentWrapper) listItem, position, true);
        }
    }

    /**
     * Implementation of Adapter#onAttachedToRecyclerView(RecyclerView).
     * <p/>
     * Called when this {@link ExpandableRecyclerAdapter} is attached to a RecyclerView.
     *
     * @param recyclerView The {@code RecyclerView} this {@code ExpandableRecyclerAdapter}
     *                     is being attached to
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.add(recyclerView);
    }

    /**
     * Implementation of Adapter.onDetachedFromRecyclerView(RecyclerView)
     * <p/>
     * Called when this ExpandableRecyclerAdapter is detached from a RecyclerView.
     *
     * @param recyclerView The {@code RecyclerView} this {@code ExpandableRecyclerAdapter}
     *                     is being detached from
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.remove(recyclerView);
    }

    /****
     * 设置收起展开Listener
     *
     * @param expandCollapseListener
     */
    public void setExpandCollapseListener(ExpandCollapseListener expandCollapseListener) {
        mExpandCollapseListener = expandCollapseListener;
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
     * Expands the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to expand
     */
    public void expandParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object listItem = getListItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (listItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) listItem;
        } else {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Expands the parent associated with a specified {@link ParentListItem} in
     * the list of parents.
     *
     * @param parentListItem The {@code ParentListItem} of the parent to expand
     */
    public void expandParent(ParentListItem parentListItem) {
        ParentWrapper parentWrapper = getParentWrapper(parentListItem);
        int parentWrapperIndex = mItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Expands all parents in a range of indices in the list of parents.
     *
     * @param startParentIndex The index at which to to start expanding parents
     * @param parentCount      The number of parents to expand
     */
    public void expandParentRange(int startParentIndex, int parentCount) {
        int endParentIndex = startParentIndex + parentCount;
        for (int i = startParentIndex; i < endParentIndex; i++) {
            expandParent(i);
        }
    }

    /**
     * Expands all parents in the list.
     */
    public void expandAllParents() {
        for (ParentListItem parentListItem : mParentItemList) {
            expandParent(parentListItem);
        }
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
     * Collapses the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to collapse
     */
    public void collapseParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object listItem = getListItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (listItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) listItem;
        } else {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Collapses the parent associated with a specified {@link ParentListItem} in
     * the list of parents.
     *
     * @param parentListItem The {@code ParentListItem} of the parent to collapse
     */
    public void collapseParent(ParentListItem parentListItem) {
        ParentWrapper parentWrapper = getParentWrapper(parentListItem);
        int parentWrapperIndex = mItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Collapses all parents in a range of indices in the list of parents.
     *
     * @param startParentIndex The index at which to to start collapsing parents
     * @param parentCount      The number of parents to collapse
     */
    public void collapseParentRange(int startParentIndex, int parentCount) {
        int endParentIndex = startParentIndex + parentCount;
        for (int i = startParentIndex; i < endParentIndex; i++) {
            collapseParent(i);
        }
    }

    /**
     * Collapses all parents in the list.
     */
    public void collapseAllParents() {
        for (ParentListItem parentListItem : mParentItemList) {
            collapseParent(parentListItem);
        }
    }

    /**
     * Stores the expanded state map across state loss.
     * <p/>
     * Should be called from {@link Activity#onSaveInstanceState(Bundle)} in
     * the {@link Activity} that hosts the RecyclerView that this
     * {@link ExpandableRecyclerAdapter} is attached to.
     * <p/>
     * This will make sure to add the expanded state map as an extra to the
     * instance state bundle to be used in {@link #onRestoreInstanceState(Bundle)}.
     *
     * @param savedInstanceState The {@code Bundle} into which to store the
     *                           expanded state map
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(EXPANDED_STATE_MAP, generateExpandedStateMap());
    }

    /**
     * Fetches the expandable state map from the saved instance state {@link Bundle}
     * and restores the expanded states of all of the list items.
     * <p/>
     * Should be called from {@link Activity#onRestoreInstanceState(Bundle)} in
     * the {@link Activity} that hosts the RecyclerView that this
     * {@link ExpandableRecyclerAdapter} is attached to.
     * <p/>
     * Assumes that the list of parent list items is the same as when the saved
     * instance state was stored.
     *
     * @param savedInstanceState The {@code Bundle} from which the expanded
     *                           state map is loaded
     */
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
     * Gets the list item held at the specified adapter position.
     *
     * @param position The index of the list item to return
     * @return The list item at the specified position
     */
    protected Object getListItem(int position) {
        boolean indexInRange = position >= 0 && position < mItemList.size();
        if (indexInRange) {
            return mItemList.get(position);
        } else {
            return null;
        }
    }

    /**
     * Calls through to the ParentViewHolder to expand views for each
     * RecyclerView the specified parent is a child of.
     * <p/>
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
     * <p/>
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
     * <p/>
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
    private void expandParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean
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

    /**
     * Gets the number of expanded child list items before the specified position.
     *
     * @param position The index before which to return the number of expanded
     *                 child list items
     * @return The number of expanded child list items before the specified position
     */
    private int getExpandedItemCount(int position) {
        if (position == 0) {
            return 0;
        }

        int expandedCount = 0;
        for (int i = 0; i < position; i++) {
            Object listItem = getListItem(i);
            if (!(listItem instanceof ParentWrapper)) {
                expandedCount++;
            }
        }
        return expandedCount;
    }

    // endregion

    // region Data Manipulation

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has been newly inserted. The ParentListItem previously at {@code parentPosition} is now at
     * position {@code parentPosition + 1}.
     * <p/>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the newly inserted ParentListItem in the data set, relative
     *                       to list of ParentListItems only.
     * @see #notifyParentItemRangeInserted(int, int)
     */
    public void notifyParentItemInserted(int parentPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);

        int wrapperIndex;
        if (parentPosition < mParentItemList.size() - 1) {
            wrapperIndex = getParentWrapperIndex(parentPosition);
        } else {
            wrapperIndex = mItemList.size();
        }

        int sizeChanged = addParentWrapper(wrapperIndex, parentListItem);
        notifyItemRangeInserted(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the currently reflected {@code itemCount}
     * ParentListItems starting at {@code parentPositionStart} have been newly inserted.
     * The ParentListItems previously located at {@code parentPositionStart} and beyond
     * can now be found starting at position {@code parentPositionStart + itemCount}.
     * <p/>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart Position of the first ParentListItem that was inserted, relative
     *                            to list of ParentListItems only.
     * @param itemCount           Number of items inserted
     * @see #notifyParentItemInserted(int)
     */
    public void notifyParentItemRangeInserted(int parentPositionStart, int itemCount) {
        int initialWrapperIndex;
        if (parentPositionStart < mParentItemList.size() - itemCount) {
            initialWrapperIndex = getParentWrapperIndex(parentPositionStart);
        } else {
            initialWrapperIndex = mItemList.size();
        }

        int sizeChanged = 0;
        int wrapperIndex = initialWrapperIndex;
        int changed;
        int parentPositionEnd = parentPositionStart + itemCount;
        for (int i = parentPositionStart; i < parentPositionEnd; i++) {
            ParentListItem parentListItem = mParentItemList.get(i);
            changed = addParentWrapper(wrapperIndex, parentListItem);
            wrapperIndex += changed;
            sizeChanged += changed;
        }

        notifyItemRangeInserted(initialWrapperIndex, sizeChanged);
    }

    private int addParentWrapper(int wrapperIndex, ParentListItem parentListItem) {
        int sizeChanged = 1;
        ParentWrapper parentWrapper = new ParentWrapper(parentListItem);
        mItemList.add(wrapperIndex, parentWrapper);
        if (parentWrapper.isInitiallyExpanded()) {
            parentWrapper.setExpanded(true);
            List<?> childItemList = parentWrapper.getChildItemList();
            mItemList.addAll(wrapperIndex + sizeChanged, childItemList);
            sizeChanged += childItemList.size();
        }
        return sizeChanged;
    }

    /**
     * Notify any registered observers that the ParentListItem previously located at {@code parentPosition}
     * has been removed from the data set. The ParentListItems previously located at and after
     * {@code parentPosition} may now be found at {@code oldPosition - 1}.
     * <p/>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem that has now been removed, relative
     *                       to list of ParentListItems only.
     */
    public void notifyParentItemRemoved(int parentPosition) {
        int wrapperIndex = getParentWrapperIndex(parentPosition);
        int sizeChanged = removeParentWrapper(wrapperIndex);

        notifyItemRangeRemoved(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the {@code itemCount} ParentListItems previously located
     * at {@code parentPositionStart} have been removed from the data set. The ParentListItems
     * previously located at and after {@code parentPositionStart + itemCount} may now be found at
     * {@code oldPosition - itemCount}.
     * <p/>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart The previous position of the first ParentListItem that was
     *                            removed, relative to list of ParentListItems only.
     * @param itemCount           Number of ParentListItems removed from the data set
     */
    public void notifyParentItemRangeRemoved(int parentPositionStart, int itemCount) {
        int sizeChanged = 0;
        int wrapperIndex = getParentWrapperIndex(parentPositionStart);
        for (int i = 0; i < itemCount; i++) {
            sizeChanged += removeParentWrapper(wrapperIndex);
        }

        notifyItemRangeRemoved(wrapperIndex, sizeChanged);
    }

    private int removeParentWrapper(int parentWrapperIndex) {
        int sizeChanged = 1;
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.remove(parentWrapperIndex);
        if (parentWrapper.isExpanded()) {
            int childListSize = parentWrapper.getChildItemList().size();
            for (int i = 0; i < childListSize; i++) {
                mItemList.remove(parentWrapperIndex);
                sizeChanged++;
            }
        }
        return sizeChanged;
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has changed.
     * This will also trigger an item changed for children of the ParentList specified.
     * <p/>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at {@code parentPosition} is out of date and should be updated.
     * The ParentListItem at {@code parentPosition} retains the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPosition Position of the item that has changed
     */
    public void notifyParentItemChanged(int parentPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int wrapperIndex = getParentWrapperIndex(parentPosition);
        int sizeChanged = changeParentWrapper(wrapperIndex, parentListItem);

        notifyItemRangeChanged(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the {@code itemCount} ParentListItems starting
     * at {@code parentPositionStart} have changed. This will also trigger an item changed
     * for children of the ParentList specified.
     * <p/>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data in the given position range is out of date and should be updated.
     * The ParentListItems in the given range retain the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPositionStart Position of the item that has changed
     * @param itemCount           Number of ParentListItems changed in the dataset
     */
    public void notifyParentItemRangeChanged(int parentPositionStart, int itemCount) {
        int initialWrapperIndex = getParentWrapperIndex(parentPositionStart);

        int wrapperIndex = initialWrapperIndex;
        int sizeChanged = 0;
        int changed;
        ParentListItem parentListItem;
        for (int j = 0; j < itemCount; j++) {
            parentListItem = mParentItemList.get(parentPositionStart);
            changed = changeParentWrapper(wrapperIndex, parentListItem);
            sizeChanged += changed;
            wrapperIndex += changed;
            parentPositionStart++;
        }
        notifyItemRangeChanged(initialWrapperIndex, sizeChanged);
    }

    private int changeParentWrapper(int wrapperIndex, ParentListItem parentListItem) {
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(wrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        int sizeChanged = 1;
        if (parentWrapper.isExpanded()) {
            List<?> childItems = parentWrapper.getChildItemList();
            int childListSize = childItems.size();
            Object child;
            for (int i = 0; i < childListSize; i++) {
                child = childItems.get(i);
                mItemList.set(wrapperIndex + i + 1, child);
                sizeChanged++;
            }
        }

        return sizeChanged;

    }

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has a child list item that has been newly inserted at {@code childPosition}.
     * The child list item previously at {@code childPosition} is now at
     * position {@code childPosition + 1}.
     * <p/>
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
     * <p/>
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
            if (childPositionStart == 0 && parentListItem.isLoadMore()) {
                int p = parentWrapperIndex + childPositionStart + 1 + itemCount;
                if (p >= 0 && p < mItemList.size() && mItemList.get(p) instanceof LoadMoreStatus) {
                    // do nothing
                } else {
                    mItemList.add(parentListItem.getLoadingStatus());
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
     * Notify any registered observers that the ParentListItem located at {@code parentPosition}
     * has a child list item that has been removed from the data set, previously located at {@code childPosition}.
     * The child list item previously located at and after {@code childPosition} may
     * now be found at {@code childPosition - 1}.
     * <p/>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has a child removed from, relative
     *                       to list of ParentListItems only.
     * @param childPosition  Position of the child object that has been removed, relative to children
     *                       of the ParentListItem specified by {@code parentPosition} only.
     */
    public void notifyChildItemRemoved(int parentPosition, int childPosition) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            mItemList.remove(parentWrapperIndex + childPosition + 1);
            notifyItemRemoved(parentWrapperIndex + childPosition + 1);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at {@code parentPosition}
     * has {@code itemCount} child list items that have been removed from the data set, previously
     * located at {@code childPositionStart} onwards. The child list item previously located at and
     * after {@code childPositionStart} may now be found at {@code childPositionStart - itemCount}.
     * <p/>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition     Position of the ParentListItem which has a child removed from, relative
     *                           to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been removed, relative
     *                           to children of the ParentListItem specified by
     *                           {@code parentPosition} only.
     * @param itemCount          number of children removed
     */
    public void notifyChildItemRangeRemoved(int parentPosition, int childPositionStart, int itemCount) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            for (int i = 0; i < itemCount; i++) {
                mItemList.remove(parentWrapperIndex + childPositionStart + 1);
            }
            notifyItemRangeRemoved(parentWrapperIndex + childPositionStart + 1, itemCount);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * a child located at {@code childPosition} that has changed.
     * <p/>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at {@code childPosition} is out of date and should be updated.
     * The ParentListItem at {@code childPosition} retains the same identity.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has changed
     * @param childPosition  Position of the child that has changed
     */
    public void notifyChildItemChanged(int parentPosition, int childPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            int listChildPosition = parentWrapperIndex + childPosition + 1;
            Object child = parentWrapper.getChildItemList().get(childPosition);
            mItemList.set(listChildPosition, child);
            notifyItemChanged(listChildPosition);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * {@code itemCount} child Objects starting at {@code childPositionStart} that have changed.
     * <p/>
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
     * Generates a HashMap used to store expanded state for items in the list
     * on configuration change or whenever onResume is called.
     *
     * @return A HashMap containing the expanded state of all parent list items
     */
    private HashMap<Integer, Boolean> generateExpandedStateMap() {
        HashMap<Integer, Boolean> parentListItemHashMap = new HashMap<>();
        int childCount = 0;

        Object listItem;
        ParentWrapper parentWrapper;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) != null) {
                listItem = getListItem(i);
                if (listItem instanceof ParentWrapper) {
                    parentWrapper = (ParentWrapper) listItem;
                    parentListItemHashMap.put(i - childCount, parentWrapper.isExpanded());
                } else {
                    childCount++;
                }
            }
        }

        return parentListItemHashMap;
    }

    /**
     * Gets the index of a ParentWrapper within the helper item list based on
     * the index of the ParentWrapper.
     *
     * @param parentIndex The index of the parent in the list of parent items
     * @return The index of the parent in the list of all views in the adapter
     */
    private int getParentWrapperIndex(int parentIndex) {
        int parentCount = 0;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) instanceof ParentWrapper) {
                parentCount++;

                if (parentCount > parentIndex) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Get the parent position of the child.
     *
     * @param position
     * @return
     */
    private int getParentPosition(int position) {
        int parent = -1;
        ParentWrapper parentWrapper = null;
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
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
                    return true;
                }
            case DragEvent.ACTION_DRAG_ENTERED:
                if (mDragSelectCallback != null) {
                    mDragSelectCallback.onListItemSelected(v);
                }
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                if (mDragSelectCallback != null) {
                    mDragSelectCallback.onListItemUnSelected(v);
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
                        int toPosition = Integer.valueOf(v.getTag().toString());
                        mDragSelectCallback.onListItemDrop(fromPosition, fromParentPosition, fromChildPositionOfParent,
                                toPosition);
                    }
                    mDragSelectCallback.onListItemUnSelected(v);

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
            touch.set((int) (mTouchX - 20), (int) (mTouchY - 20));
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
                    mLoadMoreListener.loadMore(parentPosition);
                }
            }

        }
    }
}
