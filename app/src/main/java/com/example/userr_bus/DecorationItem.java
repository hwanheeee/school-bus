package com.example.userr_bus;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 아이템 간 간격(Padding)을 설정하는 ItemDecoration 클래스
 */
public class DecorationItem extends RecyclerView.ItemDecoration {

    private final int padding;

    public DecorationItem(int padding) {
        this.padding = padding;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // 각 아이템 주변에 padding 설정
        outRect.top = padding;
        outRect.bottom = padding;
        outRect.left = padding;
        outRect.right = padding;
    }
}
