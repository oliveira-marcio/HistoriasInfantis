package com.abobrinha.caixinha.network;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.abobrinha.caixinha.R;

import java.util.List;


public class SocialUtils {

    public static final int WEB = -1;
    public static final int EMAIL = 0;
    public static final int BLOG = 1;
    public static final int YOUTUBE = 2;
    public static final int FACEBOOK = 3;
    public static final int TWITTER = 4;
    public static final int INSTAGRAM = 5;
    public static final int PINTEREST = 6;

    public static final int[] SOCIAL_IMAGE_IDS = {
            R.drawable.ic_email,
            R.drawable.ic_wordpress,
            R.drawable.ic_youtube,
            R.drawable.ic_facebook,
            R.drawable.ic_twitter,
            R.drawable.ic_instagram,
            R.drawable.ic_pinterest
    };

    public static final String[] SOCIAL_NAMES = new String[]{
            "E-mail",
            "Blog",
            "YouTube",
            "Facebok",
            "Twitter",
            "Instagram",
            "Pinterest"
    };

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

    public static void openExternalLink(Context c, int linkType, String url) {
        final String EMAIL_BLOG = "abobrinhastudios@gmail.com";

        final String USER_YOUTUBE = "UC9P5Q7CsPk0JUOvZF-uTQZA/";
        final String USER_FACEBOOK_WEB = "abobrinhastudios";
        final String USER_FACEBOOK_APP = "1603776923277775";
        final String USER_TWITTER = "abobrinhastudio";
        final String USER_INSTAGRAM = "abobrinhastudios";
        final String USER_PINTEREST = "abobrinhastudio";

        final String URL_BLOG = "https://historiasinfantisabobrinha.wordpress.com";
        final String URL_YOUTUBE = "https://www.youtube.com/channel/";
        final String URL_FACEBOOK = "https://www.facebook.com/";
        final String URL_TWITTER = "https://twitter.com/";
        final String URL_INSTAGRAM = "http://instagram.com/";
        final String URL_PINTEREST = "https://www.pinterest.com/";

        final String INTENT_FACEBOOK = "fb://page/";
        final String INTENT_TWITTER = "twitter://user?screen_name=";
        final String INTENT_INSTAGRAM = "http://instagram.com/_u/";
        final String INTENT_PINTEREST = "pinterest://www.pinterest.com/";

        final String PACKAGE_FACEBOOK = "com.facebook.katana";
        final String PACKAGE_TWITTER = "com.twitter.android";
        final String PACKAGE_INSTAGRAM = "com.instagram.android";
        final String PACKAGE_PINTEREST = "com.pinterest";

        switch (linkType) {
            case WEB:
                openWebPage(c, url);
                break;
            case BLOG:
                openWebPage(c, URL_BLOG);
                break;
            case YOUTUBE:
                openWebPage(c, URL_YOUTUBE + USER_YOUTUBE);
                break;
            case FACEBOOK:
                openSocialApp(c, PACKAGE_FACEBOOK, INTENT_FACEBOOK + USER_FACEBOOK_APP,
                        URL_FACEBOOK + USER_FACEBOOK_WEB, null);
                break;
            case TWITTER:
                openSocialApp(c, PACKAGE_TWITTER, INTENT_TWITTER, URL_TWITTER, USER_TWITTER);
                break;
            case INSTAGRAM:
                openSocialApp(c, PACKAGE_INSTAGRAM, INTENT_INSTAGRAM, URL_INSTAGRAM, USER_INSTAGRAM);
                break;
            case PINTEREST:
                openSocialApp(c, PACKAGE_PINTEREST, INTENT_PINTEREST, URL_PINTEREST, USER_PINTEREST);
                break;
            case EMAIL:
                openEmailApp(c, EMAIL_BLOG);
                break;
        }
    }

    private static void openWebPage(Context c, String url) {
        if (TextUtils.isEmpty(url)) return;
        Uri uri = Uri.parse(url);
        c.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private static void openEmailApp(Context c, String contact) {
        String[] toContact = {contact};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, toContact);
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        }
    }

    private static void openSocialApp(Context c, String packageIntent, String socialNameIntent,
                                      String socialUrl, String user) {
        Uri uri = Uri.parse(socialNameIntent + user);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(packageIntent);

        if (isIntentAvailable(c, intent)) {
            c.startActivity(intent);
        } else {
            openWebPage(c, socialUrl + user);
        }
    }

    private static boolean isIntentAvailable(Context c, Intent intent) {
        final PackageManager packageManager = c.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
