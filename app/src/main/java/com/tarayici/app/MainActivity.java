package com.tarayici.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView web;
    private EditText urlBar;
    private Button dlBtn;
    private ProgressBar bar;
    private final MediaDetector detector = new MediaDetector();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 28) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }

        buildUi();
        setupWebView();

        detector.setListener(count -> runOnUiThread(() -> {
            dlBtn.setText("\u2B07 " + count);
            dlBtn.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }));

        web.loadUrl("https://www.google.com");
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);

        urlBar = new EditText(this);
        urlBar.setSingleLine(true);
        urlBar.setHint("Adres veya arama");
        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBar.setOnEditorActionListener((tv, id, ev) -> {
            load();
            return true;
        });
        top.addView(urlBar, new LinearLayout.LayoutParams(0, -2, 1f));

        Button go = new Button(this);
        go.setText("Git");
        go.setOnClickListener(v -> load());
        top.addView(go, new LinearLayout.LayoutParams(-2, -2));

        dlBtn = new Button(this);
        dlBtn.setText("\u2B07");
        dlBtn.setVisibility(View.GONE);
        dlBtn.setOnClickListener(v -> showMediaList());
        top.addView(dlBtn, new LinearLayout.LayoutParams(-2, -2));

        root.addView(top, new LinearLayout.LayoutParams(-1, -2));

        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        root.addView(bar, new LinearLayout.LayoutParams(-1, 8));

        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(root);
    }

    private void setupWebView() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) {
                bar.setProgress(p);
                bar.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        web.setWebViewClient(new BrowserClient(detector, url -> urlBar.setText(url)));

        web.setDownloadListener((url, ua, cd, mime, len) ->
                DownloadHelper.download(this, url, ua, cd, mime, web.getUrl()));
    }

    private void load() {
        String t = urlBar.getText().toString().trim();
        if (t.isEmpty()) return;
        if (t.startsWith("http://") || t.startsWith("https://")) {
            web.loadUrl(t);
        } else if (t.contains(".") && !t.contains(" ")) {
            web.loadUrl("https://" + t);
        } else {
            web.loadUrl("https://www.google.com/search?q=" + Uri.encode(t));
        }
    }

    private void showMediaList() {
        final ArrayList<String> list = detector.list();
        String[] names = new String[list.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = URLUtil.guessFileName(list.get(i), null, null);
        }
        new AlertDialog.Builder(this)
                .setTitle("Indir")
                .setItems(names, (d, i) -> DownloadHelper.download(
                        this, list.get(i), web.getSettings().getUserAgentString(),
                        null, null, web.getUrl()))
                .show();
    }

    @Override
    public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(k, e);
    }
}
