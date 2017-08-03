package br.com.deyvidjlira.bollyfilmes;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.deyvidjlira.bollyfilmes.data.FilmesContract;

/**
 * Created by Deyvid on 14/05/2017.
 */

public class FilmesAdapter extends CursorAdapter {

    private final int VIEW_TYPE_DESTAQUE = 0;
    private final int VIEW_TYPE_ITEM = 1;

    private boolean useFilmeDestaque = false;

    public FilmesAdapter(Context  context, Cursor cursor) {
        super(context, cursor, 0); //O 0 é uma flag que indica o comportamento a ser adotado, 0 é o padrão.
    }

    public static class ItemFilmeHolder {
        ImageView poster;
        ImageView capa;
        TextView title;
        TextView description;
        TextView data;
        RatingBar rate;

        public ItemFilmeHolder(View view) {
            poster = (ImageView) view.findViewById(R.id.imageViewPoster);
            capa = (ImageView) view.findViewById(R.id.imageViewCapa);
            title = (TextView)  view.findViewById(R.id.textViewTitle);
            description = (TextView)  view.findViewById(R.id.textViewDescription);
            data = (TextView)  view.findViewById(R.id.textViewData);
            rate = (RatingBar)  view.findViewById(R.id.ratingBarRate);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch(viewType) {
            case VIEW_TYPE_DESTAQUE:
                layoutId = R.layout.item_filme_destaque;
                break;
            case VIEW_TYPE_ITEM:
                layoutId = R.layout.item_filme;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ItemFilmeHolder holder = new ItemFilmeHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ItemFilmeHolder holder = (ItemFilmeHolder) view.getTag();
        int viewType = getItemViewType(cursor.getPosition());

        int tituloIndex = cursor.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_TITULO);
        int descricaoIndex = cursor.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_DESCRICAO);
        int dataReleaseIndex = cursor.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_DATA_RELEASE);
        int posterIndex = cursor.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_POSTER_PATH);
        int capaIndex = cursor.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_CAPA_PATH);
        int avaliacaoIndex = cursor.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_AVALIACAO);

        switch (viewType) {
            case VIEW_TYPE_DESTAQUE:
                holder.title.setText(cursor.getString(tituloIndex));
                holder.rate.setRating(cursor.getFloat(avaliacaoIndex));
                new DownloadImageTask(holder.capa).execute(cursor.getString(capaIndex));
                break;
            case VIEW_TYPE_ITEM:
                holder.title.setText(cursor.getString(tituloIndex));
                holder.description.setText(cursor.getString(descricaoIndex));
                holder.data.setText(cursor.getString(dataReleaseIndex));
                holder.rate.setRating(cursor.getFloat(avaliacaoIndex));
                new DownloadImageTask(holder.poster).execute(cursor.getString(posterIndex));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useFilmeDestaque ? VIEW_TYPE_DESTAQUE : VIEW_TYPE_ITEM);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void setUseFilmeDestaque(boolean useFilmeDestaque) {
        this.useFilmeDestaque = useFilmeDestaque;
    }
}