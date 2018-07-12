package com.baidu.mercurial.components.gridview;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 网格控件，支持每行网格不同的配置
 * <p>
 * Created by @wangzexiang on 18/4/12.
 */

public class MultiGridView extends ViewGroup {
    private IMultiGridAdapter mAdapter;
    private IMultiGridLayouter mLayouter;
    //   回收器，将移除的子View都缓存在这里
    //  考虑用ArrayDeque来实现
    private LinkedList<View> mRecycler = new LinkedList<>();
    private Rect mRect;
    private OnHierarchyChangeListener mRecycleListener = new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View target, View child) {

        }

        @Override
        public void onChildViewRemoved(View target, View child) {
            mRecycler.add(child);
        }
    };

    public MultiGridView(Context context) {
        super(context);
        initialize();
    }

    public MultiGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MultiGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setOnHierarchyChangeListener(mRecycleListener);
        mRect = new Rect();
    }

    public void setLayouter(IMultiGridLayouter layouter) {
        mLayouter = layouter;
        requestLayout();
    }

    public void setAdapter(IMultiGridAdapter adapter) {
        mAdapter = adapter;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        if (mAdapter == null) {
            return;
        }
        int childCount = getChildCount();
        int mDataCount = mAdapter.getCount();
        for (int i = 0; i < mDataCount; i++) {
            //  如果i大于childCount，则需要生成
            if (i >= childCount) {
                View view;
                if (mRecycler.isEmpty()) {
                    view = mAdapter.genrateImageView(getContext());
                } else {
                    view = mRecycler.removeFirst();
                }
                addView(view);
                mAdapter.onFillImageView(getContext(), view, i);
            } else {
                mAdapter.onFillImageView(getContext(), getChildAt(i), i);
            }
        }
        //  计算出是否还需要生成子View
        int deltaCount = mDataCount - childCount;
        //  如果小于等于0，则说明当前View树上的子View多了，移除多余的View放到缓存列表中
        if (deltaCount < 0) {
            removeViews(mDataCount, childCount);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mLayouter != null) {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = View.MeasureSpec.getSize(heightMeasureSpec);
            int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
            //  返回子View Measure完之后的宽高信息
            //  int[0]为宽，int[1]为高
            int[] cost = mLayouter.measureChild(this, width, height);
            if (cost != null && cost.length == 2) {
                //  子View使用宽度再加上左右的Padding作为当前View的宽度
                int consumeWidth = cost[0] + getPaddingLeft() + getPaddingRight();
                //  子View使用高度再加上上下的Padding作为当前View的宽度
                int consumeHeight = cost[1] + getPaddingTop() + getPaddingBottom();
                //  此处处理Mode相关的逻辑
                setMeasuredDimension(getMeasureSize(width, widthMode, consumeWidth),
                        getMeasureSize(height, heightMode, consumeHeight));
            }
        }
    }

    private int getMeasureSize(int size, int mode, int consumeSize) {
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.UNSPECIFIED:
                return consumeSize > size ? consumeSize : size;
            case MeasureSpec.AT_MOST:
                //  如果是AT_MOST模式，则判断使用和width谁大，如果width比较大，则使用consumeSize
                //  否则如果consumeSize比较大
                return consumeSize > size ? size : consumeSize;
            default:
        }
        return consumeSize;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mLayouter != null) {
            mRect.left = left;
            mRect.right = right;
            mRect.top = top;
            mRect.bottom = bottom;
            mLayouter.layoutChild(this, mRect);
        }
    }
}
