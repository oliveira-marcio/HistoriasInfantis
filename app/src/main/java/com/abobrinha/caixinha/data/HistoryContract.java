package com.abobrinha.caixinha.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class HistoryContract {
    public static final String CONTENT_AUTHORITY = "com.abobrinha.caixinha";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HISTORIES = "movies";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_PARAGRAPHS = "trailers";

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

        public static Uri buildParagraphsFromHistoryUri(long historyId) {
            return HistoriesEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(historyId))
                    .appendPath(PATH_PARAGRAPHS)
                    .build();
        }
    }
}