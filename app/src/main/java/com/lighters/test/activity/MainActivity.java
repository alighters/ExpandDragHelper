package com.lighters.test.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.lighters.library.expanddrag.Model.LoadMoreStatus;
import com.lighters.library.expanddrag.callback.DragSelectCallback;
import com.lighters.library.expanddrag.callback.ExpandCollapseListener;
import com.lighters.library.expanddrag.callback.LoadMoreListener;
import com.lighters.test.R;
import com.lighters.test.model.Group;
import com.lighters.test.adapter.GroupAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GroupAdapter mAdapter;
    private boolean result = false;

    ArrayList<String> num1 = new ArrayList<>();
    ArrayList<String> num2 = new ArrayList<>();
    ArrayList<String> num3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ArrayList<String> num1 = new ArrayList<>();
//        num1.add("01");
//        num1.add("02");
//        num1.add("03");
//        num1.add("04");
//        num1.add("05");|
        final ArrayList<String> num2 = new ArrayList<>();
//        num2.add("11");
//        num2.add("12");
//        num2.add("13");
//        num2.add("14");
//        num2.add("15");
        final ArrayList<String> num3 = new ArrayList<>();
//        num3.add("21");
//        num3.add("22");
//        num3.add("23");
//        num3.add("24");
//        num3.add("25");
        final Group group1 = new Group("group1", num1);
        final Group group2 = new Group("group2", num2);
        final Group group3 = new Group("group3", num3);
        final List<Group> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        groups.add(group3);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new GroupAdapter(this, groups);
        mAdapter.setExpandCollapseListener(new ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
                Group expandedGroup = groups.get(position);

                String toastMsg = getResources().getString(R.string.expanded, expandedGroup.getName());
                Toast.makeText(MainActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onListItemCollapsed(int position) {
                Group collapsedGroup = groups.get(position);

                String toastMsg = getResources().getString(R.string.collapsed, collapsedGroup.getName());
                Toast.makeText(MainActivity.this,
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
                Toast.makeText(MainActivity.this,
                        "fromTotal=" + fromTotalPosition + ", fromParentPosition = " + fromParentPosition
                                + ", fromChildOfParent= " + fromChildPositionOfParent + ",topostion = " +
                                toParentPosition,
                        Toast.LENGTH_LONG)
                        .show();
                Group group = groups.get(fromParentPosition);
                String ingredient = group.getChildItemList().get(fromChildPositionOfParent);
                group.getChildItemList().remove(ingredient);
                if (ingredient != null && toParentPosition >= 0 && toParentPosition < groups.size()) {
                    groups.get(toParentPosition).getChildItemList().add(0, ingredient);
                }
            }
        });

        mAdapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void loadMore(final int parentIndex) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (parentIndex >= 0 && parentIndex < groups.size()) {
                            Group group = groups.get(parentIndex);
                            int childSize = group.getChildItemList().size();

                            List<String> appendList = new ArrayList<String>();
                            if (childSize > 10)
                                group.setLoadMoreStatus(LoadMoreStatus.FINISH);
                            else {
                                for (int i = 0; i < 5; i++)
                                    appendList.add(parentIndex + "" + (i + 1));
                                group.getChildItemList().addAll(appendList);
                                group.setLoadMoreStatus(LoadMoreStatus.INIT);
                            }
                            mAdapter.notifyChildItemRangeInserted(parentIndex, childSize, appendList.size());
                        }
                    }
                }, 2000);

            }
        });

        recyclerView.setAdapter(mAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        findViewById(R.id.test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!result) {
                    mAdapter.expandAllParents(1);
                } else
                    mAdapter.collapseAllParents();
                result = !result;
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                List<String> appendList = new ArrayList<String>();
                for (int i = 0; i < 5; i++)
                    appendList.add("" + (i + 1));
                group3.getChildItemList().addAll(appendList);
                group3.setLoadMoreStatus(LoadMoreStatus.INIT);
                mAdapter.notifyChildItemRangeInserted(2, 0, appendList.size());
            }
        });
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
