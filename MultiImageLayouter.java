package com.baidu.mercurial.components.gridview;

import android.graphics.Rect;
import android.view.View;

/**
 * 九宫格布局，正方形均匀分布，如果需要每个不一样的，则自己实现IMultiGridLayouter接口
 * 布局自己的子View
 * <p>
 * Created by @wangzexiang on 18/4/13.
 */

public class MultiImageLayouter implements IMultiGridLayouter {
    private static final String TAG = MultiImageLayouter.class.getSimpleName();
    private static final int DEFAULT_LINE_MAX = 3;
    private static final int DEFALT_SPACE_PADDING = 20;
    private final int[] mConsumeRect;
    //  上下间距
    private int mSpacePadding = DEFALT_SPACE_PADDING;
    //  每一行最多数量
    private int mLineMax = DEFAULT_LINE_MAX;

    public MultiImageLayouter() {
        //  初始化创建一个数据，避免在measure的时候多次创建
        mConsumeRect = new int[2];
    }

    public void setSpacePadding(int padding) {
        if (padding < 0) {
            return;
        }
        mSpacePadding = padding;
    }

    public void setLineMax(int lineMax) {
        if (lineMax < 1) {
            return;
        }
        mLineMax = lineMax;
    }

    @Override
    public int[] measureChild(MultiGridView view, int width, int height) {
        //  清理原有数据
        mConsumeRect[0] = 0;
        mConsumeRect[1] = 0;
        if (view == null) {
            return mConsumeRect;
        }
        int childCount = view.getChildCount();
        //  计算每个子View应有的宽高
        int horizontalSpace = mSpacePadding * (mLineMax - 1);
        //  减去空白区域后除以每一行的数量，得到每个子View的宽度
        int childWidth = (width - horizontalSpace) / mLineMax;
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY);
        View child;
        //  开始测量子View的宽高
        for (int i = 0; i < childCount; i++) {
            child = view.getChildAt(i);
            if (child == null) {
                continue;
            }
            // 子View的宽高相同
            child.measure(childWidthSpec, childWidthSpec);
        }
        //  计算所有的子View总共的高度和宽度
        mConsumeRect[0] = width;
        //  计算子View的所使用高度
        int lineCount = getLineCount(childCount, mLineMax);
        mConsumeRect[1] = lineCount * childWidth + (lineCount - 1) * mSpacePadding;
        return mConsumeRect;
    }

    private int getLineCount(int childCount, int lineMax) {
        //  如果能整除，模为0，则说明正好
        int mod = childCount % lineMax;
        int result = childCount / lineMax;
        //  如果能整除，则直接返回，不能整除则加1
        return mod == 0 ? result : result + 1;
    }

    @Override
    public void layoutChild(MultiGridView view, Rect rect) {
        if (view == null || rect == null) {
            return;
        }
        // 相对于父View的上，左位置
        int left = rect.left;
        int top = rect.top;
        //  Layout相关参数
        int childCount = view.getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = view.getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            //  开始layout子View
            child.layout(left, top, left + childWidth, top + childHeight);
            //  layout完后更新left top，i + 1 判断下一个子View是否要另起一行
            if ((i + 1) % mLineMax == 0) {
                // 如果正好能模尽，说明一行已经结束，另起一行
                top += childHeight + mSpacePadding;
                left = rect.left;
            } else {
                left += childWidth + mSpacePadding;
            }
        }
    }
}
