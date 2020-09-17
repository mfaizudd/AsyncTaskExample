package net.faizud.asynctaskexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView mainList;
    GridImageAdapter adapter;
    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mainList = findViewById(R.id.main_list);
        adapter = new GridImageAdapter(getApplicationContext(), new ArrayList<ArrayList<Bitmap>>());
        loadMore();
        mainList.setAdapter(adapter);
        mainList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    loadMore();
                }
            }
        });
    }

    void loadMore() {
        try {
            new LoadMoreImages(adapter).execute(new URL("https://api.thecatapi.com/v1/images/search?limit=9&page="+page));
            page++;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public class GridImageAdapter extends BaseAdapter {

        private final Context context;
        private ArrayList<ArrayList<Bitmap>> bitmaps;
        private int[] layouts = new int[] {
                R.layout.list_item1,
                R.layout.list_item2,
                R.layout.list_item3,
                R.layout.list_item4
        };

        GridImageAdapter(Context context, ArrayList<ArrayList<Bitmap>> bitmaps) {
            this.context = context;
            this.bitmaps = bitmaps;
        }

        void addBitmaps(ArrayList<Bitmap> bitmaps) {
            this.bitmaps.add(bitmaps);
            notifyDataSetChanged();
        }

        void setBitmaps(int i, ArrayList<Bitmap> bitmaps) {
            this.bitmaps.set(i, bitmaps);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return bitmaps.size();
        }

        @Override
        public ArrayList<Bitmap> getItem(int i) {
            return bitmaps.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view==null)
            {
                view = LayoutInflater.from(context).inflate(layouts[i%4], viewGroup, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            holder = (ViewHolder) view.getTag();
            ArrayList<Bitmap> currentBitmaps = bitmaps.get(i);
            if(currentBitmaps.size() <= 0) {
                holder.loadingBar.setVisibility(View.VISIBLE);
            }
            else {
                holder.loadingBar.setVisibility(View.GONE);
                for (int j = 0; j < currentBitmaps.size(); j++) {
                    int totalImages = holder.images.length;
                    holder.images[j%totalImages].setImageBitmap(currentBitmaps.get(j));
                }
            }
            return view;
        }

        class ViewHolder {
            ImageView[] images;
            View loadingBar;

            ViewHolder(View view) {
                images = new ImageView[] {
                  view.findViewById(R.id.image1),
                  view.findViewById(R.id.image2),
                  view.findViewById(R.id.image3),
                  view.findViewById(R.id.image4),
                  view.findViewById(R.id.image5),
                  view.findViewById(R.id.image6),
                  view.findViewById(R.id.image7),
                  view.findViewById(R.id.image8),
                  view.findViewById(R.id.image9),
                };
                loadingBar = view.findViewById(R.id.loading_bar);
            }
        }
    }

    static class LoadMoreImages extends AsyncTask<URL, Void, ArrayList<Bitmap>> {

        GridImageAdapter adapter;
        ArrayList<Bitmap> loadedBitmaps;
        int index;

        LoadMoreImages(GridImageAdapter adapter) {
            this.adapter = adapter;
            index = this.adapter.getCount();
            loadedBitmaps = new ArrayList<Bitmap>();
            this.adapter.addBitmaps(loadedBitmaps);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(URL... urls) {
            URL url = urls[0];
            try {
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JSONArray cats = new JSONArray(readStream(connection.getInputStream()));
                    for (int i = 0; i < cats.length(); i++) {
                        JSONObject cat = cats.getJSONObject(i);
                        URL imageURL = new URL(cat.getString("url"));
                        Log.d("CAT URL", cat.getString("url"));
                        Bitmap bmp = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                        loadedBitmaps.add(bmp);
                        publishProgress();
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return loadedBitmaps;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            adapter.setBitmaps(index, bitmaps);
        }

        String readStream(InputStream input) throws IOException {
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
}
