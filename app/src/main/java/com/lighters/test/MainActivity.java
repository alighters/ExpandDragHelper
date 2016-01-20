package com.lighters.test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.lighters.library.expanddrag.Adapter.ExpandableRecyclerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter mAdapter;
    private boolean result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        Recipe taco = new Recipe("taco", num1);
        Recipe quesadilla = new Recipe("quesadilla", num2);
        Recipe burger = new Recipe("burger", num3);
        final List<Recipe> recipes = Arrays.asList(taco, quesadilla, burger);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new RecipeAdapter(this, recipes);
        mAdapter.setExpandDragListener(new ExpandableRecyclerAdapter.ExpandDragListener() {
            @Override
            public void onListItemExpanded(int position) {
                Recipe expandedRecipe = recipes.get(position);

                String toastMsg = getResources().getString(R.string.expanded, expandedRecipe.getName());
                Toast.makeText(MainActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onListItemCollapsed(int position) {
                Recipe collapsedRecipe = recipes.get(position);

                String toastMsg = getResources().getString(R.string.collapsed, collapsedRecipe.getName());
                Toast.makeText(MainActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onListItemSelected(View view) {
                view.setBackgroundColor(Color.RED);
                view.invalidate();
            }

            @Override
            public void onListItemUnSelected(View view) {
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
                        Toast.LENGTH_SHORT)
                        .show();
                Recipe recipe = recipes.get(fromParentPosition);
                String ingredient = recipe.getChildItemList().get(fromChildPositionOfParent);
                recipe.getChildItemList().remove(ingredient);
                if (ingredient != null) {
                    recipes.get(toParentPosition).getChildItemList().add(0, ingredient);
                }
                mAdapter.notifyDataSetChanged();
            }

        });

        recyclerView.setAdapter(mAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        findViewById(R.id.test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!result) {
                    mAdapter.expandAllParents(2);
                } else
                    mAdapter.collapseAllParents();
                result = !result;
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
