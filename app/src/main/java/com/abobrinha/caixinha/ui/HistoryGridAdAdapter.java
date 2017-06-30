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

public class HistoryGridAdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final private GridOnItemClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;

    private final int AD_INTERVAL = 8;
    private final int AD_INITIAL_OFFSET = 3;

    private final int ITEM_AD = 0;
    private final int ITEM_REGULAR = 1;

    public interface GridOnItemClickListener {
        void onListItemClick(long historyId, int position);
    }

    public HistoryGridAdAdapter(@NonNull Context context, GridOnItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        RecyclerView.ViewHolder viewHolder;

        if (viewType == ITEM_AD) {
            View view = inflater.inflate(R.layout.history_grid_item_banner, viewGroup, false);
            viewHolder = new AdViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.history_grid_item, viewGroup, false);
            viewHolder = new RegularViewHolder(view);
        }

        return viewHolder;
    }

    private int getOffsetPosition(int position) {
        return (position < AD_INITIAL_OFFSET || AD_INTERVAL <= 0) ? position :
                position - (int) Math.ceil((position - AD_INITIAL_OFFSET) / (double) (AD_INTERVAL + 1));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_REGULAR) {
            int offsetPosition = getOffsetPosition(position);
            mCursor.moveToPosition(offsetPosition);
            String title = Jsoup.parse(mCursor.getString(HistoryGridFragment.INDEX_HISTORY_TITLE)).text();
            RegularViewHolder rHolder = (RegularViewHolder) holder;
            rHolder.historyTitle.setText("(" + offsetPosition + ") " + title);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount() + (AD_INTERVAL > 0 && mCursor.getCount() > AD_INITIAL_OFFSET
                ? (int) Math.ceil((mCursor.getCount() - AD_INITIAL_OFFSET) / (double) AD_INTERVAL)
                : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (position >= AD_INITIAL_OFFSET &&
                AD_INTERVAL > 0 &&
                (position - AD_INITIAL_OFFSET) % (AD_INTERVAL + 1) == 0) ?
                ITEM_AD : ITEM_REGULAR;
    }

    public long getHistoryIdAtPosition(int position) {
        if (ITEM_AD == getItemViewType(position)) return -1;
        mCursor.moveToPosition(getOffsetPosition(position));
        return mCursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID);
    }

    public class RegularViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView historyTitle;

        public RegularViewHolder(View itemView) {
            super(itemView);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int offsetPosition = getOffsetPosition(getAdapterPosition());
            mCursor.moveToPosition(offsetPosition);
            mOnClickListener.onListItemClick(mCursor
                    .getLong(HistoryGridFragment.INDEX_HISTORY_ID), getAdapterPosition());
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        public AdViewHolder(View itemView) {
            super(itemView);
        }
    }
}
