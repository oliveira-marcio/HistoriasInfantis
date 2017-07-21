package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private String mTitle;
    private int mTitleVisibility;

    private int mIndexParagraphType;
    private int mIndexParagraphContent;


    public HistoryAdapter(@NonNull Context context) {
        mContext = context;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void swapCursor(Cursor newCursor) {
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
                prepareTitleView(aHolder.historyTitleContainer, aHolder.historyTitle);
                break;
            case HistoryContract.ParagraphsEntry.TYPE_END:
                ParagraphTextViewHolder eHolder = (ParagraphTextViewHolder) holder;
                prepareContentView(eHolder.historyContent, paragraphContent, Typeface.BOLD);
                prepareTitleView(eHolder.historyTitleContainer, eHolder.historyTitle);
                break;
            case HistoryContract.ParagraphsEntry.TYPE_IMAGE:
                final ParagraphImageViewHolder iHolder = (ParagraphImageViewHolder) holder;
                prepareTitleView(iHolder.historyTitleContainer, iHolder.historyTitle);

                Glide.with(mContext.getApplicationContext())
                        .load(paragraphContent)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                int currentWidth = iHolder.historyImage.getWidth();
                                double multiplier = (double) resource.getIntrinsicHeight() / (double) resource.getIntrinsicWidth();
                                int finalHeight = (int) Math.round(currentWidth * multiplier);
                                iHolder.historyImage.setLayoutParams(new FrameLayout.LayoutParams(currentWidth, finalHeight));
//                                Toast.makeText(mContext,
//                                        "size:" + resource.getIntrinsicWidth() + " x " + resource.getIntrinsicHeight() +
//                                        "\niv_size:" +  iHolder.historyImage.getWidth() + " x " + iHolder.historyImage.getHeight()
//                                        , Toast.LENGTH_SHORT).show();
                                iHolder.historyImage.setVisibility(View.VISIBLE);
                                iHolder.loadingIndicator.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(iHolder.historyImage);

                break;
            default:
                ParagraphTextViewHolder tHolder = (ParagraphTextViewHolder) holder;
                prepareContentView(tHolder.historyContent, paragraphContent, Typeface.NORMAL);
                prepareTitleView(tHolder.historyTitleContainer, tHolder.historyTitle);
                break;
        }
    }

    private void prepareContentView(TextView contentView, String text, int style) {
        contentView.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "Rosario-Regular.ttf"));
        contentView.setTypeface(Typeface.DEFAULT, style);
        contentView.setText(text);
    }

    private void prepareTitleView(LinearLayout titleContainer, TextView titleView) {
        titleView.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AmaticSC-Bold.ttf"));
        titleView.setText(mTitle);
        titleContainer.setVisibility(mTitleVisibility);
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        mTitleVisibility = (position == 0) ? View.VISIBLE : View.GONE;
        return mCursor.getInt(mIndexParagraphType);
    }

    public class ParagraphTextViewHolder extends RecyclerView.ViewHolder {

        public TextView historyContent;
        public TextView historyTitle;
        public LinearLayout historyTitleContainer;

        public ParagraphTextViewHolder(View itemView) {
            super(itemView);
            historyContent = (TextView) itemView.findViewById(R.id.content_text_view);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            historyTitleContainer = (LinearLayout) itemView.findViewById(R.id.title_container);
        }
    }

    public class ParagraphImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView historyImage;
        public TextView historyTitle;
        public LinearLayout historyTitleContainer;
        public ProgressBar loadingIndicator;

        public ParagraphImageViewHolder(View itemView) {
            super(itemView);
            historyImage = (ImageView) itemView.findViewById(R.id.image_view);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            historyTitleContainer = (LinearLayout) itemView.findViewById(R.id.title_container);
            loadingIndicator = (ProgressBar) itemView.findViewById(R.id.loading_indicator);
        }
    }
}
