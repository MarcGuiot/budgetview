package org.designup.picsou.gui.config;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.utils.KeyService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Inline;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ConfigService {

  public static final String COM_APP_LICENSE_URL = PicsouApplication.APPNAME + ".license.url";
  public static final String COM_APP_LICENSE_FTP_URL = PicsouApplication.APPNAME + ".license.ftp.url";
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
  public static final String HEADER_LANG = "lang";
  public static final String REQUEST_FOR_REGISTER = "/register";
  public static final String REQUEST_FOR_CONFIG = "/requestForConfig";
  public static final String REQUEST_FOR_MAIL = "/mailTo";
  public static final String REQUEST_SEND_MAIL = "/sendMailToUs";

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
  public static int RETRY_PERIOD = 1000;
  public static final String HEADER_BAD_ADRESS = "badAdress";
  private Directory directory = null;
  private GlobRepository repository = null;
  public static final String MAIL_CONTACT = "contact";
  private ServerAccess serverAccess;

  public ConfigService(String applicationVersion, Long jarVersion, Long localConfigVersion, File currentConfigFile) {
    this.currentConfigFile = currentConfigFile;
    Utils.beginRemove();
    RETRY_PERIOD = 1000;
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

  synchronized public boolean loadConfigFileFromLastestJar(Directory directory, GlobRepository repository) {
    return loadConfig(directory, repository);
  }

  // return a translated message
  synchronized public String askForNewCodeByMail(String mail) {
    try {
      String url = URL + REQUEST_FOR_MAIL;
      final PostMethod postMethod = new PostMethod(url);
      postMethod.getParams().setContentCharset("UTF-8");
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
      updateConnectionStatus(e);
      return Lang.get("license.mail.send.error");
    }
  }

  synchronized private boolean sendRequestForNewConfig(byte[] repoId, String mail, String signature,
                                                       long launchCount, String activationCode) throws IOException {
    this.repoId = repoId;
    String url = URL + REQUEST_FOR_CONFIG;
    PostMethod postMethod = new PostMethod(url);
    postMethod.getParams().setContentCharset("UTF-8");
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
          configReceive = new ConfigReceive(directory, repository);
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
          jarReceive = new JarReceive(directory, repository, serverAccess);
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

  synchronized public void sendRegister(String mail, String code, final GlobRepository repository) {
    Utils.beginRemove();
    if (URL == null || URL.length() == 0) {
      return;
    }
    Utils.endRemove();
    try {
      String url = URL + REQUEST_FOR_REGISTER;
      final PostMethod postMethod = new PostMethod(url);
      postMethod.getParams().setContentCharset("UTF-8");
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
      Header header = postMethod.getResponseHeader(HEADER_MAIL_UNKNOWN);
      if (header != null && "true".equalsIgnoreCase(header.getValue())) {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_MAIL_UNKNOWN);
      }
      else {
        Header signature = postMethod.getResponseHeader(HEADER_SIGNATURE);
        if (signature != null) {
          String value = signature.getValue();
          repository.update(User.KEY, User.SIGNATURE, Encoder.stringToByte(value));
        }
        else {
          Header isMailSentHeader = postMethod.getResponseHeader(HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT);
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

  public interface Listener {
    void sent(String mail, String title, String content);

    void sendFail(String mail, String title, String content);
  }

  synchronized public void sendMail(final String toMail, final String fromMail,
                                    final String title, final String content, final Listener listener) {
    Thread thread = new Thread() {
      public void run() {
        String url = URL + REQUEST_SEND_MAIL;
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setContentCharset("UTF-8");
        postMethod.setRequestHeader(HEADER_LANG, Lang.get("lang"));
        postMethod.setRequestHeader(HEADER_MAIL, fromMail);
        postMethod.setRequestHeader(HEADER_TO_MAIL, toMail);
        postMethod.setRequestHeader(HEADER_MAIL_TITLE, title);
        postMethod.setRequestHeader(HEADER_MAIL_CONTENT, encodeContent(content));
        try {
          httpClient.executeMethod(postMethod);
        }
        catch (final Exception e) {
          updateConnectionStatus(e);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              Log.write("Email not sent", e);
              listener.sendFail(fromMail, title, content);
            }
          });
          return;
        }
        final int statusCode = postMethod.getStatusCode();
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
              Log.write("Email not sent with error code: " + statusCode);
              listener.sendFail(fromMail, title, content);
            }
          });
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }

  private void updateConnectionStatus(Exception e) {
    if (e instanceof IOException){
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          repository.update(User.KEY, User.CONNECTED, false);
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
    if (URL == null || URL.length() == 0) {
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
}
