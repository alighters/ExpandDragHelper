package com.lighters.test.viewholder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.lighters.library.expanddrag.ViewHolder.ParentViewHolder;
import com.lighters.test.R;
import com.lighters.test.model.Group;

public class GroupViewHolder extends ParentViewHolder {

    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    private final ImageView mArrowExpandImageView;
    private TextView mRecipeTextView;

    public GroupViewHolder(View itemView) {
        super(itemView);
        mRecipeTextView = (TextView) itemView.findViewById(R.id.recipe_textview);

        mArrowExpandImageView = (ImageView) itemView.findViewById(R.id.arrow_expand_imageview);
    }

    public void bind(Group group) {
        mRecipeTextView.setText(group.getName());
    }

    @SuppressLint("NewApi")
    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (expanded) {
                mArrowExpandImageView.setRotation(ROTATED_POSITION);
            } else {
                mArrowExpandImageView.setRotation(INITIAL_POSITION);
            }
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            RotateAnimation rotateAnimation;
            if (expanded) { // rotate clockwise
                rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            } else { // rotate counterclockwise
                rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            }

            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            mArrowExpandImageView.startAnimation(rotateAnimation);
        }
    }

    @Override
    public void onSelected() {
        mRecipeTextView.setTextColor(Color.RED);
    }

    @Override
    public void onUnSelected() {
        mRecipeTextView.setTextColor(Color.BLACK);
    }
}
