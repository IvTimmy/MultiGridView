package com.baidu.mercurial.components.gridview;

import android.content.Context;
import android.view.View;

/**
 * MultiImageView的适配器
 * <p>
 * Created by @wangzexiang on 18/4/12.
 */

public interface IMultiGridAdapter {

    View genrateImageView(Context context);

    void onFillImageView(Context context, View view, int position);

    int getCount();
}
