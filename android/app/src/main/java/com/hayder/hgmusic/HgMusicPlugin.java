package com.hayder.hgmusic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.InputStream;
import java.net.URL;

@CapacitorPlugin(name = "HgMusicPlugin")
public class HgMusicPlugin extends Plugin {

    @Override
    public void load() {
        Intent serviceIntent = new Intent(getContext(), MusicService.class);
        getContext().startForegroundService(serviceIntent);

        MusicService.callback = new MusicService.MusicServiceCallback() {
            @Override
            public void onPlay() {
                notifyListeners("play", new JSObject());
            }
            @Override
            public void onPause() {
                notifyListeners("pause", new JSObject());
            }
            @Override
            public void onNext() {
                notifyListeners("next", new JSObject());
            }
            @Override
            public void onPrevious() {
                notifyListeners("previous", new JSObject());
            }
        };
    }

    @PluginMethod
    public void updateNotification(PluginCall call) {
        String title = call.getString("title", "H.g Music");
        String artist = call.getString("artist", "");
        String artworkUrl = call.getString("artworkUrl", null);
        String artworkBase64 = call.getString("artworkBase64", null);
        boolean isPlaying = call.getBoolean("isPlaying", false);

        new Thread(() -> {
            Bitmap artwork = null;
            if (artworkBase64 != null && !artworkBase64.isEmpty()) {
                try {
                    String pure = artworkBase64;
                    int comma = pure.indexOf(",");
                    if (comma != -1) pure = pure.substring(comma + 1);
                    byte[] bytes = android.util.Base64.decode(pure, android.util.Base64.DEFAULT);
                    artwork = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } catch (Exception e) {
                    artwork = null;
                }
            } else if (artworkUrl != null) {
                try {
                    InputStream in = new URL(artworkUrl).openStream();
                    artwork = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    artwork = null;
                }
            }
            final Bitmap finalArtwork = artwork;

            if (MusicService.instance != null) {
                MusicService.instance.updateNotification(title, artist, finalArtwork, isPlaying);
            }
        }).start();

        call.resolve();
    }
}
