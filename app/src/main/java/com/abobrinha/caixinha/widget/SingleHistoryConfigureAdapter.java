package com.abobrinha.caixinha.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.ui.HistoryGridFragment;

import org.jsoup.Jsoup;

public class SingleHistoryConfigureAdapter extends
        RecyclerView.Adapter<SingleHistoryConfigureAdapter.HistoryConfigWidgetViewHolder> {

    final private OnItemClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;
    private static int sItemSelected = -1;
    public static final int INVALID_HISTORY_ID = -1;

    public interface OnItemClickListener {
        void onListItemClick(long historyId);
    }

    public SingleHistoryConfigureAdapter(@NonNull Context context, OnItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public void invalidateSelection() {
        sItemSelected = -1;
    }

    public long getSelectedHistoryId() {
        return (mCursor != null && mCursor.moveToPosition(sItemSelected))
                ? mCursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID)
                : INVALID_HISTORY_ID;
    }

    @Override
    public HistoryConfigWidgetViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutIdForListItem = R.layout.single_history_widget_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new HistoryConfigWidgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryConfigWidgetViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String title = Jsoup.parse(mCursor.getString(HistoryGridFragment.INDEX_HISTORY_TITLE)).text();
        holder.historyRadio.setText(title);
        holder.historyRadio.setChecked(sItemSelected == position);
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public class HistoryConfigWidgetViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public RadioButton historyRadio;

        public HistoryConfigWidgetViewHolder(View itemView) {
            super(itemView);
            historyRadio = (RadioButton) itemView.findViewById(R.id.option_radio);
            itemView.setOnClickListener(this);
            historyRadio.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            mOnClickListener.onListItemClick(mCursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID));

            sItemSelected = getAdapterPosition();
            notifyItemRangeChanged(0, getItemCount());
        }
    }
}
