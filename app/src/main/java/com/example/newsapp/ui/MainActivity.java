package com.example.newsapp.ui;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.adapter.NewsAdapter;
import com.example.newsapp.model.NewsModel;
import com.example.newsapp.R;
import com.example.newsapp.receivers.InternetCheckReceiver;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    public static String API_URL = "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json";
    private RecyclerView newsrecyclerView;
    NewsAdapter mAdapter;
    String[] vals = {"New-to-Old", "Old-to-New"};
    Spinner spinner;
    List<NewsModel> data = new ArrayList<>();

    InternetCheckReceiver internetCheckReceiver;
    ViewDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main, null);
        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.e("notification","refresh Token:" + token);
            }
        });
        init();
        internetCheckReceiver = new InternetCheckReceiver() {
            @Override
            protected void onNetworkConnected() {
                new GetNewsAPI().execute();
            }

            @Override
            protected void onNetworkDisConnected() {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(internetCheckReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(internetCheckReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to Exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user pressed "yes", then he is allowed to exit from application
                    finish();
                }
            });
            builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user select "No", just cancel this dialog and continue with app
                    dialog.cancel();
                }
            });
            AlertDialog alert=builder.create();
            alert.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init() {
        newsrecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vals);
        spinner.setAdapter(ad);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = adapterView.getItemAtPosition(position).toString();
                if (!data.isEmpty()) {
                    if (selectedItem.equals("New-to-Old")) {
                        // Setup and Handover data to recyclerview
                        Collections.sort(data, new Comparator<NewsModel>() {
                            public int compare(NewsModel o1, NewsModel o2) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                try {
                                    if (format.parse(o1.publishAt) == null || format.parse(o2.publishAt) == null)
                                        return 0;
                                    return format.parse(o2.publishAt).compareTo(format.parse(o1.publishAt));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    } else {
                        // Setup and Handover data to recyclerview
                        Collections.sort(data, new Comparator<NewsModel>() {
                            public int compare(NewsModel o1, NewsModel o2) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                try {
                                    if (format.parse(o1.publishAt) == null || format.parse(o2.publishAt) == null)
                                        return 0;
                                    return format.parse(o1.publishAt).compareTo(format.parse(o2.publishAt));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                    mAdapter = new NewsAdapter(MainActivity.this, data);
                    newsrecyclerView.setAdapter(mAdapter);
                    newsrecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private class GetNewsAPI extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL(API_URL);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(20 * 1000);
                conn.setReadTimeout(20 * 1000);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }
            try {
                int response_code = conn.getResponseCode();
                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // Pass data to onPostExecute method
                    return (result.toString());
                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //this method will be running on UI thread
            pdLoading.dismiss();
            data = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jArray = jsonObject.getJSONArray("articles");
                // Extract data from json and store into ArrayList as class objects
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    NewsModel newsModel = new NewsModel(json_data.getString("author"),
                            json_data.getString("title"),
                            json_data.getString("description"),
                            json_data.getString("url"),
                            json_data.getString("urlToImage"),
                            json_data.getString("publishedAt"),
                            json_data.getString("content"));
                    data.add(newsModel);
                }
                mAdapter = new NewsAdapter(MainActivity.this, data);
                newsrecyclerView.setAdapter(mAdapter);
                newsrecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}