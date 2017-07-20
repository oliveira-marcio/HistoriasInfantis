package com.abobrinha.caixinha.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.sync.NotificationUtils;

import org.jsoup.Jsoup;


public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public HistoryFragment() {
    }

    private Uri mHistoryUri;
    private Uri mParagraphsUri;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean mIsFavorite;

    private final String VISIBLE_POSITION = "visible_position";
    private final String HISTORY_URI = "history_uri";
    private final String STATUS_BAR_HEIGHT = "status_bar_height";

    private RecyclerView mHistoryView;
    private LinearLayoutManager mLayoutManager;
    private FloatingActionButton mFabFavorite;

    private HistoryAdapter mAdapter;
    private ProgressBar mLoadingIndicator;

    private final int HISTORY_LOADER_ID = 1;
    private final int PARAGRAPH_LOADER_ID = 2;

    public final String[] MAIN_HISTORY_PROJECTION = {
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE,
            HistoryContract.HistoriesEntry.COLUMN_FAVORITE
    };

    public static final int INDEX_HISTORY_TITLE = 0;
    public static final int INDEX_HISTORY_IMAGE = 1;
    public static final int INDEX_FAVORITE = 2;

    private int mStatusBarHeight;
    private int mUpButtonBaseTop = 0;
    private View mParallaxView;
    private View mStatusBar;
    private ImageView mUpButton;
    private int mScrollY;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Rect rectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        mStatusBarHeight = rectangle.top;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(VISIBLE_POSITION);
            mHistoryUri = savedInstanceState.getParcelable(HISTORY_URI);
            mStatusBarHeight = savedInstanceState.getInt(STATUS_BAR_HEIGHT);
        }

        if (mHistoryUri == null) throw
                new NullPointerException("URI para HistoryFragment não pode ser nula.");

        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        long historyId = ContentUris.parseId(mHistoryUri);
        mParagraphsUri = HistoryContract.ParagraphsEntry.buildParagraphsFromHistoryId(historyId);

        mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.loading_indicator);
        mHistoryView = (RecyclerView) rootView.findViewById(R.id.rv_history);

        mFabFavorite = (FloatingActionButton) rootView.findViewById(R.id.fabFavorite);
        mFabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavoriteStatus();
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());

        mHistoryView.setLayoutManager(mLayoutManager);
        mHistoryView.setHasFixedSize(true);

        mAdapter = new HistoryAdapter(getActivity());
        mHistoryView.setAdapter(mAdapter);

        mUpButton = (ImageView) rootView.findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUpButtonBaseTop += mStatusBarHeight;
            mStatusBar = rootView.findViewById(R.id.status_bar_background);
            mStatusBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mStatusBarHeight));
            updateUpButtonPostition();
        }

        mParallaxView = rootView.findViewById(R.id.parallax_image);

        mHistoryView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollY = mHistoryView.computeVerticalScrollOffset();
                updateParallaxViewPosition();
                updateUpButtonPostition();
            }
        });

        showLoading();

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
        getLoaderManager().initLoader(PARAGRAPH_LOADER_ID, null, this);

        return rootView;
    }

    private void updateStatusBar(float progress) {
        if (mStatusBar == null) return;

        int mStatusBarColor = ActivityCompat.getColor(getActivity(), R.color.colorPrimaryDark);
        int color = Color.argb((int) (255 * constrain(progress, 0, 1)),
                Color.red(mStatusBarColor),
                Color.green(mStatusBarColor),
                Color.blue(mStatusBarColor));

        mStatusBar.setBackgroundColor(color);
    }

    private float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void updateUpButtonPostition() {
        final int TITLE_OFFSET = getResources().getDimensionPixelSize(R.dimen.history_title_offset);
        final int MAX = mUpButton.getHeight();

        if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
            int dY = mUpButtonBaseTop;
            if (mScrollY > TITLE_OFFSET - MAX - dY) {
                dY = Math.max(-MAX, TITLE_OFFSET - mScrollY - MAX);
            }
            mUpButton.setTranslationY(dY);
            updateStatusBar((float) (mUpButtonBaseTop - dY) / (float) MAX);
        }
    }

    private void updateParallaxViewPosition() {
        final int PARALLAX_FACTOR = 2;
        final int MAX = mParallaxView.getHeight();

        mParallaxView.setTranslationY(Math.max(-MAX, -mScrollY / PARALLAX_FACTOR));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mPosition = mLayoutManager.findFirstVisibleItemPosition();
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(VISIBLE_POSITION, mPosition);
        }
        outState.putParcelable(HISTORY_URI, mHistoryUri);
        outState.putInt(STATUS_BAR_HEIGHT, mStatusBarHeight);

        super.onSaveInstanceState(outState);
    }


    public void setHistoryUri(Uri uri) {
        mHistoryUri = uri;
    }

    private void showLoading() {
        mHistoryView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showHistoryDataView() {
        mHistoryView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case HISTORY_LOADER_ID:
                return new CursorLoader(getActivity(),
                        mHistoryUri,
                        MAIN_HISTORY_PROJECTION,
                        null,
                        null,
                        null);

            case PARAGRAPH_LOADER_ID:
                return new CursorLoader(getActivity(),
                        mParagraphsUri,
                        null,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader não implementado: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) return;

        switch (loader.getId()) {
            case HISTORY_LOADER_ID:
                mAdapter.setTitle(Jsoup.parse(data.getString(INDEX_HISTORY_TITLE)).text());
                mIsFavorite = (data.getInt(INDEX_FAVORITE) == HistoryContract.IS_FAVORITE);
                setFavoriteFabColor();
                break;

            case PARAGRAPH_LOADER_ID:
                mAdapter.swapCursor(data);
                if (mPosition != RecyclerView.NO_POSITION)
                    mHistoryView.scrollToPosition(mPosition);
                showHistoryDataView();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void setFavoriteFabColor() {
        int color = ActivityCompat
                .getColor(getActivity(), mIsFavorite ? R.color.colorFavorite : R.color.colorAccent);
        mFabFavorite.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public void toggleFavoriteStatus() {
        mIsFavorite = !mIsFavorite;

        ContentValues values = new ContentValues();
        values.put(HistoryContract.HistoriesEntry.COLUMN_FAVORITE,
                mIsFavorite ? HistoryContract.IS_FAVORITE : HistoryContract.IS_NOT_FAVORITE);

        int updatedRows = getActivity().getContentResolver().update(
                mHistoryUri,
                values,
                null,
                null);

        if (updatedRows > 0) {
            Toast.makeText(getActivity(),
                    mIsFavorite ? getString(R.string.favorite_added) : getString(R.string.favorite_removed),
                    Toast.LENGTH_SHORT).show();

            NotificationUtils.updateWidgets(getActivity());
        }
    }
}