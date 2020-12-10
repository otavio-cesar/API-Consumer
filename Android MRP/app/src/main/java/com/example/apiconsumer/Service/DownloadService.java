package com.example.apiconsumer.Service;

import android.graphics.drawable.Drawable;
import android.text.style.TtsSpan;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/*
* Responsável por fazer downloads.
*/
public class DownloadService {

    private final OkHttpClient client = new OkHttpClient();

    private String downloadUrl ="https://solucao-teste10.plune.com.br/REST/Produto.Produto/Download?__debug__=1&";

    public void downloadFotoGrande(String companyId, String id, Callback callback) {
        String urlComParametros = downloadUrl + "CompanyId="+ companyId + "&Id=" + id +
                "&_Produto.Produto.File=FotoGrande";

        Request request = new Request.Builder()
                .url(urlComParametros)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public String downloadFotoGrande_Metodo2(String companyId, String id) throws IOException {
        String urlComParametros = downloadUrl + "CompanyId="+ companyId + "&Id=" + id +
                "&_Produto.Produto.File=FotoGrande";
        return urlComParametros;
    }

}
