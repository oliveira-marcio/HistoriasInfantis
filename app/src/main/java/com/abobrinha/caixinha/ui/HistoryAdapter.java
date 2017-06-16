package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.Paragraph;

import java.util.List;

import static com.abobrinha.caixinha.data.Paragraph.TYPE_AUTHOR;
import static com.abobrinha.caixinha.data.Paragraph.TYPE_END;
import static com.abobrinha.caixinha.data.Paragraph.TYPE_IMAGE;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Paragraph> mHistory;
    private Context mContext;

    public HistoryAdapter(List<Paragraph> history) {
        mHistory = history;
    }

    // Métodos clear() e addAll() criados para simular os equivalentes de um Adapter de ListView
    public void clear() {
        mHistory.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Paragraph> history) {
        mHistory.clear();
        mHistory.addAll(history);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        RecyclerView.ViewHolder viewHolder;

        if (viewType == TYPE_IMAGE) {
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
        Paragraph paragraph = mHistory.get(position);
        switch (holder.getItemViewType()) {
            case Paragraph.TYPE_AUTHOR:
                ParagraphTextViewHolder aHolder = (ParagraphTextViewHolder) holder;
                aHolder.historyContent.setText("(AUTHOR) " + paragraph.getContent());
                break;
            case Paragraph.TYPE_END:
                ParagraphTextViewHolder eHolder = (ParagraphTextViewHolder) holder;
                eHolder.historyContent.setText("(END) " + paragraph.getContent());
                break;
            case Paragraph.TYPE_IMAGE:
                ParagraphImageViewHolder iHolder = (ParagraphImageViewHolder) holder;
                iHolder.historyImage.setText(paragraph.getContent());
                break;
            default:
                ParagraphTextViewHolder tHolder = (ParagraphTextViewHolder) holder;
                tHolder.historyContent.setText(paragraph.getContent());
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mHistory == null) return 0;
        return mHistory.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mHistory.get(position).getType();
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
