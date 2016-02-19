package com.lighters.library.expanddrag.Model;

import java.util.List;

/**
 * Interface for implementing required methods in a parent list item.
 */
public abstract class ParentListItem {

    public LoadMoreStatus mLoadMoreStatus = LoadMoreStatus.INIT;

    /**
     * Getter for the list of this parent list item's child list items.
     * <p/>
     * If list is empty, the parent list item has no children.
     *
     * @return A {@link List} of the children of this {@link ParentListItem}
     */
    public abstract List<?> getChildItemList();

    /**
     * Getter used to determine if this {@link ParentListItem}'s
     * {@link android.view.View} should show up initially as expanded.
     *
     * @return true if expanded, false if not
     */
    public abstract boolean isInitiallyExpanded();

    /**
     * Getter the parent has more child.
     *
     * @return true if have more child (not show at now), false if not
     */
    public boolean isLoadMore() {
        return false;
    }

    /**
     * Getter the loading status of the parent item.
     */
    public LoadMoreStatus getLoadingStatus() {
        return mLoadMoreStatus;
    }

    /**
     * Setterr the loading status of the parent item.
     */
    public void setLoadMoreStatus(LoadMoreStatus loadMoreStatus) {
        mLoadMoreStatus = loadMoreStatus;
    }
}