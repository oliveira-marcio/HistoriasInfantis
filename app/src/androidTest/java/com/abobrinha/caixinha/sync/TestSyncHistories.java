package com.abobrinha.caixinha.sync;


import android.content.ContentValues;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.network.WordPressConn;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class TestSyncHistories {
    private final Context context = InstrumentationRegistry.getTargetContext();

    private final String EMPTY_URL = "http://google.com/?";
    private final String INVALID_URL = "http://google.com/ping?";
    private final String VALID_URL = WordPressConn.WORDPRESS_BASE_URL;

    private int mStatus;
    private ContentValues[] mData;

    /**
     * Teste dos possíveis valores de retorno do servidor da API:
     * - OK
     * - Dados inválidos
     * - Servidor fora do ar
     */
    @Test
    public void testServerStatus() {
        getDataFromUrl(EMPTY_URL, 1);
        String error = "Erro no teste de servidor fora do ar";
        assertEquals(error, PreferencesUtils.HISTORY_STATUS_SERVER_DOWN, mStatus);

        getDataFromUrl(INVALID_URL, 1);
        error = "Erro no teste de dados inválidos do servidor";
        assertEquals(error, PreferencesUtils.HISTORY_STATUS_SERVER_INVALID, mStatus);

        getDataFromUrl(VALID_URL, 1);
        error = "Erro no teste de dados válidos do servidor";
        assertEquals(error, PreferencesUtils.HISTORY_STATUS_OK, mStatus);
    }

    /**
     * Teste de retorno da API quando utilizado paginações diferentes dos resultados, ou seja,
     * os resultados finais devem ser os mesmos independente do número de dados por página
     * retornado do  servidor.
     */
    @Test
    public void testServerResponseInPages() {
        ContentValues[] dataSet1, dataSet2;
        int results_per_page1 = 10;
        int results_per_page2 = 30;

        getDataFromUrl(VALID_URL, results_per_page1);
        String error = "Erro para obter o primeiro conjunto de dados.";
        assertEquals(error, PreferencesUtils.HISTORY_STATUS_OK, mStatus);
        dataSet1 = mData;

        getDataFromUrl(VALID_URL, results_per_page2);
        error = "Erro para obter o segundo conjunto de dados.";
        assertEquals(error, PreferencesUtils.HISTORY_STATUS_OK, mStatus);
        dataSet2 = mData;

        error = "O tamanho dos conjuntos de dados não estão iguais.";
        assertEquals(error, dataSet1.length, dataSet2.length);

        error = "Os dados dos 2 conjutos não estão iguais.";
        for (int i = 0; i < dataSet1.length; i++) {
            TestSyncUtilities.validateCurrentRecord(error, dataSet2[i], dataSet1[i]);
        }
    }

    private void getDataFromUrl(String urlString, int results_per_page) {
        try {
            List<ContentValues> dataRetrieved = TestSyncUtilities.getDataFromAllApiPages(context, urlString, results_per_page);
            if (dataRetrieved == null) {
                mStatus = PreferencesUtils.HISTORY_STATUS_SERVER_DOWN;
                return;
            }

            mData = dataRetrieved.toArray(new ContentValues[0]);
            mStatus = PreferencesUtils.HISTORY_STATUS_OK;
        } catch (IOException e) {
            mStatus = PreferencesUtils.HISTORY_STATUS_SERVER_DOWN;
        } catch (JSONException e) {
            mStatus = PreferencesUtils.HISTORY_STATUS_SERVER_INVALID;
        }
    }
}
