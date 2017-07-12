package com.abobrinha.caixinha.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.sync.HistorySyncUtils;
import com.abobrinha.caixinha.sync.NotificationUtils;


public class ListHistoriesConfigureActivity extends AppCompatActivity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    int mActualCategoryPref = PreferencesUtils.CATEGORY_HISTORIES;
    int mActualOrderPref = PreferencesUtils.ORDER_DATE;

    private int[] mCategoryRadioOptions = new int[]{R.id.all_radio, R.id.favorites_radio};
    private int[] mOrderRadioOptions = new int[]{R.id.date_radio, R.id.title_radio};

    public ListHistoriesConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);

        setContentView(R.layout.list_histories_widget_configure);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mActualCategoryPref = PreferencesUtils.loadWidgetCategoryPref(this, mAppWidgetId);
        final RadioGroup categoryRadioGroup = (RadioGroup) findViewById(R.id.category_radio_group);
        categoryRadioGroup.check(mCategoryRadioOptions[mActualCategoryPref]);

        mActualOrderPref = PreferencesUtils.loadWidgetOrderPref(this, mAppWidgetId);
        final RadioGroup orderRadioGroup = (RadioGroup) findViewById(R.id.order_radio_group);
        orderRadioGroup.check(mOrderRadioOptions[mActualOrderPref]);

        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button okButton = (Button) findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = ListHistoriesConfigureActivity.this;

                int category;
                switch (categoryRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.favorites_radio:
                        category = PreferencesUtils.CATEGORY_FAVORITES;
                        break;
                    default:
                        category = PreferencesUtils.CATEGORY_HISTORIES;
                        break;
                }
                PreferencesUtils.saveWidgetCategoryPref(context, mAppWidgetId, category);

                int order;
                switch (orderRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.title_radio:
                        order = PreferencesUtils.ORDER_TITLE;
                        break;
                    default:
                        order = PreferencesUtils.ORDER_DATE;
                        break;
                }
                PreferencesUtils.saveWidgetOrderPref(context, mAppWidgetId, order);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ListHistoriesWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                NotificationUtils.updateWidgets(context);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        HistorySyncUtils.initialize(this);
    }
}

