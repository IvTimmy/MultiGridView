package com.baidu.mercurial.components.gridview;

import android.graphics.Rect;

/**
 * MultiGridView的Layouet规则，主要负责子View的Measure以及Layout
 * <p>
 * Created by @wangzexiang on 18/4/12.
 */

public interface IMultiGridLayouter {

    int[] measureChild(MultiGridView view, int width, int height);

    void layoutChild(MultiGridView view, Rect rect);
}
