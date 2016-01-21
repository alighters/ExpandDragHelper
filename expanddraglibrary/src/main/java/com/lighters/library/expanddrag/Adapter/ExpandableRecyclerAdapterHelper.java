package com.lighters.library.expanddrag.Adapter;

import com.lighters.library.expanddrag.Model.ParentListItem;
import com.lighters.library.expanddrag.Model.ParentWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for {@link ExpandableRecyclerAdapter}.
 * <p/>
 * Created by Ryan Brooks on 6/11/15.
 */
public class ExpandableRecyclerAdapterHelper {

    public static final String PARENT_LOAD_MORE_PREFIX = "parent_load_more_";

    /**
     * Generates a full list of all {@link ParentListItem} objects and their
     * children, in order.
     *
     * @param parentItemList A list of the {@code ParentListItem} objects from
     *                       the {@link ExpandableRecyclerAdapter}
     * @return A list of all {@code ParentListItem} objects and their children, expanded
     */
    public static List<Object> generateParentChildItemList(List<? extends ParentListItem> parentItemList) {
        List<Object> parentWrapperList = new ArrayList<>();
        ParentListItem parentListItem;
        ParentWrapper parentWrapper;

        int parentListItemCount = parentItemList.size();
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = parentItemList.get(i);
            parentWrapper = new ParentWrapper(parentListItem);
            parentWrapperList.add(parentWrapper);

            if (parentWrapper.isInitiallyExpanded()) {
                parentWrapper.setExpanded(true);

                int childListItemCount = parentWrapper.getChildItemList().size();
                for (int j = 0; j < childListItemCount; j++) {
                    parentWrapperList.add(parentWrapper.getChildItemList().get(j));
                }

                // 添加LoadMore的数据.
                if (parentListItem.isLoadMore())
                    parentWrapperList.add(PARENT_LOAD_MORE_PREFIX + i);
            }

        }

        return parentWrapperList;
    }
}
