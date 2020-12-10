package com.example.apiconsumer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.apiconsumer.Service.DownloadService;
import com.example.apiconsumer.Service.LoginService;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity {

    private LoginService loginService;
    private DownloadService downloadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    * Função para gerar token.
    */
    public void handleLogin(View v){
        /*
        * Pega objeto View que contem os dados digitados.
        */
        EditText userEditText = findViewById(R.id.editTextUser);
        EditText passwordEditText = findViewById(R.id.editTextPassword);

        /*
        * Pega as informações digitadas propriamente ditas.
        */
        String user = userEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        /*
         * Faz chamada na api de autenticar.
         */
        autenciar(user, password);
    }

    private void autenciar(String user, String password){
        loginService = new LoginService();

        /*
        * O código declarado dentro do callback será executado de forma assíncrona.
        */
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String erro = responseBody.string();
                        Log.d("Autenticar: ", erro);
                    } else {
                        /*
                        * Pega o token contido na resposta da requisição.
                        */
                        JSONObject jsonResponse = new JSONObject(responseBody.string());
                        String token = jsonResponse.getString("UltraClassLogin");
                        /*
                         * Salva o token na tela.
                         */
                        TextView tokenTextView= findViewById(R.id.editTextToken);
                        tokenTextView.setText(token);
                        Log.d("Autenticar: ", "Sucesso.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        loginService.autenticar(user, password, callback);
    }

    /*
    * Função para download.
    */
    public void handleDownload(View v){
        /*
        *  Atribui valores aos parametros da requisição.
        */
        String idCompany = "992";
        String id = "11511";

        /*
        * Metodo normal, usa biblioteca HtppOk.
        * Apenas mostra imagem na tela.
        */
        mostraFotoNaTela(idCompany, id);

        /*
         * Metodo 2, usa biblioteca nativa do Android.
         * Faz o download e mostra imagem na tela.
         */
        //downloadFotoGrande_Metodo2(idCompany, id);
    }

    private void mostraFotoNaTela(String idCompany, String id){
        downloadService = new DownloadService();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String erro = responseBody.string();
                        Log.d("Download: ", erro);
                    } else {
                        /*
                        * Transforma a imagem que está na requisição em um Drawable.
                        */
                        Drawable d = Drawable.createFromStream( responseBody.byteStream() , "src name");

                        /*
                        * Mostra imagem na tela.
                        */
                        setImage(d);
                        Log.d("Download: ", "sucesso");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        downloadService.downloadFotoGrande(idCompany, id, callback); ;
    }

    /*
     * Roda na Thread principal da Activity.
     * Para alterar alguns elementos, é obrigatório rodar na Thread principal.
     */
    private void setImage(final Drawable image){
        runOnUiThread (new Thread(new Runnable() {
            public void run() {
                View v = findViewById(R.id.imageLayout);
                v.setBackground(image);
            }
        }));
    }

    /*
    * A classe AsyncTask executa uma operação que roda em segundo plano.
    */
    AsyncTask<String, String, String> runningTask;
    private void downloadFotoGrande_Metodo2(final String idCompany, final String id){
        if (runningTask != null)
            runningTask.cancel(true);
        runningTask = new LongOperation();
        try {
            /*
            * Pede permissão para poder salvar no external storage.
            * Os dados serão salvos em armazenamento/Wallpaper.
             */
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            /*
             * Busca url que tem o conteudo a ser baixado.
             */
            downloadService = new DownloadService();
            String url = downloadService.downloadFotoGrande_Metodo2(idCompany, id);

            /*
            * Executa o AsynckTask que fará o download.
            */
            runningTask.execute(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Responsável por fazer a chamada da api em um thread diferente da mainUiThread.
    */
    private final class LongOperation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                InputStream is;

                /*
                 * Faz requisicao e pega o conteudo.
                 */
                String urlComParametros = params[0];
                URLConnection url = new URL(urlComParametros).openConnection();
                url.connect();

                /*
                 * Recupera nome do arquivo.
                 */
                String[] split = url.getHeaderField("Content-disposition").split("filename=");
                String filename = split[split.length - 1];

                /*
                 * Faz o download.
                 */
                is = url.getInputStream();
                fazDownloadArquivo(is, filename);

                /*
                 * Se for uma imagem, mostra na tela.
                 */
                if(filename.contains(".jpg") || filename.contains("jpeg")) {
                    url = new URL(urlComParametros).openConnection();
                    url.connect();
                    is = url. getInputStream();
                    Drawable d = Drawable.createFromStream(is, "src name");
                    setImage(d);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }

        /*
         * Função responsavel por fazer o download do arquivo.
         */
        private String fazDownloadArquivo(InputStream is, String filename) throws FileNotFoundException {
            File wallpaperDirectory = new File("/sdcard/Wallpaper/");
            wallpaperDirectory.mkdirs();
            File outputFile = new File(wallpaperDirectory, filename);
            FileOutputStream output = new FileOutputStream(outputFile);
            try {
                int fileLength = 0; //connection.getContentLength();
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = is.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        is.close();
                        return null;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (is != null)
                        is.close();
                } catch (IOException ignored) {
                }
            }
            return null;
        }
    }

    /*
     * Método executado ao encerrar a activity.
     * Cancela alguma task que esteja rodando.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running task(s) to avoid memory leaks
        if (runningTask != null)
            runningTask.cancel(true);
    }

}
