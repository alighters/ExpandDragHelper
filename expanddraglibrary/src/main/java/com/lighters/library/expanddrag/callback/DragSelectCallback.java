package com.lighters.library.expanddrag.callback;

import android.view.View;

/**
 * Created by david on 16/1/21.
 */
public interface DragSelectCallback {

    /**
     * Called when a list item is selected when drag.
     *
     * @param view The view of the item in the list being selected
     */
    void onListItemSelected(View view);

    /**
     * Called when a list item is unselected when drag.
     *
     * @param view The view of the item in the list being unselected
     */
    void onListItemUnSelected(View view);

    /**
     * Called when the drag proceudure is stopped
     *
     * @param fromTotalPosition
     * @param fromParentPosition
     * @param fromChildPositionOfParent
     * @param toParentPosition
     */
    void onListItemDrop(int fromTotalPosition, int fromParentPosition, int fromChildPositionOfParent, int
            toParentPosition);

}
