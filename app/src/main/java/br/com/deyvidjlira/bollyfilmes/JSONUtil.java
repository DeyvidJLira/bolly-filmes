package br.com.deyvidjlira.bollyfilmes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deyvid on 27/05/2017.
 */

public class JSONUtil {

    public static List<ItemFilme> fromJSONToList(String json) {
        List<ItemFilme> listFilmes = new ArrayList<>();
        try {
            JSONObject jsonBase = new JSONObject(json);
            JSONArray results = jsonBase.getJSONArray("results");

            for(int i = 0; i < results.length(); i++) {
                JSONObject filmeObject = results.getJSONObject(i);
                ItemFilme itemFilme = new ItemFilme(filmeObject);
                listFilmes.add(itemFilme);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listFilmes;
    }

}
