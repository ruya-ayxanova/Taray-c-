package com.tarayici.app;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Sayfada bulunan video/ses linklerini tutar. */
public class MediaDetector {

    public interface Listener {
        void onChanged(int count);
    }

    /** Sayfadaki video, audio ve source etiketlerinin linklerini bosluk ile birlestirip dondurur. */
    public static final String SCAN_JS =
            "(function(){var r=[];document.querySelectorAll('video,audio,source').forEach("
            + "function(e){var s=e.currentSrc||e.src;if(s&&s.indexOf('http')==0)r.push(s)});"
            + "return r.join(' ');})()";

    private static final String[] EXT = {
            ".mp4", ".mp3", ".m4a", ".webm", ".ogg", ".wav", ".mkv", ".aac"
    };

    private final LinkedHashSet<String> found = new LinkedHashSet<>();
    private Listener listener;

    public void setListener(Listener l) {
        listener = l;
    }

    public static boolean isMediaUrl(String url) {
        String l = url.toLowerCase();
        for (String e : EXT) {
            if (l.contains(e)) return true;
        }
        return false;
    }

    public synchronized void add(String url) {
        if (url == null || !url.startsWith("http")) return;
        if (found.add(url) && listener != null) listener.onChanged(found.size());
    }

    public synchronized void clear() {
        found.clear();
        if (listener != null) listener.onChanged(0);
    }

    public synchronized ArrayList<String> list() {
        return new ArrayList<>(found);
    }
}
