package com.tarayici.app;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;

/** Dosyayi Android DownloadManager ile Downloads klasorune indirir. */
public class DownloadHelper {

    public static void download(Context ctx, String url, String userAgent,
                                String contentDisposition, String mime, String referer) {
        try {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            String name = URLUtil.guessFileName(url, contentDisposition, mime);

            if (mime != null && !mime.isEmpty()) r.setMimeType(mime);

            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null) r.addRequestHeader("Cookie", cookie);
            if (userAgent != null) r.addRequestHeader("User-Agent", userAgent);
            r.addRequestHeader("Referer", referer == null ? url : referer);

            r.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);

            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(r);
            Toast.makeText(ctx, "Indiriliyor: " + name, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
