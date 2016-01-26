package com.lighters.library.expanddrag.callback;

import android.view.View;

/**
 * Created by david on 16/1/21.
 */
public abstract class DragSelectCallback {

    /**
     * Called when a list item is selected when drag.
     *
     * @param view           The view of the item in the list being selected
     * @param parentPosition The position of the parent item.
     */
    public void onListItemSelected(View view, int parentPosition) {
    }

    /**
     * Called when a list item is unselected when drag.
     *
     * @param view           The view of the item in the list being unselected
     * @param parentPosition The position of the parent item.
     */
    public void onListItemUnSelected(View view, int parentPosition) {
    }

    /**
     * Called when the drag proceudure is stopped
     *
     * @param fromTotalPosition
     * @param fromParentPosition
     * @param fromChildPositionOfParent
     * @param toParentPosition
     */
    public void onListItemDrop(int fromTotalPosition, int fromParentPosition, int fromChildPositionOfParent,
                               int
                                       toParentPosition) {
    }

    /**
     * Called when start the drag event.
     *
     * @param fromPosition       The from position of the drag event.
     * @param fromParentPosition The from parent position of the drag event.
     * @param offsetOfParent     The offset of the parent position when the drag event from.
     */
    public void onStartDrag(int fromPosition, int fromParentPosition, int offsetOfParent) {
    }

    /**
     * Called when the drag event is ended.
     */
    public void onEndDrag() {

    }

}
