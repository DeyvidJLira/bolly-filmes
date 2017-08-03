package br.com.deyvidjlira.bollyfilmes;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.deyvidjlira.bollyfilmes.sync.FilmesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    public static final String FILME_DETALHE_URI = "FILME";

    private boolean isTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.fragmentFilmeDetalhe) != null) {

            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentFilmeDetalhe, new FilmeDetalheFragment())
                        .commit();
            }

            isTablet = true;
        } else {
            isTablet = false;
        }

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMain);
        mainFragment.setUseFilmeDestaque(!isTablet);

        FilmesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onItemSelected(Uri uri) {
        if(isTablet) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            FilmeDetalheFragment filmeDetalheFragment = new FilmeDetalheFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(MainActivity.FILME_DETALHE_URI, uri);
            filmeDetalheFragment.setArguments(bundle);

            fragmentTransaction.replace(R.id.fragmentFilmeDetalhe, filmeDetalheFragment);
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(MainActivity.this, FilmeDetalheActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    }
}
