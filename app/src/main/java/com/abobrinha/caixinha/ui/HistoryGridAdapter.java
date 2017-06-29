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

import org.jsoup.Jsoup;

public class HistoryGridAdapter extends RecyclerView.Adapter<HistoryGridAdapter.HistoryGridViewHolder> {

    final private GridOnItemClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;

    public interface GridOnItemClickListener {
        void onListItemClick(long historyId, int position);
    }

    public HistoryGridAdapter(@NonNull Context context, GridOnItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
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
        String title = Jsoup.parse(mCursor.getString(MainActivity.INDEX_HISTORY_TITLE)).text();
        holder.historyTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public class HistoryGridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView historyTitle;

        public HistoryGridViewHolder(View itemView) {
            super(itemView);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            mOnClickListener.onListItemClick(mCursor
                    .getLong(MainActivity.INDEX_HISTORY_ID), getAdapterPosition());
        }
    }
}
