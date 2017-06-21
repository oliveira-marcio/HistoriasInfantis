package com.abobrinha.caixinha.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class HistoryContract {
    public static final String CONTENT_AUTHORITY = "com.abobrinha.caixinha";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HISTORIES = "histories";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_PARAGRAPHS = "paragraphs";

    public static final int IS_FAVORITE = 1;
    public static final int IS_NOT_FAVORITE = 0;

    public static final class HistoriesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_HISTORIES)
                .build();

        public static final String TABLE_NAME = "histories";

        public static final String COLUMN_HISTORY_TITLE = "title";
        public static final String COLUMN_HISTORY_URL = "url_history";
        public static final String COLUMN_HISTORY_IMAGE = "url_image";
        public static final String COLUMN_HISTORY_DATE = "date_created";
        public static final String COLUMN_HISTORY_MODIFIED = "date_modified";
        public static final String COLUMN_FAVORITE = "favorite";

        public static Uri buildFavoritesUri() {
            return HistoriesEntry.CONTENT_URI.buildUpon()
                    .appendPath(PATH_FAVORITES)
                    .build();
        }

        public static Uri buildSingleHistoryUri(long historyId) {
            return HistoriesEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(historyId))
                    .build();
        }
    }

    public static final class ParagraphsEntry implements BaseColumns {

        public static final String TABLE_NAME = "paragraphs";

        public static final String COLUMN_HISTORY_ID = "history_id";
        public static final String COLUMN_PARAGRAPH_TYPE = "type";
        public static final String COLUMN_PARAGRAPH_CONTENT = "content";

        public static final int TYPE_TEXT = 0;
        public static final int TYPE_IMAGE = 1;
        public static final int TYPE_AUTHOR = 2;
        public static final int TYPE_END = 3;

        public static final String AUTHOR = "Rodrigo Lopes";
        public static final String END = "FIM";

        public static Uri buildParagraphsFromHistoryUri(long historyId) {
            return HistoriesEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(historyId))
                    .appendPath(PATH_PARAGRAPHS)
                    .build();
        }
    }
}