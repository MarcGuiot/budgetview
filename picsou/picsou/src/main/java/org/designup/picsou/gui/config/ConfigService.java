package org.designup.picsou.gui.config;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.utils.KeyChecker;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
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
  public static final String HEADER_CONFIG_VERSION = "configVersion";
  public static final String HEADER_APPLICATION_VERSION = "applicationVersion";
  public static final String HEADER_NEW_VERSION = "newVersion";
  public static final String HEADER_REPO_ID = "repoId";
  private HttpClient httpClient;
  private File pathToConfig;
  private File configFile;
  private long localVersion = -1;
  private int newVersion = -1;
  private Integer applicationVersion;
  private Directory directory;
  private Long launchCount;
  private String mail;
  private String signature;
  private byte[] repoId;
  private String activationCode;
  private boolean isValideSignature;
  private boolean isStillValide;
  public static final String REGISTER_SERVLET = "register";
  private static final String REGISTER = "/" + REGISTER_SERVLET;
  private static final String REQUEST_FOR_CONFIG = "/requestForConfig";
  private static final String REQUEST_SERVLET = "requestForLicence";
  private static final String REQUEST_FOR_LICENCE = "/" + REQUEST_SERVLET;

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

  }

  public void loadConfig(GlobRepository repository) {
    synchronized (this) {
      notify();
    }
  }

  private boolean sendRequestForNewConfig(String url) {
    url += REQUEST_FOR_CONFIG;
    try {
      PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_CONFIG_VERSION, Long.toString(localVersion));
      postMethod.setRequestHeader(HEADER_APPLICATION_VERSION, Integer.toString(applicationVersion));
      postMethod.setRequestHeader(HEADER_REPO_ID, repoId.toString());
      if (signature != null && signature.length() > 1) {
        postMethod.setRequestHeader(HEADER_MAIL, mail);
        postMethod.setRequestHeader(HEADER_SIGNATURE, signature);
        postMethod.setRequestHeader(HEADER_CODE, activationCode);
        postMethod.setRequestHeader(HEADER_COUNT, launchCount.toString());
      }
      httpClient.executeMethod(postMethod);
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 200) {
        Header versionHeader = postMethod.getRequestHeader(HEADER_NEW_VERSION);
        if (versionHeader != null) {
          newVersion = Integer.parseInt(versionHeader.getValue());
          InputStream responseBodyAsStream = postMethod.getResponseBodyAsStream();
          String fileName = "0000000000000000000" + newVersion;
          configFile = new File(pathToConfig, fileName.substring(fileName.length() - 19) + ".jar");
          Files.copyStreamTofile(responseBodyAsStream, configFile.getAbsolutePath());
        }
        Header validity = postMethod.getRequestHeader(HEADER_IS_VALIDE);
        if (validity != null) {
          return "true".equalsIgnoreCase(validity.getValue());
        }
      }
      return true;
    }
    catch (IOException e) {
      return true;
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

  public void update(byte[] repoId, long launchCount, byte[] mail, byte[] signature, String activationCode) {
    this.repoId = repoId;
    if (mail.length != 0 && signature.length != 0) {
      isValideSignature = KeyChecker.checkSignature(mail, signature);
      this.mail = new String(mail);
      this.signature = Encoder.b64Decode(signature);
      this.launchCount = launchCount;
      this.activationCode = activationCode;
    }
    ConfigRequest request = new ConfigRequest();
    request.start();
  }

  private class ConfigRequest extends Thread {
    private ConfigRequest() {
      setDaemon(true);
    }

    public void run() {
      if (URL == null) {
        return;
      }
      isStillValide = sendRequestForNewConfig(URL);
      loadNewConfig();
    }
  }
}
