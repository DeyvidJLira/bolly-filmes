package br.com.deyvidjlira.bollyfilmes;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.deyvidjlira.bollyfilmes.data.FilmesContract;

public class FilmeDetalheFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri filmeUri;

    private TextView textViewTitulo;
    private TextView textViewDataRelease;
    private TextView textViewDescricao;
    private RatingBar ratingBarAvaliacao;
    private ImageView imageViewCapa;
    private ImageView imageViewPoster;

    private static final int FILME_DETALHE_LOADER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            filmeUri = getArguments().getParcelable(MainActivity.FILME_DETALHE_URI);
        }

        getLoaderManager().initLoader(FILME_DETALHE_LOADER, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_filme_detalhe, container, false);

        textViewTitulo = (TextView) view.findViewById(R.id.textViewTitle);
        textViewDataRelease = (TextView) view.findViewById(R.id.textViewData);
        textViewDescricao = (TextView) view.findViewById(R.id.textViewDescription);
        ratingBarAvaliacao = (RatingBar) view.findViewById(R.id.ratingBarRate);
        imageViewCapa = (ImageView) view.findViewById(R.id.imageViewCapa);
        imageViewPoster = (ImageView) view.findViewById(R.id.imageViewPoster);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                FilmesContract.FilmeEntry._ID,
                FilmesContract.FilmeEntry.COLUMN_TITULO,
                FilmesContract.FilmeEntry.COLUMN_DESCRICAO,
                FilmesContract.FilmeEntry.COLUMN_POSTER_PATH,
                FilmesContract.FilmeEntry.COLUMN_CAPA_PATH,
                FilmesContract.FilmeEntry.COLUMN_AVALIACAO,
                FilmesContract.FilmeEntry.COLUMN_DATA_RELEASE
        };

        return new CursorLoader(getContext(), filmeUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() < 1)
            return;

        if(data.moveToFirst()) {
            int tituloIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_TITULO);
            int descricaoIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_DESCRICAO);
            int dataReleaseIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_DATA_RELEASE);
            int avaliacaoIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_AVALIACAO);
            int posterIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_POSTER_PATH);
            int capaIndex = data.getColumnIndex(FilmesContract.FilmeEntry.COLUMN_CAPA_PATH);

            String titulo = data.getString(tituloIndex);
            String descricao = data.getString(descricaoIndex);
            String dataRelease = data.getString(dataReleaseIndex);
            float avaliacao = data.getFloat(avaliacaoIndex);
            String poster = data.getString(posterIndex);
            String capa = data.getString(capaIndex);

            textViewTitulo.setText(titulo);
            textViewDescricao.setText(descricao);
            textViewDataRelease.setText(dataRelease);
            ratingBarAvaliacao.setRating(avaliacao);

            Picasso.with(getContext()).load(capa).into(imageViewCapa);

            if(imageViewPoster != null) {
                Picasso.with(getContext())
                        .load(poster)
                        .placeholder(R.drawable.progress_animation)
                        .into(imageViewPoster);
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
