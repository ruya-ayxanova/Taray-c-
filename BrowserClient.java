package com.tarayici.app;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Sayfa olaylarini dinler ve video/ses linklerini yakalar. */
public class BrowserClient extends WebViewClient {

    public interface Callback {
        void onUrlChanged(String url);
    }

    private final MediaDetector detector;
    private final Callback callback;

    public BrowserClient(MediaDetector detector, Callback callback) {
        this.detector = detector;
        this.callback = callback;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        detector.clear();
        callback.onUrlChanged(url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        view.evaluateJavascript(MediaDetector.SCAN_JS, value -> {
            if (value == null) return;
            value = value.replace("\"", "").trim();
            if (value.isEmpty() || value.equals("null")) return;
            for (String u : value.split(" ")) detector.add(u);
        });
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String u = request.getUrl().toString();
        if (MediaDetector.isMediaUrl(u)) detector.add(u);
        return null;
    }
}
