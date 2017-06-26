package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    private int mIndexParagraphType;
    private int mIndexParagraphContent;

    public HistoryAdapter(@NonNull Context context) {
        mContext = context;
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
                aHolder.historyContent.setText("(AUTHOR) " + paragraphContent);
                break;
            case HistoryContract.ParagraphsEntry.TYPE_END:
                ParagraphTextViewHolder eHolder = (ParagraphTextViewHolder) holder;
                eHolder.historyContent.setText("(END) " + paragraphContent);
                break;
            case HistoryContract.ParagraphsEntry.TYPE_IMAGE:
                ParagraphImageViewHolder iHolder = (ParagraphImageViewHolder) holder;
                iHolder.historyImage.setText(paragraphContent);
                break;
            default:
                ParagraphTextViewHolder tHolder = (ParagraphTextViewHolder) holder;
                tHolder.historyContent.setText(paragraphContent);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getInt(mIndexParagraphType);
    }

    public class ParagraphTextViewHolder extends RecyclerView.ViewHolder {

        public TextView historyContent;

        public ParagraphTextViewHolder(View itemView) {
            super(itemView);
            historyContent = (TextView) itemView.findViewById(R.id.content_text_view);
        }
    }

    public class ParagraphImageViewHolder extends RecyclerView.ViewHolder {

        public TextView historyImage;

        public ParagraphImageViewHolder(View itemView) {
            super(itemView);
            historyImage = (TextView) itemView.findViewById(R.id.image_text_view);
        }
    }
}
