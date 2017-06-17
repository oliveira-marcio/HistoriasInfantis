package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.History;

import org.jsoup.Jsoup;

import java.util.List;

public class HistoryGridAdapter extends RecyclerView.Adapter<HistoryGridAdapter.HistoryGridViewHolder> {

    final private GridOnItemClickListener mOnClickListener;

    private List<History> mHistory;
    private Context mContext;

    public interface GridOnItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public HistoryGridAdapter(List<History> history, GridOnItemClickListener listener) {
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
    public HistoryGridViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.history_grid_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new HistoryGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryGridViewHolder holder, int position) {
        String title = Jsoup.parse(mHistory.get(position).getTitle()).text();
        holder.historyTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        if (mHistory == null) return 0;
        return mHistory.size();
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
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
