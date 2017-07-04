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

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {

    final private DrawerOnItemClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;

    public interface DrawerOnItemClickListener {
        void onDrawerItemClick(long historyId, int position);
    }

    public DrawerAdapter(@NonNull Context context, DrawerOnItemClickListener listener, Cursor cursor) {
        mContext = context;
        mOnClickListener = listener;
        mCursor = cursor;
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutIdForListItem = R.layout.drawer_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new DrawerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String title = Jsoup.parse(mCursor.getString(HistoryGridFragment.INDEX_HISTORY_TITLE)).text();
        holder.historyTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public long getHistoryIdAtPosition(int position){
        mCursor.moveToPosition(position);
        return mCursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID);
    }

    public class DrawerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView historyTitle;

        public DrawerViewHolder(View itemView) {
            super(itemView);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            mOnClickListener.onDrawerItemClick(mCursor
                    .getLong(HistoryGridFragment.INDEX_HISTORY_ID), getAdapterPosition());
        }
    }
}
