package com.abobrinha.caixinha.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private String mTitle;
    private int mTitleVisibility;
    private int mBottomPaddingVisibility;

    private int mIndexParagraphType;
    private int mIndexParagraphContent;


    HistoryAdapter(@NonNull Context context) {
        mContext = context;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        if (mCursor != null) {
            mIndexParagraphType = mCursor.getColumnIndex(
                    HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE);
            mIndexParagraphContent = mCursor.getColumnIndex(
                    HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT);
        } else {
            mIndexParagraphType = -1;
            mIndexParagraphContent = -1;
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        RecyclerView.ViewHolder viewHolder;

        if (viewType == HistoryContract.ParagraphsEntry.TYPE_IMAGE) {
            View view = inflater.inflate(R.layout.history_paragraph_image, viewGroup, false);
            viewHolder = new ParagraphImageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.history_paragraph_text, viewGroup, false);
            viewHolder = new ParagraphTextViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String paragraphContent = mCursor.getString(mIndexParagraphContent);

        switch (holder.getItemViewType()) {
            case HistoryContract.ParagraphsEntry.TYPE_AUTHOR:
                ParagraphTextViewHolder aHolder = (ParagraphTextViewHolder) holder;
                prepareContentView(aHolder.historyContent, paragraphContent, Typeface.BOLD_ITALIC);
                prepareTitleView(aHolder.historyTitle, aHolder.titleBottomPadding);
                aHolder.bodyBottomPadding.setVisibility(mBottomPaddingVisibility);
                fixShadowOverlap(aHolder.bodyBottomPadding);
                break;
            case HistoryContract.ParagraphsEntry.TYPE_END:
                ParagraphTextViewHolder eHolder = (ParagraphTextViewHolder) holder;
                prepareContentView(eHolder.historyContent, paragraphContent, Typeface.BOLD);
                prepareTitleView(eHolder.historyTitle, eHolder.titleBottomPadding);
                eHolder.bodyBottomPadding.setVisibility(mBottomPaddingVisibility);
                fixShadowOverlap(eHolder.bodyBottomPadding);
                break;
            case HistoryContract.ParagraphsEntry.TYPE_IMAGE:
                final ParagraphImageViewHolder iHolder = (ParagraphImageViewHolder) holder;
                prepareTitleView(iHolder.historyTitle, iHolder.titleBottomPadding);

                Glide.with(mContext.getApplicationContext())
                        .load(paragraphContent)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                iHolder.imageContainer.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                int currentWidth = iHolder.historyImage.getWidth();
                                double multiplier = (double) resource.getIntrinsicHeight() / (double) resource.getIntrinsicWidth();
                                int finalHeight = (int) Math.round(currentWidth * multiplier);
                                iHolder.historyImage.setLayoutParams(new FrameLayout.LayoutParams(currentWidth, finalHeight));
                                iHolder.historyImage.setContentDescription(mTitle);
                                iHolder.historyImage.setVisibility(View.VISIBLE);
                                iHolder.loadingIndicator.setVisibility(View.GONE);
                                iHolder.imageContainer.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(iHolder.historyImage);

                iHolder.bodyBottomPadding.setVisibility(mBottomPaddingVisibility);
                fixShadowOverlap(iHolder.bodyBottomPadding);
                break;
            default:
                ParagraphTextViewHolder tHolder = (ParagraphTextViewHolder) holder;
                prepareContentView(tHolder.historyContent, paragraphContent, Typeface.NORMAL);
                prepareTitleView(tHolder.historyTitle, tHolder.titleBottomPadding);
                tHolder.bodyBottomPadding.setVisibility(mBottomPaddingVisibility);
                fixShadowOverlap(tHolder.bodyBottomPadding);
                break;
        }
    }

    private void prepareContentView(TextView contentView, String text, int style) {
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "Rosario-Regular.ttf");
        contentView.setTypeface(typeface, style);
        contentView.setText(text);
    }

    private void prepareTitleView(TextView titleView, View titleBottomView) {
        titleView.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AmaticSC-Bold.ttf"));
        titleView.setText(mTitle);
        titleView.setVisibility(mTitleVisibility);
        titleBottomView.setVisibility(mTitleVisibility);
        fixShadowOverlap(titleBottomView);
    }

    private void fixShadowOverlap(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        view.setOutlineProvider(new ViewOutlineProvider() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void getOutline(View view, Outline outline) {
                int topOffset = mContext.getResources().getDimensionPixelSize(R.dimen.history_elevation);
                outline.setRect(0, topOffset, view.getWidth(), view.getHeight() - topOffset);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        mTitleVisibility = (position == 0) ? View.VISIBLE : View.GONE;
        mBottomPaddingVisibility = (position < getItemCount() - 1) ? View.GONE : View.VISIBLE;
        return mCursor.getInt(mIndexParagraphType);
    }

    private class ParagraphTextViewHolder extends RecyclerView.ViewHolder {

        private TextView historyContent;
        private TextView historyTitle;
        private View titleBottomPadding;
        private View bodyBottomPadding;

        private ParagraphTextViewHolder(View itemView) {
            super(itemView);
            historyContent = (TextView) itemView.findViewById(R.id.content_text_view);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            titleBottomPadding = itemView.findViewById(R.id.title_bottom_padding);
            bodyBottomPadding = itemView.findViewById(R.id.body_bottom_padding);
        }
    }

    private class ParagraphImageViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout imageContainer;
        private ImageView historyImage;
        private TextView historyTitle;
        private View titleBottomPadding;
        private ProgressBar loadingIndicator;
        private View bodyBottomPadding;

        private ParagraphImageViewHolder(View itemView) {
            super(itemView);
            imageContainer = (FrameLayout) itemView.findViewById(R.id.image_container);
            historyImage = (ImageView) itemView.findViewById(R.id.image_view);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            titleBottomPadding = itemView.findViewById(R.id.title_bottom_padding);
            loadingIndicator = (ProgressBar) itemView.findViewById(R.id.loading_indicator);
            bodyBottomPadding = itemView.findViewById(R.id.body_bottom_padding);
        }
    }
}
