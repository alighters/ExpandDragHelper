package com.lighters.test.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.lighters.library.expanddrag.callback.DragSelectCallback;
import com.lighters.library.expanddrag.callback.ExpandCollapseListener;
import com.lighters.test.R;
import com.lighters.test.adapter.GroupDragAdapter;
import com.lighters.test.model.GroupDrag;

import java.util.ArrayList;
import java.util.List;

public class GroupDragActivity extends AppCompatActivity {

    private GroupDragAdapter mAdapter;
    private boolean result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);


        ArrayList<String> num1 = new ArrayList<>();
        num1.add("01");
        num1.add("02");
        num1.add("03");
        num1.add("04");
        num1.add("05");
        ArrayList<String> num2 = new ArrayList<>();
        num2.add("11");
        num2.add("12");
        num2.add("13");
        num2.add("14");
        num2.add("15");
        ArrayList<String> num3 = new ArrayList<>();
        num3.add("21");
        num3.add("22");
        num3.add("23");
        num3.add("24");
        num3.add("25");
        GroupDrag taco = new GroupDrag("taco", num1);
        GroupDrag quesadilla = new GroupDrag("quesadilla", num2);
        GroupDrag burger = new GroupDrag("burger", num3);
        final List<GroupDrag> GroupDrags = new ArrayList<>();
        GroupDrags.add(taco);
        GroupDrags.add(quesadilla);
        GroupDrags.add(burger);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new GroupDragAdapter(this, GroupDrags);
        mAdapter.setExpandCollapseListener(new ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
                GroupDrag expandedGroupDrag = GroupDrags.get(position);

                String toastMsg = getResources().getString(R.string.expanded, expandedGroupDrag.getName());
                Toast.makeText(GroupDragActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onListItemCollapsed(int position) {
                GroupDrag collapsedGroupDrag = GroupDrags.get(position);

                String toastMsg = getResources().getString(R.string.collapsed, collapsedGroupDrag.getName());
                Toast.makeText(GroupDragActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

        });

        mAdapter.setDragSelectCallback(new DragSelectCallback() {
            @Override
            public void onListItemSelected(View view, int parentPostion) {
                view.setBackgroundColor(Color.RED);
                view.invalidate();
            }

            @Override
            public void onListItemUnSelected(View view, int parentPostion) {
                view.setBackgroundColor(Color.TRANSPARENT);
                view.invalidate();
            }

            @Override
            public void onListItemDrop(int fromTotalPosition, int fromParentPosition, int fromChildPositionOfParent,
                                       int toParentPosition) {
                Toast.makeText(GroupDragActivity.this,
                        "fromTotal=" + fromTotalPosition + ", fromParentPosition = " + fromParentPosition
                                + ", fromChildOfParent= " + fromChildPositionOfParent + ",topostion = " +
                                toParentPosition,
                        Toast.LENGTH_LONG)
                        .show();
                GroupDrag GroupDrag = GroupDrags.get(fromParentPosition);
                String ingredient = GroupDrag.getChildItemList().get(fromChildPositionOfParent);
                GroupDrag.getChildItemList().remove(ingredient);
                if (ingredient != null && toParentPosition >= 0 && toParentPosition < GroupDrags.size()) {
                    GroupDrags.get(toParentPosition).getChildItemList().add(0, ingredient);
                }
            }

            @Override
            public void onStartDrag(int fromPosition, int fromParentPosition, int offsetOfParent) {
                super.onStartDrag(fromPosition, fromParentPosition, offsetOfParent);
                Toast.makeText(GroupDragActivity.this
                        , "DragStart: fromParentPosition = " + fromParentPosition,
                        Toast.LENGTH_LONG)
                        .show();
            }

        });

        recyclerView.setAdapter(mAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAdapter.onRestoreInstanceState(savedInstanceState);
    }

}
