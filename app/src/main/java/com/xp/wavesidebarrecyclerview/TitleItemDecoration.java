package com.xp.wavesidebarrecyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import java.util.List;

/**
 * 有分类title的 ItemDecoration
 */

public class TitleItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "TitleItemDecoration";
    private List<SortModel> mData;
    private Paint mPaint;
    private Rect mBounds;

    private int mTitleHeight;
    private static int TITLE_BG_COLOR = Color.parseColor("#FFDFDFDF");
    private static int TITLE_TEXT_COLOR = Color.parseColor("#FF000000");
    private static int mTitleTextSize;


    public TitleItemDecoration(Context context, List<SortModel> data) {
        super();
        mData = data;
        mPaint = new Paint();
        mBounds = new Rect();
        mTitleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        mTitleTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics());
        mPaint.setTextSize(mTitleTextSize);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int position = params.getViewLayoutPosition();
            if (position > -1) {
                if (position == 0) {//等于0的时候绘制title
                    drawTitle(c, left, right, child, params, position);
                } else {
                    if (null != mData.get(position).getLetters() && !mData.get(position)
                            .getLetters().equals(mData.get(position - 1).getLetters())) {
                        //字母不为空，并且不等于前一个，也要title
                        drawTitle(c, left, right, child, params, position);
                    }
                }
            }
        }
    }

    /**
     * 绘制Title区域背景和文字的方法
     *最先调用，绘制最下层的title
     * @param c
     * @param left
     * @param right
     * @param child
     * @param params
     * @param position
     */
    private void drawTitle(Canvas c, int left, int right, View child, RecyclerView.LayoutParams params, int position) {
        mPaint.setColor(TITLE_BG_COLOR);
        c.drawRect(left, child.getTop() - params.topMargin - mTitleHeight, right, child.getTop() - params.topMargin, mPaint);
        mPaint.setColor(TITLE_TEXT_COLOR);

        mPaint.getTextBounds(mData.get(position).getLetters(), 0, mData.get(position).getLetters().length(), mBounds);
        c.drawText(mData.get(position).getLetters(), 
                child.getPaddingLeft(), 
                child.getTop() - params.topMargin - (mTitleHeight / 2 - mBounds.height() / 2), mPaint);
    }

    /**
     * 最后调用，绘制最上层的title
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDrawOver(Canvas c, final RecyclerView parent, RecyclerView.State state) {
        int position = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
        if (position == -1) return;//在搜索到没有的索引的时候position可能等于-1，所以在这里判断一下
        String tag = mData.get(position).getLetters();
        View child = parent.findViewHolderForLayoutPosition(position).itemView;
        //Canvas是否位移过的标志
        boolean flag = false;
        if ((position + 1) < mData.size()) {
            //当前第一个可见的Item的字母索引，不等于其后一个item的字母索引，说明悬浮的View要切换了
            if (null != tag && !tag.equals(mData.get(position + 1).getLetters())) {
                //当第一个可见的item在屏幕中剩下的高度小于title的高度时，开始悬浮Title的动画
                if (child.getHeight() + child.getTop() < mTitleHeight) {
                    c.save();
                    flag = true;
                    /**
                     * 下边的索引把上边的索引顶上去的效果
                     */
                    c.translate(0, child.getHeight() + child.getTop() - mTitleHeight);

                    /**
                     * 头部折叠起来的视效（下边的索引慢慢遮住上边的索引）
                     */
                    /*c.clipRect(parent.getPaddingLeft(),
                            parent.getPaddingTop(),
                            parent.getRight() - parent.getPaddingRight(),
                            parent.getPaddingTop() + child.getHeight() + child.getTop());*/
                }
            }
        }
        mPaint.setColor(TITLE_BG_COLOR);
        c.drawRect(parent.getPaddingLeft(), 
                parent.getPaddingTop(), 
                parent.getRight() - parent.getPaddingRight(), 
                parent.getPaddingTop() + mTitleHeight, mPaint);
        mPaint.setColor(TITLE_TEXT_COLOR);
        mPaint.getTextBounds(tag, 0, tag.length(), mBounds);
        c.drawText(tag, child.getPaddingLeft(),
                parent.getPaddingTop() + mTitleHeight - (mTitleHeight / 2 - mBounds.height() / 2),
                mPaint);
        if (flag)
            c.restore();//恢复画布到之前保存的状态

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        if (position > -1) {
            //等于0的时候绘制title
            if (position == 0) {
                outRect.set(0, mTitleHeight, 0, 0);
            } else {
                if (null != mData.get(position).getLetters() && 
                        !mData.get(position).getLetters().equals(mData.get(position - 1).getLetters())) {
                    //字母不为空，并且不等于前一个，绘制title
                    outRect.set(0, mTitleHeight, 0, 0);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            }
        }
    }

}
