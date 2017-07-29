package com.abobrinha.caixinha.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jsoup.Jsoup;

public class HistoryGridAdapter extends RecyclerView.Adapter<HistoryGridAdapter.HistoryGridViewHolder> {

    final private GridOnItemClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;
    private float mOffset;
    private int lastPosition = -1;

    public interface GridOnItemClickListener {
        void onListItemClick(long historyId);
    }

    public HistoryGridAdapter(@NonNull Context context, GridOnItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
        mOffset = mContext.getResources().getDimensionPixelSize(R.dimen.grid_animation_offset_y);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public HistoryGridViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutIdForListItem = R.layout.history_grid_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new HistoryGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryGridViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String title = Jsoup.parse(mCursor.getString(HistoryGridFragment.INDEX_HISTORY_TITLE)).text();

        Glide.with(mContext.getApplicationContext())
                .load(mCursor.getString(HistoryGridFragment.INDEX_HISTORY_IMAGE))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.post_placeholder)
                .error(R.drawable.post_placeholder)
                .into(holder.historyImage);

        holder.historyImage.setContentDescription(title);

        holder.historyTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AmaticSC-Bold.ttf"));
        holder.historyTitle.setText(title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateViewsIn(holder.cardView, position);
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public long getHistoryIdAtPosition(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateViewsIn(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Interpolator interpolator =
                    AnimationUtils.loadInterpolator(mContext,
                            android.R.interpolator.linear_out_slow_in);

            viewToAnimate.setVisibility(View.VISIBLE);
            viewToAnimate.setTranslationY(mOffset);
            viewToAnimate.setAlpha(0.85f);
            viewToAnimate.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(interpolator)
                    .setDuration(1000L)
                    .start();

            mOffset *= 1.5f;
            lastPosition = position;
        }
    }

    public class HistoryGridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CardView cardView;
        public ImageView historyImage;
        public TextView historyTitle;

        public HistoryGridViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            historyImage = (ImageView) itemView.findViewById(R.id.thumbnail);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            mOnClickListener.onListItemClick(mCursor
                    .getLong(HistoryGridFragment.INDEX_HISTORY_ID));
        }
    }
}
