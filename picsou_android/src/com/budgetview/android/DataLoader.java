package com.budgetview.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.budgetview.shared.model.MobileModel;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class DataLoader {

  private static final String FILE_URL = "http://www.mybudgetview.com/files/mobile/globsdata.xml";
  private static final String TEMP_FILE_NAME = "temp.xml";

  private Activity activity;

  public DataLoader(Activity activity) {
    this.activity = activity;
  }

  public void load() {
    ConnectivityManager connMgr = (ConnectivityManager)
      activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo == null || !networkInfo.isConnected()) {
      try {
        loadTempFile();
        onLoadFinished();
      }
      catch (Exception e) {
        showError("No internet connection available");
      }
      return;
    }

    new DownloadWebpage(activity).execute();
  }

  protected void loadTempFile() throws IOException {
    FileInputStream tempFile = null;
    try {
      tempFile = activity.openFileInput(TEMP_FILE_NAME);
      Reader reader = new InputStreamReader(tempFile);
      doLoad(reader);
    }
    finally {
      if (tempFile != null) {
        try {
          tempFile.close();
        }
        catch (IOException e) {
        }
      }
    }

  }

  protected abstract void onLoadFinished();

  private class DownloadWebpage extends AsyncTask<URL, Integer, Boolean> {

    private Activity activity;

    public DownloadWebpage(Activity activity) {
      this.activity = activity;
    }

    protected Boolean doInBackground(URL... urls) {
      try {
        return downloadUrl();
      }
      catch (IOException e) {
        return false;
      }
    }

    protected void onPostExecute(Boolean loadSuccessful) {
      if (!loadSuccessful) {
        showError("Are you sure you want to exit?");
      }

      onLoadFinished();
    }

    private Boolean downloadUrl() throws IOException {
      InputStream inputStream = null;
      try {
        URL url = new URL(FILE_URL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setReadTimeout(10000 /* milliseconds */);
        connection.setConnectTimeout(15000 /* milliseconds */);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        int response = connection.getResponseCode();

        inputStream = connection.getInputStream();

        String content = Files.loadStreamToString(inputStream, "UTF-8");
        StringReader reader = new StringReader(content);
        doLoad(reader);

        PrintWriter writer = new PrintWriter(activity.openFileOutput(TEMP_FILE_NAME, Context.MODE_PRIVATE));
        writer.write(content);
        writer.close();

        return true;
      }
      finally {
        if (inputStream != null) {
          inputStream.close();
        }
      }
    }
  }

  private void doLoad(Reader reader) {
    App app = (App)this.activity.getApplication();
    GlobRepository repository = app.getRepository();
    XmlGlobParser.parse(MobileModel.get(), repository, reader, "globs");
    app.forceLocale("fr");
  }

  private void showError(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setMessage(message)
      .setCancelable(false)
      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          activity.finish();
        }
      });
    AlertDialog alert = builder.create();
    alert.show();
  }
}
