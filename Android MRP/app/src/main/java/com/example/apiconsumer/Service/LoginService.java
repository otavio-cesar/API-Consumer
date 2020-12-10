package com.example.apiconsumer.Service;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/*
* Responsável por fazer a requisição da autenticação.
*/
public class LoginService {

    private final OkHttpClient client = new OkHttpClient();

    private String autenticacaoUrl ="https://solucao-teste10.plune.com.br/JSON/Ultra.LOGIN/LOGIN?__debug__=1&";

    public void autenticar(String user, String password, Callback callback) {
        String urlComParametros = autenticacaoUrl + "u="+ user + "&p=" + password;

        Request request = new Request.Builder()
                .url(urlComParametros)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

}
