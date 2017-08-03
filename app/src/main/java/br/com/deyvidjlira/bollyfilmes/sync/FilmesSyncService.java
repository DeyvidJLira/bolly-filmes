package br.com.deyvidjlira.bollyfilmes.sync;

import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Deyvid on 27/06/2017.
 */

public class FilmesSyncService extends Service {

    private static FilmesSyncAdapter filmesSyncAdapter = null;

    private static final Object lock = new Object();

    @Override
    public void onCreate() {
        synchronized (lock) {
            if(filmesSyncAdapter == null) {
                filmesSyncAdapter = new FilmesSyncAdapter(getApplicationContext(), true);
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return filmesSyncAdapter.getSyncAdapterBinder();
    }
}
