package com.lighters.library.expanddrag.callback;

import android.view.View;

/**
 * Created by david on 16/1/21.
 */
public abstract class DragSelectCallback {

    /**
     * Called when a list item is selected when drag.
     *
     * @param view The view of the item in the list being selected
     * @param parentIndex The Index of the parent item.
     */
    public void onListItemSelected(View view, int parentIndex) {
    }

    /**
     * Called when a list item is unselected when drag.
     *
     * @param view The view of the item in the list being unselected
     * @param parentIndex The Index of the parent item.
     */
    public void onListItemUnSelected(View view, int parentIndex) {
    }

    /**
     * Called when a list item move end
     *
     * @param fromParentIndex the index of the parent
     * @param fromChildIndexOfParent the index of the child in the parent list.
     * @param toParentIndex the index of the parent
     */
    public void onListItemMoveEnd(int fromParentIndex, int fromChildIndexOfParent, int toParentIndex) {

    }

    /**
     * Called when the drag proceudure is stopped
     */
    public void onListItemDrop(int fromTotalIndex, int fromParentIndex, int fromChildIndexOfParent,
        int toParentIndex) {
    }

    /**
     * Called when start the drag event.
     *
     * @param fromIndex The from Index of the drag event.
     * @param fromParentIndex The from parent Index of the drag event.
     * @param offsetOfParent The offset of the parent Index when the drag event from.
     */
    public void onStartDrag(int fromIndex, int fromParentIndex, int offsetOfParent) {
    }

    /**
     * Called when the drag event is ended.
     */
    public void onEndDrag() {

    }
}
