package org.designup.picsou.gui.config;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.utils.KeyService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.User;
import org.designup.picsou.model.VersionInformation;
import org.designup.picsou.utils.Inline;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class ConfigService {
  private static final Pattern FILTER = Pattern.compile(PicsouApplication.APPNAME + "[0-9][0-9]*" + "\\.jar");

  public static final String COM_APP_LICENSE_URL = PicsouApplication.APPNAME + ".license.url";
  public static final String COM_APP_LICENSE_FTP_URL = PicsouApplication.APPNAME + ".license.ftp.url";
  public static final String HEADER_MAIL = "mail";
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
  public static final String HEADER_LANG = "lang";
  public static final String REQUEST_FOR_REGISTER = "/register";
  public static final String REQUEST_FOR_CONFIG = "/requestForConfig";
  public static final String REQUEST_FOR_MAIL = "/mailTo";

  private String URL = PicsouApplication.REGISTER_URL;
  private String FTP_URL = PicsouApplication.FTP_URL;
  private HttpClient httpClient;
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
  public static final String HEADER_BAD_ADRESS = "badAdress";

  public ConfigService(String applicationVersion, Long jarVersion, Long localConfigVersion, File currentConfigFile) {
    this.currentConfigFile = currentConfigFile;
    Utils.beginRemove();
    URL = System.getProperty(COM_APP_LICENSE_URL);
    FTP_URL = System.getProperty(COM_APP_LICENSE_FTP_URL);
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


  // return a translated message
  public String askForNewCodeByMail(String mail) {
    try {
      String url = URL + REQUEST_FOR_MAIL;
      final PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_MAIL, mail);
      postMethod.setRequestHeader(HEADER_LANG, Lang.get("lang"));
      httpClient.executeMethod(postMethod);
      int statusCode = postMethod.getStatusCode();
      if (statusCode == 200) {
        Header status = postMethod.getResponseHeader(HEADER_STATUS);
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
      return Lang.get("license.mail.send.error");
    }
  }


  private boolean sendRequestForNewConfig(byte[] repoId, String mail, String signature,
                                          long launchCount, String activationCode) throws IOException {
    this.repoId = repoId;
    String url = URL + REQUEST_FOR_CONFIG;
    PostMethod postMethod = new PostMethod(url);
    postMethod.setRequestHeader(HEADER_CONFIG_VERSION, Long.toString(localConfigVersion));
    postMethod.setRequestHeader(HEADER_JAR_VERSION, Long.toString(localJarVersion));
    postMethod.setRequestHeader(HEADER_APPLICATION_VERSION, applicationVersion);
    postMethod.setRequestHeader(HEADER_REPO_ID, Encoder.byteToString(repoId));
    postMethod.setRequestHeader(HEADER_LANG, Lang.get("lang"));
    if (signature != null && signature.length() > 1 && mail != null && activationCode != null) {
      postMethod.setRequestHeader(HEADER_MAIL, mail);
      postMethod.setRequestHeader(HEADER_SIGNATURE, signature);
      postMethod.setRequestHeader(HEADER_CODE, activationCode);
      postMethod.setRequestHeader(HEADER_COUNT, Long.toString(launchCount));
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
            new DownloadThread(FTP_URL, PicsouApplication.getBankConfigPath(),
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
            new DownloadThread(FTP_URL, PicsouApplication.getJarPath(),
                               generatePicsouJarName(newJarVersion), newJarVersion, jarReceive);
          dowloadJarThread.start();
        }
      }
      Header validityHeader = postMethod.getResponseHeader(HEADER_IS_VALIDE);
      if (validityHeader == null) {
        userState = userState.fireKillUser(false);
      }
      else {
        boolean validity = "true".equalsIgnoreCase(validityHeader.getValue());
        if (!validity) {
          if (checkMailSent(postMethod)) {
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

  private boolean checkMailSent(PostMethod postMethod) {
    Header header = postMethod.getResponseHeader(HEADER_MAIL_SENT);
    return header != null && header.getValue().equals("true");
  }

  public void sendRegister(String mail, String code, final GlobRepository repository) {
    Utils.beginRemove();
    if (URL == null || URL.length() == 0) {
      return;
    }
    Utils.endRemove();
    try {
      String url = URL + REQUEST_FOR_REGISTER;
      final PostMethod postMethod = new PostMethod(url);
      postMethod.setRequestHeader(HEADER_MAIL, mail);
      postMethod.setRequestHeader(HEADER_CODE, code);
      postMethod.setRequestHeader(HEADER_REPO_ID, Encoder.byteToString(repoId));
      postMethod.setRequestHeader(HEADER_LANG, Lang.get("lang"));
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
        updateRepository(repository, User.ACTIVATION_FAILED_HTTP_REQUEST);
      }
    }
    catch (final Exception e) {
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
  }

  private void updateRepository(final GlobRepository repository, final int cause) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        repository.update(User.KEY, User.ACTIVATION_STATE, cause);
      }
    });
  }

  private void computeResponse(GlobRepository repository, PostMethod postMethod) {
    repository.startChangeSet();
    try {
      Header header = postMethod.getRequestHeader(HEADER_MAIL_UNKNOWN);
      if (header != null && "true".equalsIgnoreCase(header.getValue())) {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_MAIL_UNKNOWN);
      }
      else {
        Header signature = postMethod.getResponseHeader(HEADER_SIGNATURE);
        if (signature != null) {
          String value = signature.getValue();
          repository.update(User.KEY, User.IS_REGISTERED_USER, true);
          repository.update(User.KEY, User.SIGNATURE, Encoder.stringToByte(value));
        }
        else {
          Header isMailSentHeader = postMethod.getResponseHeader(HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT);
          if (isMailSentHeader != null && "true".equalsIgnoreCase(isMailSentHeader.getValue())) {
            repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_MAIL_SENT);
          }
          else {
            repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_BAD_SIGNATURE);
          }
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public boolean update(final byte[] repoId, final long launchCount, byte[] mailInBytes,
                        byte[] signatureInByte, final String activationCode) {
    boolean isValideUser;
    if (signatureInByte != null && activationCode != null) {
      if (KeyService.checkSignature(mailInBytes, signatureInByte)) {
        userState = UserStateFactory.localValidSignature();
        isValideUser = true;
      }
      else {
        userState = UserStateFactory.localInvalidSignature();
        isValideUser = false;
      }
    }
    else {
      userState = UserStateFactory.noSignature();
      isValideUser = false;
    }
    final String mail = mailInBytes == null ? null : new String(mailInBytes);
    final String signature = signatureInByte == null ? null : Encoder.byteToString(signatureInByte);
    if (URL != null && URL.length() != 0) {
      // le thread est inliné pour eviter de copier (donc de rendre visible) les variables (repoId, ...)
      // dans des donnée membres
      Thread request = new Thread() {
        {
          setDaemon(true);
        }

        public void run() {
          if (URL == null) {
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

  private void updateUserValidity(Directory directory, GlobRepository repository) {
    userState = userState.updateUserValidity(directory, repository);
  }

  public boolean loadConfig(Directory directory, GlobRepository repository) {
    boolean configLoaded = false;
    if (configReceive != null) {
      configLoaded = configReceive.set(directory, repository);
    }
    if (!configLoaded && currentConfigFile != null) {
      configLoaded = loadConfigFile(currentConfigFile, localConfigVersion, repository, directory);
    }
    if (jarReceive != null) {
      jarReceive.set(directory, repository);
    }
    return configLoaded;
  }


  public Boolean isVerifiedServerValidity() {
    Utils.beginRemove();
    if (URL == null || URL.length() == 0) {
      userState = new CompletedUserState();
      return true;
    }
    Utils.endRemove();
    return userState.isVerifiedServerValidity();
  }

  @Inline
  public static void waitEndOfConfigRequest(Directory directory) {
    ConfigService configService = directory.get(ConfigService.class);
    synchronized (configService) {
      while (!configService.isVerifiedServerValidity()) {
        try {
          configService.wait(200);
        }
        catch (InterruptedException e) {
          return;
        }
      }
    }
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

    protected void loadJar(File jarFile, long version) {
      loadConfigFile(jarFile, version, repository, directory);
    }
  }

  private boolean loadConfigFile(File jarFile, long version, GlobRepository repository, Directory directory) {
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
      repository.update(VersionInformation.KEY, VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, version);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  private static class JarReceive extends AbstractJarReceived {

    protected void loadJar(File jarFile, long version) {
      repository.update(VersionInformation.KEY, VersionInformation.LATEST_AVALAIBLE_JAR_VERSION, version);
    }
  }

  static class UserStateFactory {
    static UserState localValidSignature() {
      return new LocallyValidUser();
    }

    static UserState localInvalidSignature() {
      return new LocallyInvalidUser();
    }

    static UserState noSignature() {
      return new AnonymousUser();
    }
  }
}
