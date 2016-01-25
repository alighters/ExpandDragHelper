package com.lighters.test.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.lighters.library.expanddrag.callback.ExpandCollapseListener;
import com.lighters.test.R;
import com.lighters.test.adapter.GroupAdapter;
import com.lighters.test.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private GroupAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);


        final ArrayList<String> num1 = new ArrayList<>();
        num1.add("01");
        num1.add("02");
        num1.add("03");
        num1.add("04");
        num1.add("05");
        final ArrayList<String> num2 = new ArrayList<>();
        num2.add("11");
        num2.add("12");
        num2.add("13");
        num2.add("14");
        num2.add("15");
        final ArrayList<String> num3 = new ArrayList<>();
        num3.add("21");
        num3.add("22");
        num3.add("23");
        num3.add("24");
        num3.add("25");
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
                Toast.makeText(GroupActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onListItemCollapsed(int position) {
                Group collapsedGroup = groups.get(position);

                String toastMsg = getResources().getString(R.string.collapsed, collapsedGroup.getName());
                Toast.makeText(GroupActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
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
