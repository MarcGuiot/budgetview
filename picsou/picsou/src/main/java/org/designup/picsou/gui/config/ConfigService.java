package org.designup.picsou.gui.config;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.utils.KeyService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.ServerInformation;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Inline;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class ConfigService {
  private static final Pattern FILTER = Pattern.compile(PicsouApplication.PICSOU + "[0-9][0-9]*" + "\\.jar");


  public static final String COM_PICSOU_LICENCE_URL = "com.picsou.licence.url";
  public static final String COM_PICSOU_LICENCE_FTP_URL = "com.picsou.licence.ftp.url";
  public static final String HEADER_MAIL = "mail";
  public static final String HEADER_SIGNATURE = "signature";
  public static final String HEADER_IS_VALIDE = "isValide";
  public static final String HEADER_CODE = "code";
  public static final String HEADER_COUNT = "count";
  public static final String HEADER_MAIL_SENT = "mailSent";
  public static final String HEADER_MAIL_UNKNOWN = "mailUnknown";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT = "activationCodeNotValideMailNotSent";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT = "activationCodeNotValideMailSent";
  public static final String HEADER_APPLICATION_VERSION = "applicationVersion";
  public static final String HEADER_CONFIG_VERSION = "configVersion";
  public static final String HEADER_NEW_CONFIG_VERSION = "newConfigVersion";
  public static final String HEADER_JAR_VERSION = "jarVersion";
  public static final String HEADER_NEW_JAR_VERSION = "newJarVersion";
  public static final String HEADER_REPO_ID = "repoId";
  public static final String REGISTER_SERVLET = "register";
  private static final String REGISTER = "/" + REGISTER_SERVLET;
  private static final String REQUEST_FOR_CONFIG = "/requestForConfig";

  private String URL = PicsouApplication.REGISTER_URL;
  private String FTP_URL = PicsouApplication.FTP_URL;
  private HttpClient httpClient;
  private long localJarVersion = -1;
  private long localConfigVersion = -1;
  private Long applicationVersion;
  private Long launchCount;
  private String mail;
  private String signature;
  private byte[] repoId;
  private String activationCode;
  private boolean isValideSignature = true;
  private Boolean serverVerifiedValidity = null;
  private boolean mailSend = false;
  private DownloadThread dowloadJarThread;
  private DownloadThread dowloadConfigThread;
  private ConfigReceive configReceive;
  private JarReceive jarReceive;
  private File pathToconfigFileToLoad;

  public ConfigService(Long applicationVersion, Long jarVersion, Long localConfigVersion,
                       File pathToconfigFileToLoad) {
    this.pathToconfigFileToLoad = pathToconfigFileToLoad;
    Utils.beginRemove();
    URL = System.getProperty(COM_PICSOU_LICENCE_URL);
    FTP_URL = System.getProperty(COM_PICSOU_LICENCE_FTP_URL);
    Utils.endRemove();
    this.applicationVersion = applicationVersion;
    localJarVersion = jarVersion;
    this.localConfigVersion = localConfigVersion;
    Protocol easyhttps = new Protocol("https", new HttpsClientTransport.EasySSLProtocolSocketFactory(), 443);
    Protocol.registerProtocol("https", easyhttps);
    this.httpClient = new HttpClient();
  }

  public boolean loadConfigFileFromLastestJar(Directory directory, GlobRepository repository) {
    return loadConfig(directory, repository);
  }

  private Boolean sendRequestForNewConfig() {
    String url = URL + REQUEST_FOR_CONFIG;
    try {
      PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_CONFIG_VERSION, Long.toString(localConfigVersion));
      postMethod.setRequestHeader(HEADER_JAR_VERSION, Long.toString(localJarVersion));
      postMethod.setRequestHeader(HEADER_APPLICATION_VERSION, Long.toString(applicationVersion));
      postMethod.setRequestHeader(HEADER_REPO_ID, Encoder.byteToString(repoId));
      if (signature != null && signature.length() > 1 && mail != null && activationCode != null) {
        postMethod.setRequestHeader(HEADER_MAIL, mail);
        postMethod.setRequestHeader(HEADER_SIGNATURE, signature);
        postMethod.setRequestHeader(HEADER_CODE, activationCode);
        postMethod.setRequestHeader(HEADER_COUNT, launchCount.toString());
      }
      httpClient.executeMethod(postMethod);
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 200) {
        Header configVersionHeader = postMethod.getResponseHeader(HEADER_NEW_CONFIG_VERSION);
        if (configVersionHeader != null) {
          long newConfigVersion = Long.parseLong(configVersionHeader.getValue());
          if (localConfigVersion < newConfigVersion) {
            configReceive = new ConfigReceive();
            dowloadConfigThread =
              new DownloadThread(FTP_URL, PicsouApplication.getPicsouConfigPath(),
                                 generateConfigJarName(newConfigVersion), newConfigVersion, configReceive);
            dowloadConfigThread.start();
          }
        }
        Header jarVersionHeader = postMethod.getResponseHeader(HEADER_NEW_JAR_VERSION);
        if (jarVersionHeader != null) {
          long newJarVersion = Long.parseLong(jarVersionHeader.getValue());
          if (localJarVersion < newJarVersion) {
            jarReceive = new JarReceive();
            dowloadJarThread =
              new DownloadThread(FTP_URL, PicsouApplication.getPicsouJarPath(),
                                 generatePicsouJarName(newJarVersion), newJarVersion, jarReceive);
            dowloadJarThread.start();
          }
        }
        Header validityHeader = postMethod.getResponseHeader(HEADER_IS_VALIDE);
        if (validityHeader == null) {
          return null;
        }
        boolean validity = "true".equalsIgnoreCase(validityHeader.getValue());
        if (!validity) {
          if (checkMailSent(postMethod)) {
            mailSend = true;
          }
          return false;
        }
      }
      return true;
    }
    catch (IOException e) {
      return true;
    }
  }

  private boolean checkMailSent(PostMethod postMethod) {
    Header header = postMethod.getResponseHeader(HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT);
    return header != null && header.getValue().equals("true");
  }

  public void sendRegister(String mail, String code, final GlobRepository repository) {
    Utils.beginRemove();
    if (URL == null || URL.length() == 0) {
      return;
    }
    Utils.endRemove();
    try {
      String url = URL + REGISTER;
      final PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_MAIL, mail);
      postMethod.setRequestHeader(HEADER_CODE, code);
      postMethod.setRequestHeader(HEADER_REPO_ID, Encoder.byteToString(repoId));
      httpClient.executeMethod(postMethod);
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 200) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            computeResponse(repository, postMethod);
          }
        });
      }
      else {
        updateRepository(repository, User.ACTIVATION_FAIL_HTTP_REQUEST);
      }
    }
    catch (final Exception e) {
      updateRepository(repository, User.ACTIVATION_FAIL_CAN_NOT_CONNECT);
      // pas de stack risque de faciliter le piratage
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
      thread.run();
    }
  }

  private void updateRepository(final GlobRepository repository, final int cause) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        repository.update(User.KEY, User.ACTIVATION_STATE, cause);
      }
    });
  }

  private void computeResponse(GlobRepository repository, PostMethod postMethod) {
    repository.enterBulkDispatchingMode();
    try {
      Header header = postMethod.getRequestHeader(HEADER_MAIL_UNKNOWN);
      if (header != null && "true".equalsIgnoreCase(header.getValue())) {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAIL_MAIL_UNKNOWN);
      }
      else {
        Header signature = postMethod.getResponseHeader(HEADER_SIGNATURE);
        if (signature != null) {
          String value = signature.getValue();
          repository.update(UserPreferences.key, UserPreferences.REGISTRED_USER, true);
          repository.update(User.KEY, User.SIGNATURE, Encoder.stringToByte(value));
        }
        else {
          repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAIL_BAD_SIGNATURE);
        }
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public void update(byte[] repoId, long launchCount, byte[] mail, byte[] signature, String activationCode) {
    this.repoId = repoId;
    if (mail != null && mail.length != 0 && signature != null && signature.length != 0
        && activationCode != null) {
      isValideSignature = KeyService.checkSignature(mail, signature);
      this.mail = new String(mail);
      this.signature = Encoder.byteToString(signature);
      this.launchCount = launchCount;
      this.activationCode = activationCode;
    }
    if (URL != null && URL.length() != 0) {
      ConfigRequest request = new ConfigRequest();
      request.start();
    }
  }

  @Inline
  public static void check(Directory directory, GlobRepository repository) {
    ConfigService configService = directory.get(ConfigService.class);
    if (repository.get(UserPreferences.key).get(UserPreferences.REGISTRED_USER)) {
      if (configService.serverVerifiedValidity() == null) {
        repository.update(UserPreferences.key, UserPreferences.FUTURE_MONTH_COUNT,
                          UserPreferences.VISIBLE_MONTH_COUNT_FOR_ANONYMOUS);
        repository.update(User.KEY, User.ACTIVATION_STATE,
                          User.ACTIVATED_AS_ANONYMOUS_BUT_REGISTERED_USER);
      }
    }
    if (!configService.isValideSignature() ||
        (configService.serverVerifiedValidity() != null && !configService.serverVerifiedValidity())) {

      repository.update(UserPreferences.key, UserPreferences.REGISTRED_USER, false);
      repository.update(UserPreferences.key, UserPreferences.FUTURE_MONTH_COUNT,
                        UserPreferences.VISIBLE_MONTH_COUNT_FOR_ANONYMOUS);
    }
    if (configService.isMailSend()) {
      repository.update(ServerInformation.KEY, ServerInformation.MAIL_SEND, true);
    }
  }

  public boolean loadConfig(Directory directory, GlobRepository repository) {
    boolean configLoaded = false;
    if (configReceive != null) {
      configLoaded = configReceive.set(directory, repository);
    }
    if (!configLoaded && pathToconfigFileToLoad != null) {
      configLoaded = loadConfigFile(pathToconfigFileToLoad, localConfigVersion, directory, repository);
    }
    if (jarReceive != null) {
      jarReceive.set(directory, repository);
    }
    return configLoaded;
  }

  public boolean isValideSignature() {
    return isValideSignature;
  }

  public Boolean serverVerifiedValidity() {
    if (URL == null || URL.length() == 0) {
      return false;
    }
    return serverVerifiedValidity;
  }

  @Inline
  public static void waitEndOfConfigRequest(Directory directory) {
    ConfigService configService = directory.get(ConfigService.class);
    long end = System.currentTimeMillis() + 10000;
    synchronized (configService) {
      while (configService.serverVerifiedValidity() == null && System.currentTimeMillis() < end) {
        try {
          configService.wait(2000);
        }
        catch (InterruptedException e) {
        }
      }
    }
  }

  public boolean isMailSend() {
    return mailSend;
  }

  private class ConfigRequest extends Thread {
    private ConfigRequest() {
      setDaemon(true);
    }

    public void run() {
      if (URL == null) {
        return;
      }
      serverVerifiedValidity = sendRequestForNewConfig();
      synchronized (ConfigService.this) {
        ConfigService.this.notify();
      }
    }
  }

  static public String generatePicsouJarName(long newVersion) {
    String name = "0000000000000000000" + newVersion;
    return PicsouApplication.PICSOU + name.substring(name.length() - 19) + ".jar";
  }

  static public String generateConfigJarName(long newVersion) {
    String name = "0000000000000000000" + newVersion;
    return "config" + name.substring(name.length() - 19) + ".jar";
  }

  private class ConfigReceive extends AbstractJarReceived {

    protected void loadJar(File jarFile, long version) {
      loadConfigFile(jarFile, version, directory, repository);
    }
  }

  private boolean loadConfigFile(File jarFile, long version, Directory directory, GlobRepository repository) {
    URL url;
    try {
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
      };
      directory.get(TransactionAnalyzerFactory.class).load(loader, version);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  private static class JarReceive extends AbstractJarReceived {

    protected void loadJar(File jarFile, long version) {
      repository.update(ServerInformation.KEY, ServerInformation.LATEST_SOFTWARE_VERSION, version);
    }
  }
}
