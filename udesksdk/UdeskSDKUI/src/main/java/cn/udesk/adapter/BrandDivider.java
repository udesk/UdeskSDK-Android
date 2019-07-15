package cn.udesk.adapter;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class BrandDivider extends RecyclerView.ItemDecoration {
    private int width=0;
    private int height=0;

    public BrandDivider(int width) {
        this.width = width;
    }

    public BrandDivider(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0,0,width,height);

    }
}
