package org.designup.picsou.gui.config;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.client.exceptions.BadConnection;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.utils.KeyChecker;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.serialization.Encoder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Comparator;

public class ConfigService {
  private String URL = null;
  public static final String HEADER_MAIL = "mail";
  public static final String HEADER_SIGNATURE = "signature";
  public static final String HEADER_IS_VALIDE = "isValide";
  public static final String HEADER_CODE = "code";
  public static final String HEADER_COUNT = "count";
  public static final String HEADER_MAIL_SENT = "mailSent";
  public static final String HEADER_MAIL_UNKNOWN = "mailUnknown";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE = "activationCodeNotValide";
  private HttpClient httpClient;
  private File pathToConfig;
  private File configFile;
  private long localVersion = -1;
  private int newVersion = -1;
  private Integer applicationVersion;
  private Directory directory;
  static private Long launchCount;
  static private String mail;
  static private String signature;
  private static boolean isV1;
  private boolean isValide;
  private static final String REGISTER = "/register";
  private static final String REQUEST_FOR_CONFIG = "/requestForConfig";

  public ConfigService(Integer applicationversion, Directory directory) {
    URL = System.getProperty("com.picsou.licence.url");
    applicationVersion = applicationversion;
    this.directory = directory;
    Protocol easyhttps = new Protocol("https", new HttpsClientTransport.EasySSLProtocolSocketFactory(), 8443);
    Protocol.registerProtocol("https", easyhttps);
    this.httpClient = new HttpClient();
    String path = PicsouApplication.getLocalPrevaylerPath();
    pathToConfig = new File(path + "/config");
    File[] files = pathToConfig.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return !pathname.isDirectory() && pathname.getName().endsWith(".jar");
      }
    });
    if (files == null) {
      startCheckForNewVersion();
      return;
    }

    Arrays.sort(files, new Comparator<File>() {
      public int compare(File f1, File f2) {
        String s1 = f1.getName();
        String s2 = f2.getName();
        return new Long(Long.parseLong(s1.substring(0, s1.indexOf("."))))
          .compareTo(Long.parseLong(s2.substring(0, s2.indexOf("."))));
      }
    });
    if (files.length >= 1) {
      configFile = files[files.length - 1];
      this.localVersion = Long.parseLong(configFile.getName().substring(0, configFile.getName().indexOf(".")));
    }

    startCheckForNewVersion();
  }

  private void startCheckForNewVersion() {
    Thread thread = new ConfigRequest();
    thread.start();
  }

  public void loadConfig(GlobRepository repository) {
    synchronized (this) {
      notify();
    }
  }

  private void sendRequestForNewConfig(String url) {
    url += REQUEST_FOR_CONFIG;
    boolean hasError = true;
    try {
      Log.enter("send request " + url);
      PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader("configVersion", Long.toString(localVersion));
      postMethod.setRequestHeader("applicationVersion", Integer.toString(applicationVersion));
      httpClient.executeMethod(postMethod);
      Header versionHeader = postMethod.getRequestHeader("newVersion");
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 400 && versionHeader != null) {
        newVersion = Integer.parseInt(versionHeader.getValue());
        InputStream responseBodyAsStream = postMethod.getResponseBodyAsStream();
        String fileName = "0000000000000000000" + newVersion;
        configFile = new File(pathToConfig, fileName.substring(fileName.length() - 19) + ".jar");
        Files.copyStreamTofile(responseBodyAsStream, configFile.getAbsolutePath());
      }
      Log.leave("send Ok");
      hasError = false;
    }
    catch (IOException e) {
      Log.write("ex : ", e);
      throw new BadConnection(e);
    }
    finally {
      if (hasError) {
        Log.leave("send with Error");
      }
    }
  }

  private void loadNewConfig() {
    if (newVersion == localVersion || configFile == null) {
      return;
    }
    URL url;
    try {
      url = new URL("file", "", 0, configFile.getAbsolutePath());
    }
    catch (MalformedURLException e) {
      throw new InvalidFormat(e);
    }
    URLClassLoader loader = new URLClassLoader(new URL[]{url});
    TransactionAnalyzerFactory analyzerFactory = directory.get(TransactionAnalyzerFactory.class);
    analyzerFactory.load(loader);
  }

  private boolean sendRequestForLicence(String url) {
    long endDate = System.currentTimeMillis() + 1000;
    synchronized (this) {
      while (launchCount == null) {
        try {
          long still = endDate - System.currentTimeMillis();
          if (still > 0) {
            wait(System.currentTimeMillis());
          }
          else {
            return false;
          }
        }
        catch (InterruptedException e) {
          return false;
        }
      }
    }
    return sendRequestToServer(url);
  }

  private boolean sendRequestToServer(String url) {
    try {
      url += "/requestForLicence";
      PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_MAIL, mail);
      postMethod.setRequestHeader(HEADER_CODE, signature);
      postMethod.setRequestHeader(HEADER_COUNT, launchCount.toString());
      httpClient.executeMethod(postMethod);
      Header validity = postMethod.getRequestHeader(HEADER_IS_VALIDE);
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 400 && validity != null) {
        return Boolean.parseBoolean(validity.getValue());
      }
    }
    catch (IOException e) {
      return true;
    }
    return true;
  }

  public static void register(final Directory directory, GlobRepository repository) {
    repository.addChangeListener(new RegistrationListener(directory));
  }

  public void sendRegister(String mail, String code, GlobRepository repository) {
    if (URL == null) {
      return;
    }
    try {
      String url = URL + REGISTER;
      PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_MAIL, mail);
      postMethod.setRequestHeader(HEADER_CODE, code);
      httpClient.executeMethod(postMethod);
      Header header = postMethod.getRequestHeader(HEADER_MAIL_UNKNOWN);
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 200) {
        Header signature = postMethod.getResponseHeader(HEADER_SIGNATURE);
        if (signature != null) {
          String value = signature.getValue();
          repository.update(User.KEY, User.SIGNATURE, Encoder.b64Encode(value));
        }
      }
      else {
        System.err.println("error : " + statusCode);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void set(long launchCount, byte[] mail, byte[] signature) {
    if (mail.length == 0 && signature.length == 0) {
      return;
    }
    isV1 = KeyChecker.checkSignature(mail, signature);
    ConfigService.mail = new String(mail);
    ConfigService.signature = Encoder.b64Decode(signature);
    ConfigService.launchCount = launchCount;
  }

  private class ConfigRequest extends Thread {

    public void run() {
      if (URL == null) {
        return;
      }
      sendRequestForNewConfig(URL);
      loadNewConfig();
      isValide = sendRequestForLicence(URL);
    }
  }
}
