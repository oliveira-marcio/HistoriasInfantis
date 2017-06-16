package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.History;

import java.util.List;

public class HistoryGridAdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final private GridOnItemClickListener mOnClickListener;

    private List<History> mHistory;
    private Context mContext;

    private final int AD_INTERVAL = 8;
    private final int AD_INITIAL_OFFSET = 0;

    private final int ITEM_AD = 0;
    private final int ITEM_REGULAR = 1;

    public interface GridOnItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public HistoryGridAdAdapter(List<History> history, GridOnItemClickListener listener) {
        mHistory = history;
        mOnClickListener = listener;
    }

    // Métodos clear() e addAll() criados para simular os equivalentes de um Adapter de ListView
    public void clear() {
        mHistory.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<History> history) {
        mHistory.clear();
        mHistory.addAll(history);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
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
        return (position < AD_INITIAL_OFFSET) ? position :
                position - (int) Math.ceil((position - AD_INITIAL_OFFSET) / (double) (AD_INTERVAL + 1));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_REGULAR) {
            int offsetPosition = getOffsetPosition(position);
            String title = mHistory.get(offsetPosition).getTitle();
            RegularViewHolder rHolder = (RegularViewHolder) holder;
            rHolder.historyTitle.setText("(" + offsetPosition + ") " + title);
        }
    }

    @Override
    public int getItemCount() {
        if (mHistory == null) return 0;
        return mHistory.size() + (int) Math.ceil((mHistory.size() - AD_INITIAL_OFFSET) / (double) AD_INTERVAL);
    }

    @Override
    public int getItemViewType(int position) {
        return (position >= AD_INITIAL_OFFSET &&
                (position - AD_INITIAL_OFFSET) % (AD_INTERVAL + 1) == 0) ?
                ITEM_AD : ITEM_REGULAR;
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
            mOnClickListener.onListItemClick(getOffsetPosition(getAdapterPosition()));
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        public AdViewHolder(View itemView) {
            super(itemView);
        }
    }
}
