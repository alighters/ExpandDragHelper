package com.lighters.library.expanddrag.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * ViewHolder for a child list
 * item.
 * <p/>
 * The user should extend this class and implement as they wish for their
 * child list item.
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 5/27/2015
 */
public class ChildViewHolder extends RecyclerView.ViewHolder {

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public ChildViewHolder(View itemView) {
        super(itemView);
//        itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                // Create a new ClipData.Item from the ImageView object's tag
//                ClipData.Item item = new ClipData.Item(v.getTag());
//
//                // Create a new ClipData using the tag as a label, the plain text MIME type, and
//                // the already-created item. This will create a new ClipDescription object within the
//                // ClipData, and set its MIME type entry to "text/plain"
//                ClipData dragData = new ClipData(v.getTag(), ClipData.newIntent(), item);
//
//                // Instantiates the drag shadow builder.
//                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(imageView);
//
//                // Starts the drag
//
//                v.startDrag(dragData,  // the data to be dragged
//                        myShadow,  // the drag shadow builder
//                        null,      // no need to use local data
//                        0          // flags (not currently used, set to 0)
//                );
//                return false;
//            }
//        });
    }
}