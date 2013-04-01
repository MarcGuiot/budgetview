package org.designup.picsou.gui.config;

import com.budgetview.shared.model.MobileModel;
import com.budgetview.shared.utils.ComCst;
import com.budgetview.shared.utils.Crypt;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.MD5PasswordBasedEncryptor;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.AppPaths;
import org.designup.picsou.gui.utils.KeyService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Inline;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ConfigService {

  public static final String COM_APP_LICENSE_URL = PicsouApplication.APPNAME + ".license.url";
  public static final String COM_APP_MOBILE_URL = PicsouApplication.APPNAME + ".mobile.url";
  public static final String COM_APP_FTP_URL = PicsouApplication.APPNAME + ".license.ftp.url";
  public static final String HEADER_TO_MAIL = "toMail";
  public static final String HEADER_MAIL = "mail";
  public static final String HEADER_MAIL_TITLE = "title";
  public static final String HEADER_MAIL_CONTENT = "content";
  public static final String HEADER_SIGNATURE = "signature";
  public static final String HEADER_IS_VALIDE = "isValide";
  public static final String HEADER_CODE = "code";
  public static final String HEADER_COUNT = "count";
  public static final String HEADER_MAIL_SENT = "mailSent";
  public static final String HEADER_MAIL_SENT_FAILED = "mailSentFailed";
  public static final String HEADER_STATUS = "status";
  public static final String HEADER_MAIL_UNKNOWN = "mailUnknown";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT = "activationCodeNotValideMailNotSent";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT = "activationCodeNotValideMailSent";
  public static final String HEADER_APPLICATION_VERSION = "applicationVersion";
  public static final String HEADER_CONFIG_VERSION = "configVersion";
  public static final String HEADER_NEW_CONFIG_VERSION = "newConfigVersion";
  public static final String HEADER_JAR_VERSION = "jarVersion";
  public static final String HEADER_NEW_JAR_VERSION = "newJarVersion";
  public static final String HEADER_REPO_ID = "repoId";
  public static final String HEADER_USE_INFO = "use";
  public static final String REQUEST_FOR_REGISTER = "/register";
  public static final String REQUEST_FOR_CONFIG = "/requestForConfig";
  public static final String REQUEST_FOR_MAIL = "/mailTo";
  public static final String REQUEST_SEND_MAIL = "/sendMailToUs";
  public static final String REQUEST_CLIENT_TO_SERVER_DATA = "/sendMobileData";
  public static final String CODING = "coding";
  public static final String SOME_PASSWORD = "HdsB 8(Rfm";
  public static final String SEND_USE_INFO = "/sendUseInfo";
  public static final String HEADER_BAD_ADRESS = "badAdress";
  public static final String MOBILE_SALT = "d48(cWqH";

  public static final String SUPPORT_EMAIL = "support";
  public static final String ADMIN_EMAIL = "admin";

  static final byte[] expectedPublicKey = {48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1,
                                           15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -83, 16, 42, 127, -66, -24, 109, 66, 114,
                                           8, -45, 44, 99, 77, -86, 60, 41, -113, -113, -123, 57, 54, 28, 40, -29, 73, 4, -103,
                                           66, -108, 26, 29, -38, -101, 23, -66, 31, -125, 52, -59, 51, -80, 121, 22, -8, -18,
                                           -57, -48, 63, 15, 30, -15, -19, -113, 90, 7, -75, -31, 1, 77, 95, 118, -79, -102, -34,
                                           -87, -46, -118, -23, 38, -30, -97, 26, -125, 79, -115, -9, 110, -110, -20, 14, -93,
                                           49, -111, 78, -73, -9, -29, -22, -41, -91, -47, -37, 81, 23, -88, 9, -32, -116, 17,
                                           32, -121, -114, 14, -99, -117, 120, 86, -27, -122, -35, 103, -104, -97, 108, 34, 55,
                                           -34, 96, -56, -64, -5, -5, 90, -120, 8, -84, 25, -105, 62, -83, 36, 115, 114, 97, 22,
                                           -120, -29, 3, -79, 85, 49, 81, -70, -54, 13, -35, -28, 117, 75, 14, -19, -84, -98, 33,
                                           125, -54, -93, -15, -1, -15, 87, -114, 104, -27, -6, 22, 11, 63, 39, -46, 106, -42, 70,
                                           -107, -40, 103, -120, 89, 2, 126, -9, 6, -21, 57, -34, -116, -36, 115, -105, 113, -35,
                                           59, -64, -121, 96, -67, -122, 87, 17, 30, 119, 70, -104, -50, 125, -12, 66, -100, 101,
                                           -82, -62, 24, -95, -91, 58, 55, -88, 34, -41, -100, -13, -101, -74, 52, 115, -97, -3,
                                           124, 59, 15, -50, 71, -16, -17, -26, -124, 53, -120, 46, -53, 36, 103, -86, -92, -57,
                                           -31, -77, -106, -30, -88, -18, -48, -117, 39, 107, 2, 3, 1, 0, 1};

  private final String LICENSE_SERVER_URL;
  private final String MOBILE_SERVER_URL;
  private final String FTP_SERVER_URL;
  private long localJarVersion = -1;
  private long localConfigVersion = -1;
  private String applicationVersion;
  private UserState userState = null;
  private boolean mailSend = false;
  private DownloadThread dowloadJarThread;
  private DownloadThread dowloadConfigThread;
  private ConfigReceive configReceive;
  private JarReceive jarReceive;
  private File currentConfigFile;
  private byte[] repoId;
  public static int RETRY_PERIOD = 10000;
  private Directory directory = null;
  private GlobRepository repository = null;
  private ServerAccess serverAccess;

  public ConfigService(String applicationVersion, Long jarVersion, Long localConfigVersion, File currentConfigFile) {
    this.currentConfigFile = currentConfigFile;
    Utils.beginRemove();
    RETRY_PERIOD = 500;

    LICENSE_SERVER_URL = System.getProperty(COM_APP_LICENSE_URL, PicsouApplication.LICENSE_SERVER_URL);
    MOBILE_SERVER_URL = System.getProperty(COM_APP_MOBILE_URL, PicsouApplication.MOBILE_SERVER_URL);
    FTP_SERVER_URL = System.getProperty(COM_APP_FTP_URL, PicsouApplication.FTP_SERVER_URL);

    Utils.endRemove();
    this.applicationVersion = applicationVersion;
    localJarVersion = jarVersion;
    this.localConfigVersion = localConfigVersion;
  }

  synchronized public boolean loadConfigFileFromLastestJar(Directory directory, GlobRepository repository) {
    return loadConfig(directory, repository);
  }

  // return a translated message
  synchronized public String askForNewCodeByMail(String mail) {
    HttpPost postMethod = null;
    try {
      String url = LICENSE_SERVER_URL + REQUEST_FOR_MAIL;
      HttpResponse response;
      try {
        postMethod = createPostMethod(url);
        postMethod.setHeader(HEADER_MAIL, mail);
        postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
        HttpClient httpClient = getNewHttpClient();
        response = httpClient.execute(postMethod);
      }
      catch (Exception e) {
        if (postMethod != null) {
          postMethod.releaseConnection();
        }
        postMethod = createPostMethod(url);
        postMethod.setHeader(HEADER_MAIL, mail);
        postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
        HttpClient httpClient = getNewHttpClient();
        response = httpClient.execute(postMethod);
      }
      updateConnectionStatusOk();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        Header status = response.getFirstHeader(HEADER_STATUS);
        if (status != null) {
          if (status.getValue().equalsIgnoreCase(HEADER_MAIL_SENT)) {
            return Lang.get("license.mail.sent");
          }
          if (status.getValue().equalsIgnoreCase(HEADER_MAIL_SENT_FAILED)) {
            return Lang.get("license.mail.sent.failed");
          }
          if (status.getValue().equalsIgnoreCase(HEADER_MAIL_UNKNOWN)) {
            return Lang.get("license.mail.unknown");
          }
          return Lang.get("license.mail.error");
        }
        return Lang.get("license.mail.send.error");
      }
      else {
        return Lang.get("license.mail.send.error");
      }
    }
    catch (IOException e) {
      updateConnectionStatus(e);
      return Lang.get("license.mail.send.error");
    }
    catch (Exception e) {
      updateConnectionStatus(e);
      return Lang.get("license.mail.send.error");
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
  }

  static private HttpPost createPostMethod(String url) {
    HttpPost postMethod = new HttpPost(url);
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
    return postMethod;
  }

  private HttpClient getNewHttpClient() {
    try {
      SchemeRegistry schemeRegistry = new SchemeRegistry();
      schemeRegistry.register(new Scheme("https", 443,
                                         new SSLSocketFactory(new TrustStrategy() {
                                           public boolean isTrusted(X509Certificate[] chain, String authType) {
//                                             byte[] encoded = chain[0].getPublicKey().getEncoded();
//                                             if (!Arrays.equals(encoded, expectedPublicKey)){
//                                               return false;
//                                             }
                                             return true;
                                           }
                                         },
                                                              new AbstractVerifier() {
                                                                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                                                                }
                                                              }
                                         )));
      schemeRegistry.register(new Scheme("http", 5000, new PlainSocketFactory()));
      ClientConnectionManager connectionManager = new BasicClientConnectionManager(schemeRegistry);
      HttpClient httpClient = new DefaultHttpClient(connectionManager);
      httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
      return httpClient;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  synchronized private boolean sendRequestForNewConfig(byte[] repoId, String mail, String signature,
                                                       long launchCount, String activationCode) throws IOException {
    HttpPost postMethod = null;
    try {
      this.repoId = repoId;
      String url = LICENSE_SERVER_URL + REQUEST_FOR_CONFIG;
      HttpResponse response;
      try {
        postMethod = createNewConfigPostMethod(repoId, mail, signature, launchCount, activationCode, url);
        HttpClient httpClient = getNewHttpClient();
        response = httpClient.execute(postMethod);
      }
      catch (Exception e) {
        if (postMethod != null) {
          postMethod.releaseConnection();
        }
        postMethod = createNewConfigPostMethod(repoId, mail, signature, launchCount, activationCode, url);
        HttpClient httpClient = getNewHttpClient();
        response = httpClient.execute(postMethod);
      }
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        Header configVersionHeader = response.getFirstHeader(HEADER_NEW_CONFIG_VERSION);
        if (configVersionHeader != null) {
          long newConfigVersion = Long.parseLong(configVersionHeader.getValue());
          if (localConfigVersion < newConfigVersion) {
            configReceive = new ConfigReceive(directory, repository);
            dowloadConfigThread =
              new DownloadThread(FTP_SERVER_URL, AppPaths.getBankConfigPath(),
                                 generateConfigJarName(newConfigVersion), newConfigVersion, configReceive);
            dowloadConfigThread.start();
          }
        }
        Header jarVersionHeader = response.getFirstHeader(HEADER_NEW_JAR_VERSION);
        if (jarVersionHeader != null) {
          long newJarVersion = Long.parseLong(jarVersionHeader.getValue());
          if (localJarVersion < newJarVersion) {
            jarReceive = new JarReceive(directory, repository, serverAccess);
            dowloadJarThread =
              new DownloadThread(FTP_SERVER_URL, AppPaths.getJarPath(),
                                 generatePicsouJarName(newJarVersion), newJarVersion, jarReceive);
            dowloadJarThread.start();
          }
        }
        Header validityHeader = response.getFirstHeader(HEADER_IS_VALIDE);
        if (validityHeader == null) {
          userState = userState.fireKillUser(false);
        }
        else {
          boolean validity = "true".equalsIgnoreCase(validityHeader.getValue());
          if (!validity) {
            if (checkMailSent(response)) {
              userState = userState.fireKillUser(true);
            }
            else {
              userState = userState.fireKillUser(false);
            }
          }
          else {
            userState = userState.fireValidUser();
          }
        }
        return true;
      }
      return false;
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
  }

  private HttpPost createNewConfigPostMethod(byte[] repoId, String mail, String signature, long launchCount, String activationCode, String url) {
    HttpPost postMethod = createPostMethod(url);
    postMethod.setHeader(HEADER_CONFIG_VERSION, Long.toString(localConfigVersion));
    postMethod.setHeader(HEADER_JAR_VERSION, Long.toString(localJarVersion));
    postMethod.setHeader(HEADER_APPLICATION_VERSION, applicationVersion);
    postMethod.setHeader(HEADER_REPO_ID, Encoder.byteToString(repoId));
    postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
    if (signature != null && signature.length() > 1 && mail != null && activationCode != null) {
      postMethod.setHeader(HEADER_MAIL, mail);
      postMethod.setHeader(HEADER_SIGNATURE, signature);
      postMethod.setHeader(HEADER_CODE, activationCode);
      postMethod.setHeader(HEADER_COUNT, Long.toString(launchCount));
    }
    return postMethod;
  }

  private boolean checkMailSent(HttpResponse response) {
    Header header = response.getFirstHeader(HEADER_MAIL_SENT);
    return header != null && header.getValue().equals("true");
  }


  public boolean sendMobileData(String mail, String password, byte[] bytes, Ref<String> message) {

    HttpClient client = getNewHttpClient();
    HttpPost postMethod;
    postMethod = createPostMethod(MOBILE_SERVER_URL + REQUEST_CLIENT_TO_SERVER_DATA);

    try {

      MD5PasswordBasedEncryptor encryptor =
        new MD5PasswordBasedEncryptor(ConfigService.MOBILE_SALT.getBytes(), password.toCharArray(), 5);

      byte[] data = encryptor.encrypt(bytes);
      byte[] encryptedMail = encryptor.encrypt(mail.getBytes("UTF-8"));
      String sha1Mail = Crypt.encodeSHA1AndHex(encryptedMail);
      postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
      postMethod.setHeader(HEADER_MAIL, URLEncoder.encode(mail, "UTF-8"));
      postMethod.setHeader(ComCst.CRYPTED_INFO, URLEncoder.encode(sha1Mail, "UTF-8"));
      postMethod.setHeader(ComCst.MAJOR_VERSION_NAME, Integer.toString(MobileModel.MAJOR_VERSION));
      postMethod.setHeader(ComCst.MINOR_VERSION_NAME, Integer.toString(MobileModel.MINOR_VERSION));
      postMethod.setEntity(new ByteArrayEntity(data));
      HttpResponse response = client.execute(postMethod);
      updateConnectionStatusOk();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpServletResponse.SC_FORBIDDEN) {
        Header configVersionHeader = response.getFirstHeader(ComCst.STATUS);
        message.set(configVersionHeader.getValue());
        return false;
      }
      return true;
    }
    catch (Exception e) {
      Log.write("while sending data", e);
      message.set(e.getMessage());
      updateConnectionStatus(e);
      return false;
    }
    finally {
      postMethod.releaseConnection();
    }
  }

  synchronized public void sendRegister(String mail, String code, final GlobRepository repository) {
    Utils.beginRemove();
    if (LICENSE_SERVER_URL == null || LICENSE_SERVER_URL.length() == 0) {
      return;
    }
    Utils.endRemove();
    HttpPost postMethod = null;
    HttpResponse response;
    try {
      String url = LICENSE_SERVER_URL + REQUEST_FOR_REGISTER;
      try {
        postMethod = createRegisterPostMethod(mail, code, url);
        HttpClient httpClient = getNewHttpClient();
        response = httpClient.execute(postMethod);
      }
      catch (IOException e) {
        if (postMethod != null) {
          postMethod.releaseConnection();
        }
        postMethod = createRegisterPostMethod(mail, code, url);
        HttpClient httpClient = getNewHttpClient();
        response = httpClient.execute(postMethod);
      }
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        SwingUtilities.invokeLater(new ComputeRegisterResponse(repository, response));
      }
      else {
        updateRepository(repository, User.ACTIVATION_FAILED_HTTP_REQUEST);
      }
    }
    catch (final Exception e) {
      updateConnectionStatus(e);
      updateRepository(repository, User.ACTIVATION_FAILED_CAN_NOT_CONNECT);
      // pas de stack (juste les message) risque de faciliter le piratage
      Thread thread = new Thread() {
        public void run() {
          Throwable f = e;
          while (f != null) {
            Log.write("For activation : " + f.getMessage());
            if (f != f.getCause()) {
              f = f.getCause();
            }
            else {
              f = null;
            }
          }
        }
      };
      thread.setDaemon(true);
      thread.start();
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
  }

  private HttpPost createRegisterPostMethod(String mail, String code, String url) {
    final HttpPost postMethod = createPostMethod(url);
    postMethod.setHeader(HEADER_MAIL, mail);
    postMethod.setHeader(HEADER_CODE, code);
    postMethod.setHeader(HEADER_REPO_ID, Encoder.byteToString(repoId));
    postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
    return postMethod;
  }

  private void updateRepository(final GlobRepository repository, final int cause) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        repository.update(User.KEY, User.ACTIVATION_STATE, cause);
      }
    });
  }

  private void computeResponse(GlobRepository repository, HttpResponse response) {
    repository.startChangeSet();
    try {
      Header header = response.getFirstHeader(HEADER_MAIL_UNKNOWN);
      if (header != null && "true".equalsIgnoreCase(header.getValue())) {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_MAIL_UNKNOWN);
      }
      else {
        Header signature = response.getFirstHeader(HEADER_SIGNATURE);
        if (signature != null) {
          String value = signature.getValue();
          repository.update(User.KEY, User.SIGNATURE, Encoder.stringToByte(value));
        }
        else {
          Header isMailSentHeader = response.getFirstHeader(HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT);
          if (isMailSentHeader != null && "true".equalsIgnoreCase(isMailSentHeader.getValue())) {
            repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_MAIL_SENT);
          }
          else {
            repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_MAIL_NOT_SENT);
          }
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void sendUsageData(String msg) throws IOException {
    String url = LICENSE_SERVER_URL + SEND_USE_INFO;

    HttpPost postMethod = createPostMethod(url);
    postMethod.setHeader(HEADER_USE_INFO, msg);
    HttpClient httpClient = getNewHttpClient();
    httpClient.execute(postMethod);
  }

  public boolean createMobileAccount(String mail, String password, Ref<String> message) {
    HttpPost postMethod = null;
    try {
      postMethod = createPostMessage(mail, password, MOBILE_SERVER_URL + ComCst.SEND_MAIL_TO_CONFIRM_MOBILE);
      HttpClient httpClient = getNewHttpClient();
      HttpResponse response = httpClient.execute(postMethod);
      updateConnectionStatusOk();
      if (response.getStatusLine().getStatusCode() != 200) {
        message.set(Lang.get("mobile.user.connection.failed"));
        return false;
      }
      Header isValid = response.getFirstHeader(ConfigService.HEADER_IS_VALIDE);
      if (isValid != null && isValid.getValue().equalsIgnoreCase("true")) {
        message.set(Lang.get("mobile.user.create.mail.sent"));
        return true;
      }
      message.set(Lang.get("mobile.user.create.already.exist"));
      return false;
    }
    catch (Exception e) {
      Log.write("error", e);
      updateConnectionStatus(e);
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    message.set(Lang.get("mobile.user.connection.failed"));
    return false;
  }

  private HttpPost createPostMessage(String mail, String password, final String url) throws UnsupportedEncodingException {

    MD5PasswordBasedEncryptor encryptor =
      new MD5PasswordBasedEncryptor(ConfigService.MOBILE_SALT.getBytes(), ConfigService.SOME_PASSWORD.toCharArray(), 5);

    byte[] localKey = Base64.encodeBase64(encryptor.encrypt(mail.getBytes("UTF-8")));

    MD5PasswordBasedEncryptor userEncryptor =
      new MD5PasswordBasedEncryptor(ConfigService.MOBILE_SALT.getBytes(), password.toCharArray(), 5);
    byte[] encryptedMail = userEncryptor.encrypt(mail.getBytes("UTF-8"));
    String sha1Mail = Crypt.encodeSHA1AndHex(encryptedMail);

    HttpPost postMethod = createPostMethod(url);
    postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
    postMethod.setHeader(HEADER_MAIL, URLEncoder.encode(mail, "UTF-8"));
    postMethod.setHeader(CODING, URLEncoder.encode(new String(localKey), "UTF-8"));
    postMethod.setHeader(ComCst.CRYPTED_INFO, URLEncoder.encode(sha1Mail, "UTF-8"));
    return postMethod;
  }

  public boolean deleteMobileAccount(String mail, String password, Ref<String> message) {
    HttpPost postMethod = null;
    try {
      postMethod = createPostMessage(mail, password, MOBILE_SERVER_URL + ComCst.DELETE_MOBILE_ACCOUNT);
      HttpClient httpClient = getNewHttpClient();
      HttpResponse response = httpClient.execute(postMethod);
      updateConnectionStatusOk();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpServletResponse.SC_FORBIDDEN){
        message.set(Lang.get("mobile.user.delete.invalid.password"));
        return false;
      }
      if (statusCode != 200) {
        message.set(Lang.get("mobile.user.connection.failed"));
        return false;
      }
      return true;
    }
    catch (Exception e) {
      Log.write("error", e);
      updateConnectionStatus(e);
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    message.set(Lang.get("mobile.user.connection.failed"));
    return false;
  }

  public interface Listener {
    void sent(String mail, String title, String content);

    void sendFailed(String mail, String title, String content);
  }

  synchronized public void sendMail(final String toMail, final String fromMail,
                                    final String title, final String content, final Listener listener) {
    Thread thread = new SendMailThread(fromMail, toMail, title, content, listener);
    thread.setDaemon(true);
    thread.start();
  }

  private void updateConnectionStatus(Exception e) {
    if (e instanceof IOException) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          repository.update(User.KEY, User.CONNECTED, false);
        }
      });
    }
  }

  private void updateConnectionStatusOk() {
    if (!repository.get(User.KEY).get(User.CONNECTED)){
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          repository.update(User.KEY, User.CONNECTED, true);
        }
      });
    }
  }


  synchronized public boolean update(final byte[] repoId, final long launchCount, byte[] mailInBytes,
                                     byte[] signatureInByte, final String activationCode,
                                     ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
    boolean isValideUser;
    final String mail = mailInBytes == null ? null : new String(mailInBytes);
    if (signatureInByte != null && activationCode != null) {
      if (KeyService.checkSignature(mailInBytes, signatureInByte)) {
        userState = UserStateFactory.localValidSignature(mail);
        isValideUser = true;
      }
      else {
        userState = UserStateFactory.localInvalidSignature(mail);
        isValideUser = false;
      }
    }
    else {
      userState = UserStateFactory.noSignature(mail);
      isValideUser = false;
    }
    final String signature = signatureInByte == null ? null : Encoder.byteToString(signatureInByte);
    if (LICENSE_SERVER_URL != null && LICENSE_SERVER_URL.length() != 0) {
      // le thread est inliné pour eviter de copier (donc de rendre visible) les variables (repoId, ...)
      // dans des donnée membres
      Thread request = new Thread() {
        {
          setDaemon(true);
        }

        public void run() {
          if (LICENSE_SERVER_URL == null) {
            return;
          }
          boolean connectionEstablished = false;
          while (!connectionEstablished) {
            try {
              connectionEstablished =
                sendRequestForNewConfig(repoId, mail, signature, launchCount, activationCode);
            }
            catch (Exception ex) {
            }
            if (!connectionEstablished) {
              try {
                Thread.sleep(RETRY_PERIOD);
              }
              catch (InterruptedException e) {
              }
            }
          }
          synchronized (ConfigService.this) {
            ConfigService.this.notify();
          }
        }
      };
      request.start();
    }
    return isValideUser;
  }

  @Inline
  public static void check(Directory directory, GlobRepository repository) {
    ConfigService configService = directory.get(ConfigService.class);
    configService.updateUserValidity(directory, repository);
  }

  synchronized private void updateUserValidity(Directory directory, GlobRepository repository) {
    userState = userState.updateUserValidity(directory, repository);
  }

  public boolean loadConfig(Directory directory, GlobRepository repository) {
    boolean configLoaded = false;
    if (configReceive != null) {
      configLoaded = configReceive.set(directory, repository);
    }
    if (!configLoaded && currentConfigFile != null) {
      synchronized (this) {
        configLoaded = loadConfigFile(currentConfigFile, localConfigVersion, repository, directory);
      }
    }
    if (jarReceive != null) {
      jarReceive.set(directory, repository);
    }
    synchronized (this) {
      this.directory = directory;
      this.repository = repository;
    }
    return configLoaded;
  }

  public Boolean isVerifiedServerValidity() {
    Utils.beginRemove();
    if (LICENSE_SERVER_URL == null || LICENSE_SERVER_URL.length() == 0) {
      userState = new CompletedUserState("local");
      return true;
    }
    Utils.endRemove();
    return userState.isVerifiedServerValidity();
  }

  @Inline
  public static boolean waitEndOfConfigRequest(Directory directory) {
    ConfigService configService = directory.get(ConfigService.class);
    synchronized (configService) {
      while (!configService.isVerifiedServerValidity() && !Thread.currentThread().isInterrupted()) {
        try {
          configService.wait(200);
        }
        catch (InterruptedException e) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isMailSend() {
    return mailSend;
  }

  static public String generatePicsouJarName(long newVersion) {
    String name = Long.toString(newVersion);
    return PicsouApplication.APPNAME + name + ".jar";
  }

  static public String generateConfigJarName(long newVersion) {
    String name = Long.toString(newVersion);
    return "config" + name + ".jar";
  }

  private class ConfigReceive extends AbstractJarReceived {

    // directory/repository can be null
    public ConfigReceive(Directory directory, GlobRepository repository) {
      set(directory, repository);
    }

    protected void loadJar(File jarFile, long version) {
      loadConfigFile(jarFile, version, repository, directory);
    }
  }

  static class SpecificBankLoader extends ClassLoader {
    public void load(GlobRepository globRepository, Directory directory, InputStream stream, String name) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte tab[] = new byte[1024];
      try {
        int len = stream.read(tab);
        while (len != -1) {
          outputStream.write(tab, 0, len);
          len = stream.read(tab);
        }
        byte[] def = outputStream.toByteArray();
        Class<?> specificClass = this.defineClass(name, def, 0, def.length);
        Constructor<?> constructor = specificClass.getConstructor(GlobRepository.class, Directory.class);
        constructor.newInstance(globRepository, directory);
      }
      catch (IOException e) {
        Log.write("failed to read " + name, e);
      }
      catch (Exception e) {
        Log.write("failed to load class " + name, e);
      }
    }
  }

  private boolean loadConfigFile(File jarFile, long version, final GlobRepository repository, final Directory directory) {
    try {
      final SpecificBankLoader bankLoader = new SpecificBankLoader();
      final JarFile jar = new JarFile(jarFile);
      TransactionAnalyzerFactory.Loader loader = new TransactionAnalyzerFactory.Loader() {
        public InputStream load(String file) {
          ZipEntry zipEntry = jar.getEntry(file);
          try {
            return jar.getInputStream(zipEntry);
          }
          catch (IOException e) {
            return null;
          }
        }

        public void loadBank(BankPluginService bankPluginService) {
          Enumeration<JarEntry> jarEntryEnumeration = jar.entries();
          while (jarEntryEnumeration.hasMoreElements()) {
            JarEntry entry = jarEntryEnumeration.nextElement();
            String className = entry.getName();
            if (!entry.isDirectory() && className.endsWith(".class")) {
              try {
                InputStream inputStream = jar.getInputStream(entry);
                bankLoader.load(repository, directory, inputStream,
                                className.substring(0, className.length() - ".class".length())
                                  .replace("/", "."));
              }
              catch (IOException e) {
                Log.write("Failed to get entry for " + className, e);
              }
            }
          }
        }
      };
      directory.get(TransactionAnalyzerFactory.class).load(loader, version, repository, directory);
      repository.update(AppVersionInformation.KEY, AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, version);
      return true;
    }
    catch (Exception e) {
      Log.write("", e);
      return false;
    }
  }

  private static class JarReceive extends AbstractJarReceived {
    private ServerAccess serverAccess;

    public JarReceive(Directory directory, GlobRepository repository, ServerAccess serverAccess) {
      this.serverAccess = serverAccess;
      set(directory, repository);
    }

    synchronized public void complete(File jarFile, long version) {
      serverAccess.downloadedVersion(version);
      super.complete(jarFile, version);
    }

    protected void loadJar(File jarFile, long version) {
      repository.update(AppVersionInformation.KEY, AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, version);
    }
  }

  static class UserStateFactory {
    static UserState localValidSignature(String mail) {
      return new LocallyValidUser(mail);
    }

    static UserState localInvalidSignature(String mail) {
      return new LocallyInvalidUser(mail);
    }

    static UserState noSignature(String mail) {
      return new AnonymousUser(mail);
    }
  }

  public static String encodeContent(String content) {
    try {
      return URLEncoder.encode(content, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String decodeContent(String content) {
    try {
      return URLDecoder.decode(content, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private class SendMailThread extends Thread {
    private final String fromMail;
    private final String toMail;
    private final String title;
    private final String content;
    private final Listener listener;

    public SendMailThread(String fromMail, String toMail, String title, String content, Listener listener) {
      this.fromMail = fromMail;
      this.toMail = toMail;
      this.title = title;
      this.content = content;
      this.listener = listener;
    }

    public void run() {
      String url = LICENSE_SERVER_URL + REQUEST_SEND_MAIL;
      HttpPost postMethod = createPost(url);
      HttpResponse response;
      try {
        try {
          HttpClient httpClient = getNewHttpClient();
          response = httpClient.execute(postMethod);
        }
        catch (IOException e) {
          Log.write("connection error (retrying): ", e);
          postMethod.releaseConnection();
          HttpClient httpClient = getNewHttpClient();
          postMethod = createPost(url);
          response = httpClient.execute(postMethod);
        }
        updateConnectionStatusOk();
        Log.write("Send mail ok");
      }
      catch (final Exception e) {
        Log.write("in send mail", e);
        updateConnectionStatus(e);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("mail not sent", e);
            listener.sendFailed(fromMail, title, content);
          }
        });
        return;
      }
      finally {
        postMethod.releaseConnection();
      }
      final int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            listener.sent(fromMail, title, content);
          }
        });
      }
      else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("Mail not sent with error code " + statusCode);
            listener.sendFailed(fromMail, title, content);
          }
        });
      }
    }

    private HttpPost createPost(String url) {
      HttpPost postMethod = createPostMethod(url);
      postMethod.setHeader(ComCst.HEADER_LANG, Lang.get("lang"));
      postMethod.setHeader(HEADER_MAIL, fromMail);
      postMethod.setHeader(HEADER_TO_MAIL, toMail);
      postMethod.setHeader(HEADER_MAIL_TITLE, title);
      try {
        postMethod.setEntity(new StringEntity(content, "UTF-8"));
      }
      catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      return postMethod;
    }
  }

  private class ComputeRegisterResponse implements Runnable {
    private final GlobRepository repository;
    private HttpResponse response;

    public ComputeRegisterResponse(GlobRepository repository, HttpResponse response) {
      this.repository = repository;
      this.response = response;
    }

    public void run() {
      computeResponse(repository, response);
    }
  }

  public void setLang(String lang) {
    serverAccess.setLang(lang);
  }
}
