package com.budgetview.android.datasync;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.budgetview.android.App;
import com.budgetview.android.R;
import com.budgetview.shared.model.MobileModel;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataSync {

  private static final String FILE_URL = "http://www.mybudgetview.com/files/mobile/globsdata.xml";
  private static final String LOCAL_TEMP_FILE_NAME = "temp.xml";

  private Activity activity;

  public static interface Callback {
    void onActionFinished();

    void onActionFailed();

    void onConnectionUnavailable();
  }

  public DataSync(Activity activity) {
    this.activity = activity;
  }

  public void connect(String email, String password, Callback callback) {
    if (!canConnect()) {
      callback.onConnectionUnavailable();
      return;
    }

    // TODO: à remplacer par une connexion serveur
    if (email != null && email.contains("user")) {
      callback.onActionFinished();
    }
    else {
      callback.onActionFailed();
    }
  }

  public void load(Callback callback) {
    if (!canConnect()) {
      callback.onConnectionUnavailable();
      return;
    }

    new DownloadWebpage(activity, callback).execute();
  }

  public void loadDemoFile() throws IOException {
    InputStream tempFile = null;
    try {
      tempFile = activity.getResources().openRawResource(R.raw.globsdata);
      Reader reader = new InputStreamReader(tempFile);
      parseAndLoadContent(reader);
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

  public boolean loadTempFile() {
    InputStream tempFile = null;
    try {
      tempFile = activity.openFileInput(LOCAL_TEMP_FILE_NAME);
      Reader reader = new InputStreamReader(tempFile);
      parseAndLoadContent(reader);
      return true;
    }
    catch (FileNotFoundException e) {
      return false;
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

  private class DownloadWebpage extends AsyncTask<URL, Integer, Boolean> {

    private Activity activity;
    private Callback callback;

    public DownloadWebpage(Activity activity, Callback callback) {
      this.activity = activity;
      this.callback = callback;
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
        callback.onActionFailed();
      }

      callback.onActionFinished();
    }

    private Boolean downloadUrl() throws IOException {

      // TODO: remplacer par l'appel serveur

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
        parseAndLoadContent(reader);

        PrintWriter writer = new PrintWriter(activity.openFileOutput(LOCAL_TEMP_FILE_NAME, Context.MODE_PRIVATE));
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

  private void parseAndLoadContent(Reader reader) {
    App app = (App)this.activity.getApplication();
    GlobRepository repository = app.getRepository();
    repository.deleteAll();
    XmlGlobParser.parse(MobileModel.get(), repository, reader, "globs");
    app.forceLocale("fr");
  }

  private boolean canConnect() {
    ConnectivityManager connMgr = (ConnectivityManager)
      activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }
}
