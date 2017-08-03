package br.com.deyvidjlira.bollyfilmes.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.com.deyvidjlira.bollyfilmes.BuildConfig;
import br.com.deyvidjlira.bollyfilmes.FilmeDetalheActivity;
import br.com.deyvidjlira.bollyfilmes.ItemFilme;
import br.com.deyvidjlira.bollyfilmes.JSONUtil;
import br.com.deyvidjlira.bollyfilmes.MainActivity;
import br.com.deyvidjlira.bollyfilmes.R;
import br.com.deyvidjlira.bollyfilmes.data.FilmesContract;

/**
 * Created by Deyvid on 01/07/2017.
 */

public class FilmesSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 720; //60 segs x 720 minutos em 12 horas

    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public static final int NOTIFICATION_FILMES_ID = 10001;

    public FilmesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String ordem = preferences.getString(getContext().getString(R.string.prefs_ordem_key), "popular");
        String idioma = preferences.getString(getContext().getString(R.string.prefs_idioma_key), "pt-BR");

        try {
            String urlBase = "https://api.themoviedb.org/3/movie/" + ordem + "?";
            String apiKey = "api_key";
            String language = "language";

            Uri uriApi = Uri.parse(urlBase).buildUpon()
                    .appendQueryParameter(apiKey, BuildConfig.TMDB_API_KEY)
                    .appendQueryParameter(language, idioma)
                    .build();

            URL url = new URL(uriApi.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String linha;
            StringBuffer buffer = new StringBuffer();
            while((linha = reader.readLine()) != null) {
                buffer.append(linha);
                buffer.append("\n");
            }

            List<ItemFilme> itemFilmes = JSONUtil.fromJSONToList(buffer.toString());

            if(itemFilmes == null)
                return;

            for (ItemFilme itemFilme : itemFilmes) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FilmesContract.FilmeEntry._ID, itemFilme.getId());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_TITULO, itemFilme.getTitle());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_DESCRICAO, itemFilme.getOverview());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_POSTER_PATH, itemFilme.getPosterPath());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_CAPA_PATH, itemFilme.getBackdropPath());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_AVALIACAO, itemFilme.getVoteAverage());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_DATA_RELEASE, itemFilme.getReleaseDate());
                contentValues.put(FilmesContract.FilmeEntry.COLUMN_POPULARIDADE, itemFilme.getPopularidade());

                int update = getContext().getContentResolver().update(FilmesContract.FilmeEntry.buildUriForFilmes(itemFilme.getId()), contentValues, null, null);

                if(update == 0) {
                    getContext().getContentResolver().insert(FilmesContract.FilmeEntry.CONTENT_URI, contentValues);
                    notify(itemFilme);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notify(ItemFilme itemFilme) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String notifyPrefKey = getContext().getString(R.string.prefs_notify_filmes_key);
        String notifyPrefDefault = getContext().getString(R.string.prefs_notify_filmes_default);
        boolean notifyPrefs = sharedPreferences.getBoolean(notifyPrefKey, Boolean.parseBoolean(notifyPrefDefault));

        if(!notifyPrefs) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(itemFilme.getTitle())
                .setContentText(itemFilme.getOverview());

        Intent intent = new Intent(getContext(), FilmeDetalheActivity.class);
        Uri uri = FilmesContract.FilmeEntry.buildUriForFilmes(itemFilme.getId());
        intent.setData(uri);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getContext());
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_FILMES_ID, builder.build());
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest syncRequest = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle()).build();
            ContentResolver.requestSync(syncRequest);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account account = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if(accountManager.getPassword(account) == null) {
            if(!accountManager.addAccountExplicitly(account, "", null)) {
                return null;
            }

            onAccountCreated(account, context);
        }

        return account;
    }

    private static void onAccountCreated(Account account, Context context) {
        FilmesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(account, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
