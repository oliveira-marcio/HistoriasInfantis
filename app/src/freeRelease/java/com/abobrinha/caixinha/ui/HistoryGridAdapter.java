package com.abobrinha.caixinha.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

import org.jsoup.Jsoup;

import java.util.Random;

public class HistoryGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final private GridOnItemClickListener mOnClickListener;

    private Cursor mCursor;
    private Context mContext;
    private float mOffset;
    private int lastPosition = -1;
    private InterstitialAd mInterstitialAd;
    private int mPosition;

    // Define o intervalo de distribuição dos ad's e em qual posição se iniciará a distribuição
    private final int AD_INTERVAL = 8;
    private final int AD_INITIAL_OFFSET = 3;

    private final int ITEM_AD = 0;
    private final int ITEM_REGULAR = 1;

    // Escala e frequência que os anúncios interstitiais devem aparecer
    // Ex: É sorteado um número de 0 até o range. Se for menor que a frequência, exibe o ad
    private final int INTERSTITIAL_RANGE = 100;
    private final int INTERSTITIAL_FREQUENCY = 30;

    public interface GridOnItemClickListener {
        void onListItemClick(long historyId);
    }

    public HistoryGridAdapter(@NonNull Context context, GridOnItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
        mOffset = mContext.getResources().getDimensionPixelSize(R.dimen.grid_animation_offset_y);
        MobileAds.initialize(context.getApplicationContext(),
                context.getString(R.string.admob_app_id));

        mInterstitialAd = new InterstitialAd(mContext);
        mInterstitialAd.setAdUnitId(mContext.getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                doRegularClick();
            }
        });
        requestNewInterstitial();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void doRegularClick() {
        int offsetPosition = getOffsetPosition(mPosition);
        mCursor.moveToPosition(offsetPosition);
        mOnClickListener.onListItemClick(mCursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID));
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

    // Calcula a posição correta do BD a ser utilizada de acordo com a posição do item na lista
    // e considerando a distribuição dos ad's.
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

            Glide.with(mContext.getApplicationContext())
                    .load(mCursor.getString(HistoryGridFragment.INDEX_HISTORY_IMAGE))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.post_placeholder)
                    .error(R.drawable.post_placeholder)
                    .into(rHolder.historyImage);

            rHolder.historyImage.setContentDescription(title);

            rHolder.historyTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AmaticSC-Bold.ttf"));
            rHolder.historyTitle.setText(title);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateViewsIn(rHolder.cardView, position);
            }
        } else {
            final AdViewHolder aHolder = (AdViewHolder) holder;

            final NativeExpressAdView adView = new NativeExpressAdView(mContext);

            aHolder.adContainer.addView(adView);

            adView.setAdSize(getComputedAdSize());
            adView.setAdUnitId(mContext.getString(R.string.banner_ad_unit_id));
            adView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            adView.loadAd(aHolder.adRequest);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateViewsIn(aHolder.cardView, position);
            }
        }
    }

    /**
     * Calcula o maior width possivel do ad de forma a ocupar completamente o espaço do container.
     * O cálculo é baseado no tamanho da tela, número de colunas do grid e todas as possíveis
     * margens e paddings definidos para o layout de uma linha completa do grid.
     * Teve que ser implementado desta forma por não ser possível obter neste ponto o tamanho final
     * do container cujo width está definido como MATCH_PARENT.
     */
    private AdSize getComputedAdSize() {
        Resources res = mContext.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        float screenWidth = (float) metrics.widthPixels;

        int itemMargins =
                2 * (res.getDimensionPixelSize(R.dimen.item_card_padding) +
                        res.getDimensionPixelSize(R.dimen.grid_margins_vertical));

        int parentMargins = 2 * res.getDimensionPixelSize(R.dimen.grid_margins_vertical);

        int totalCols = res.getInteger(R.integer.grid_columns);

        double itemWidth = (screenWidth - (totalCols * itemMargins) - parentMargins) / totalCols;

        int widthDp = (int) (itemWidth / metrics.density);
        int heightDp = (int) (res.getDimensionPixelSize(R.dimen.item_image_height) /
                metrics.density);

        return new AdSize(widthDp, heightDp);
    }

    // Retorna o tamanho total da lista considerando a distribuição dos ad's
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount() + (AD_INTERVAL > 0 && mCursor.getCount() > AD_INITIAL_OFFSET
                ? (int) Math.ceil((mCursor.getCount() - AD_INITIAL_OFFSET) / (double) AD_INTERVAL)
                : 0);
    }

    // Define a distribuição dos ad's em itens próprios de acordo com as constantes definidas
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateViewsIn(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Interpolator interpolator =
                    AnimationUtils.loadInterpolator(mContext,
                            android.R.interpolator.linear_out_slow_in);

            viewToAnimate.setVisibility(View.VISIBLE);
            viewToAnimate.setTranslationY(mOffset);
            viewToAnimate.setAlpha(0.85f);
            viewToAnimate.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(interpolator)
                    .setDuration(1000L)
                    .start();

            mOffset *= 1.5f;
            lastPosition = position;
        }
    }

    public class RegularViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CardView cardView;
        public ImageView historyImage;
        public TextView historyTitle;

        public RegularViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            historyImage = (ImageView) itemView.findViewById(R.id.thumbnail);
            historyTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mPosition = getAdapterPosition();
            int randomValue = new Random().nextInt(INTERSTITIAL_RANGE);

            if (randomValue < INTERSTITIAL_FREQUENCY && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                return;
            }
            doRegularClick();
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public FrameLayout adContainer;
        public AdRequest adRequest;

        public AdViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            adContainer = (FrameLayout) itemView.findViewById(R.id.ad_container);

            adRequest = new AdRequest.Builder().build();
        }
    }
}
