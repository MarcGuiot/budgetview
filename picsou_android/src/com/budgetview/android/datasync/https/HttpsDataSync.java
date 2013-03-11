package com.budgetview.android.datasync.https;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import com.budgetview.android.App;
import com.budgetview.android.R;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.shared.model.MobileModel;
import com.budgetview.shared.utils.ComCst;
import com.budgetview.shared.utils.Crypt;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

public class HttpsDataSync implements DataSync {
  public static final String URL_BV = "https://register.mybudgetview.fr:1443";
//"https://192.168.0.20:8443";
  private static final String LOCAL_TEMP_FILE_NAME = "temp.xml";

  private Activity activity;
  private String email;
  private String password;

  public HttpsDataSync(Activity activity) {
    this.activity = activity;
  }

  public void setUser(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public void load(DataSyncCallback callback) {
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
    private DataSyncCallback callback;

    public DownloadWebpage(Activity activity, DataSyncCallback callback) {
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
      else {
        callback.onActionFinished();
      }
    }


    private Boolean downloadUrl() throws IOException {
      InputStream inputStream = null;
      HttpResponse response = null;
      HttpGet get = null;
      try {
        DefaultHttpClient client = createHttpClient();

        Crypt crypt = new Crypt(HttpsDataSync.this.password.toCharArray());

        Uri.Builder uri = Uri.parse(URL_BV + ComCst.GET_MOBILE_DATA).buildUpon();
        uri.appendQueryParameter(ComCst.MAIL, URLEncoder.encode(email, "UTF-8"));

        String data = Crypt.encodeSHA1AndHex(crypt.encodeData(email.getBytes("UTF-8")));
        uri.appendQueryParameter(ComCst.CRYPTED_INFO, URLEncoder.encode(data, "UTF-8"));

        get = new HttpGet(uri.toString());
        response = client.execute(get, new BasicHttpContext());

        if (response.getStatusLine().getStatusCode() != 200) {
          return false;
        }
        Header statusHeader = response.getFirstHeader(ComCst.STATUS);
        Header majorVersionHeader = response.getFirstHeader(ComCst.MAJOR_VERSION_NAME);
        Header minorVersionHeader = response.getFirstHeader(ComCst.MINOR_VERSION_NAME);
        if (majorVersionHeader == null || minorVersionHeader == null || statusHeader == null) {
          return false;
        }
        if (!statusHeader.getValue().equalsIgnoreCase("ok")){
          return false;
        }
        int majorVersion = Integer.parseInt(majorVersionHeader.getValue());
        int minorVersion = Integer.parseInt(minorVersionHeader.getValue());
        if (majorVersion != MobileModel.MAJOR_VERSION) {

          // TODO mettre un message pour dire qu'il faut upgrader la version:
          // majorVersion > MobileModel.MAJOR_VERSION ==> la mobile
          // majorVersion < MobileModel.MAJOR_VERSION ==> BV
          return false;
        }
        inputStream = response.getEntity().getContent();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Files.copyStream(inputStream, stream);

        String content = crypt.decodeAndUnzipData(stream.toByteArray());

        StringReader reader = new StringReader(content);
        parseAndLoadContent(reader);

        PrintWriter writer = new PrintWriter(activity.openFileOutput(LOCAL_TEMP_FILE_NAME, Context.MODE_PRIVATE));
        writer.write(content);
        writer.close();

        return true;
      }
      catch (Throwable e) {
        return false;
      }
      finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (IOException e) {
          }
        }
      }
    }
  }

  private DefaultHttpClient createHttpClient() {
    DefaultHttpClient client;
    SchemeRegistry schemeRegistry = new SchemeRegistry();

    schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
    ThreadSafeClientConnManager clientConnectionManager =
      new ThreadSafeClientConnManager(new BasicHttpParams(), schemeRegistry);

    client = new DefaultHttpClient(clientConnectionManager, new BasicHttpParams());
    return client;
  }

  public boolean sendDownloadEmail(String email, DataSyncCallback callback) {
    if (!canConnect()) {
      callback.onConnectionUnavailable();
      return false;
    }

    try {
      DefaultHttpClient client = createHttpClient();

      Uri.Builder uri = Uri.parse(URL_BV + ComCst.SEND_MAIL_REMINDER_FROM_MOBILE).buildUpon();
      uri.appendQueryParameter(ComCst.MAIL, URLEncoder.encode(email, "UTF-8"));
      uri.appendQueryParameter("lang", URLEncoder.encode(email, "fr"));

      HttpPost post = new HttpPost(uri.toString());
      HttpResponse response = client.execute(post, new BasicHttpContext());

      if (response.getStatusLine().getStatusCode() != 200) {
        return false;
      }
      return true;
    }
    catch (Exception e) {
      return false;
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
