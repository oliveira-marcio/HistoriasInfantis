package com.abobrinha.caixinha.network;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.abobrinha.caixinha.R;


public class SocialUtils {
    private SocialUtils() {
    }

    public static void shareApp(Context c) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, c.getString(R.string.sharing_subject)
                    + " " + c.getString(R.string.app_name));
            String sAux = c.getString(R.string.sharing_text) + "\n\n";
            sAux = sAux + c.getString(R.string.sharing_link);
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            c.startActivity(Intent.createChooser(i, c.getString(R.string.sharing_title)));
        } catch (Exception e) {
            Toast.makeText(c, R.string.sharing_error, Toast.LENGTH_SHORT).show();
        }
    }
}
