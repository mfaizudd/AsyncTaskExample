package net.faizud.asynctaskexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ImageView mainImage;
    Button openListButton;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainImage = findViewById(R.id.main_image);
        openListButton = findViewById(R.id.open_list_button);
        openListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });
        progressBar = findViewById(R.id.progress);
        try {
            new LoadMainImage().execute(new URL("https://api.thecatapi.com/v1/images/search?limit=1&page=1"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public class LoadMainImage extends AsyncTask<URL, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            URL url = urls[0];
            Bitmap result = null;
            try {
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JSONArray cats = new JSONArray(readStream(connection.getInputStream()));
                    for (int i = 0; i < cats.length(); i++) {
                        JSONObject cat = cats.getJSONObject(i);
                        URL imageURL = new URL(cat.getString("url"));
                        result = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mainImage.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
        }
    }

    public String readStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        StringBuilder sb = new StringBuilder();
        while(line!=null) {
            sb.append(line);
            sb.append("\n");
            line = reader.readLine();
        }
        return sb.toString();
    }
}
