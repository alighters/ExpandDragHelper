package com.lighters.library.expanddrag.callback;

/**
 * Allows objects to register themselves as expand/collapse listeners to be
 * notified of change events.
 * <p/>
 * to receive these callbacks.
 */
public interface ExpandCollapseListener {

    /**
     * Called when a list item is expanded.
     *
     * @param position The index of the item in the list being expanded
     */
    void onListItemExpanded(int position);

    /**
     * Called when a list item is collapsed.
     *
     * @param position The index of the item in the list being collapsed
     */
    void onListItemCollapsed(int position);

}
