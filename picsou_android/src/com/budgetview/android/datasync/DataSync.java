package com.budgetview.android.datasync;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import com.budgetview.android.App;
import com.budgetview.android.R;
import com.budgetview.shared.model.MobileModel;
import com.budgetview.shared.utils.ComCst;
import com.budgetview.shared.utils.Crypt;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.xml.XmlGlobParser;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class DataSync {
  public static final String URL_BV = "https://register.mybudgetview.fr:1443";
//"https://192.168.0.20:8443/";
  private static final String LOCAL_TEMP_FILE_NAME = "temp.xml";

  private Activity activity;
  private String email;
  private String password;

  public static interface Callback {
    void onActionFinished();

    void onActionFailed();

    void onConnectionUnavailable();
  }

  public DataSync(Activity activity, String email, String password) {
    this.activity = activity;
    this.email = email;
    this.password = password;
  }

  public void connect(Callback callback) {
    if (!canConnect()) {
      callback.onConnectionUnavailable();
      return;
    }

    if (idAndPasswordCorrect(email, password)) {
      callback.onActionFinished();
    }
    else {
      callback.onActionFailed();
    }
  }

  private boolean idAndPasswordCorrect(String email, String password) {
    // TODO: à remplacer par une connexion serveur
    return !"fail".equals(email);
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

        Crypt crypt = new Crypt(DataSync.this.password.toCharArray());

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
      catch (Exception e) {
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

  public boolean sendDownloadEmail(String email, Callback callback) {
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

  static public class EasySSLSocketFactory implements SocketFactory, LayeredSocketFactory {

    private SSLContext sslcontext = null;

    private static SSLContext createEasySSLContext() throws IOException {
      try {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{new EasyX509TrustManager(null)}, null);
        return context;
      }
      catch (Exception e) {
        throw new IOException(e.getMessage());
      }
    }

    private SSLContext getSSLContext() throws IOException {
      if (this.sslcontext == null) {
        this.sslcontext = createEasySSLContext();
      }
      return this.sslcontext;
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket, java.lang.String, int,
     *      java.net.InetAddress, int, org.apache.http.params.HttpParams)
     */
    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort,
                                HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
      int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
      int soTimeout = HttpConnectionParams.getSoTimeout(params);
      InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
      SSLSocket sslsock = (SSLSocket)((sock != null) ? sock : createSocket());

      if ((localAddress != null) || (localPort > 0)) {
        // we need to bind explicitly
        if (localPort < 0) {
          localPort = 0; // indicates "any"
        }
        InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
        sslsock.bind(isa);
      }

      sslsock.connect(remoteAddress, connTimeout);
      sslsock.setSoTimeout(soTimeout);
      return sslsock;

    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
     */
    public Socket createSocket() throws IOException {
      return getSSLContext().getSocketFactory().createSocket();
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
     */
    public boolean isSecure(Socket socket) throws IllegalArgumentException {
      return true;
    }

    /**
     * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket, java.lang.String, int,
     *      boolean)
     */
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                                                                                               UnknownHostException {
      return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    // -------------------------------------------------------------------
    // javadoc in org.apache.http.conn.scheme.SocketFactory says :
    // Both Object.equals() and Object.hashCode() must be overridden
    // for the correct operation of some connection managers
    // -------------------------------------------------------------------

    public boolean equals(Object obj) {
      return ((obj != null) && obj.getClass().equals(EasySSLSocketFactory.class));
    }

    public int hashCode() {
      return EasySSLSocketFactory.class.hashCode();
    }

  }

  public static class EasyX509TrustManager implements X509TrustManager {
    private X509TrustManager standardTrustManager = null;

    /**
     * Constructor for EasyX509TrustManager.
     */
    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
      super();
      TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      factory.init(keystore);
      TrustManager[] trustmanagers = factory.getTrustManagers();
      if (trustmanagers.length == 0) {
        throw new NoSuchAlgorithmException("no trust manager found");
      }
      this.standardTrustManager = (X509TrustManager)trustmanagers[0];
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
      standardTrustManager.checkClientTrusted(certificates, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
      if ((certificates != null) && (certificates.length == 1)) {
        certificates[0].checkValidity();
      }
      else {
        standardTrustManager.checkServerTrusted(certificates, authType);
      }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
      return this.standardTrustManager.getAcceptedIssuers();
    }

  }
}
