package com.abobrinha.caixinha.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.History;
import com.abobrinha.caixinha.data.Paragraph;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mHistoryView;
    private HistoryAdapter mAdapter;

    List<Paragraph> mHistoryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getIntent() == null) finish();

        if (!getIntent().hasExtra(Intent.EXTRA_TEXT)) finish();

        String HistoryJson = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        Gson gson = new Gson();
        History history = gson.fromJson(HistoryJson, History.class);

        setTitle(history.getTitle());

        mHistoryData = JsoupParser(history.getContent());

        mHistoryView = (RecyclerView) findViewById(R.id.rv_history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mHistoryView.setLayoutManager(layoutManager);
        mHistoryView.setHasFixedSize(true);

        mAdapter = new HistoryAdapter(mHistoryData);
        mHistoryView.setAdapter(mAdapter);
    }

    //ToDo: No ContentProvider, essa rotina deve ser chamada ao salvar a história
    private List<Paragraph> JsoupParser(String htmlString) {

        final String BR_TOKEN = "#!#br2n#!#";
        final String TAG_P = "p";
        final String TAG_IMG = "img";
        final String TAG_SRC = "src";

        List<Paragraph> historyData = new ArrayList<>();

        Document doc = Jsoup.parse(htmlString.replaceAll("(?i)<br[^>]*>", BR_TOKEN));
        for (Element p : doc.select(TAG_P)) {
            if (!p.text().trim().isEmpty()) {
                Paragraph paragraphText = new Paragraph();
                String paragraphString = p.text().replaceAll(BR_TOKEN, "\n");
                int paragraphKey;
                boolean isAuthor = false;

                if(paragraphString.trim().toLowerCase().equals(Paragraph.AUTHOR.toLowerCase())){
                    paragraphKey = Paragraph.TYPE_AUTHOR;
                    isAuthor = true;
                } else if(paragraphString.trim().toLowerCase().equals(Paragraph.END.toLowerCase())){
                    paragraphKey = Paragraph.TYPE_END;
                } else {
                    paragraphKey = Paragraph.TYPE_TEXT;
                }

                paragraphText.setType(paragraphKey);
                paragraphText.setContent(paragraphString);
                historyData.add(paragraphText);

                if(isAuthor) break;
            }
            Paragraph paragraphImage = new Paragraph();
            for (Element img : p.select(TAG_IMG)) {
                String imageUrl = img.absUrl(TAG_SRC).substring(0, img.absUrl(TAG_SRC).indexOf('?'));
                paragraphImage.setType(Paragraph.TYPE_IMAGE);
                paragraphImage.setContent(imageUrl);
                historyData.add(paragraphImage);
            }
        }
        return historyData;
    }
}
