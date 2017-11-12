package com.budgetview.desktop.userconfig;

import com.budgetview.bank.BankPluginService;
import com.budgetview.client.ConnectionStatus;
import com.budgetview.client.DataAccess;
import com.budgetview.desktop.startup.AppPaths;
import com.budgetview.desktop.userconfig.download.ConfigReceivedCallback;
import com.budgetview.desktop.userconfig.download.DownloadThread;
import com.budgetview.desktop.userconfig.download.JarReceivedCallback;
import com.budgetview.desktop.userconfig.states.*;
import com.budgetview.desktop.utils.KeyService;
import com.budgetview.io.importer.analyzer.TransactionAnalyzerFactory;
import com.budgetview.model.AppVersionInformation;
import com.budgetview.model.LicenseActivationState;
import com.budgetview.model.User;
import com.budgetview.shared.http.Http;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import com.budgetview.utils.Inline;
import com.budgetview.utils.Lang;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class UserConfigService {

  private long localJarVersion = -1;
  private long localConfigVersion = -1;
  private String applicationVersion;
  private UserState userState = null;
  private DownloadThread dowloadJarThread;
  private DownloadThread dowloadConfigThread;
  private ConfigReceivedCallback configReceivedCallback;
  private JarReceivedCallback jarReceive;
  private File currentConfigFile;
  private byte[] repoId;
  private Directory directory = null;
  private GlobRepository repository = null;
  private DataAccess dataAccess;

  public UserConfigService(String applicationVersion, Long jarVersion, Long localConfigVersion, File currentConfigFile) {
    this.currentConfigFile = currentConfigFile;
    this.applicationVersion = applicationVersion;
    this.localJarVersion = jarVersion;
    this.localConfigVersion = localConfigVersion;
  }

  synchronized public boolean loadConfigFileFromLastestJar(Directory directory, GlobRepository repository) {
    return loadConfig(directory, repository);
  }

  private byte[] getRepoId() {
    return repoId;
  }

  synchronized public boolean retrieveUserStatus(final byte[] repoId, final long launchCount, byte[] mailInBytes,
                                                 byte[] signatureInByte, final String activationCode,
                                                 DataAccess dataAccess, boolean dataInMemory) {
    this.dataAccess = dataAccess;
    if (dataInMemory) {
      final String mail = mailInBytes == null ? null : new String(mailInBytes);
      userState = UserStateFactory.localValidSignature(mail);
      return true;
    }
    boolean isValidUser;
    final String mail = mailInBytes == null ? null : new String(mailInBytes);
    if (signatureInByte != null && activationCode != null) {
      if (KeyService.checkSignature(mailInBytes, signatureInByte)) {
        userState = UserStateFactory.localValidSignature(mail);
        isValidUser = true;
      }
      else {
        userState = UserStateFactory.localInvalidSignature(mail);
        isValidUser = false;
      }
    }
    else {
      userState = UserStateFactory.noSignature(mail);
      isValidUser = false;
    }

    final String signature = signatureInByte == null ? null : Encoder.byteToString(signatureInByte);

    Thread request = new Thread() {
      {
        setDaemon(true);
      }

      public void run() {
        boolean connectionEstablished = false;
        while (!connectionEstablished) {
          try {
            connectionEstablished =
              sendUserStatusUpdateRequest(repoId, mail, signature, launchCount, activationCode);
          }
          catch (Exception ex) {
          }
          if (!connectionEstablished) {
            try {
              Thread.sleep(LicenseConstants.RETRY_PERIOD);
            }
            catch (InterruptedException e) {
            }
          }
        }
        synchronized (UserConfigService.this) {
          UserConfigService.this.notify();
        }
      }
    };
    request.start();
    return isValidUser;
  }

  synchronized public void sendLicenseActivationRequest(String mail, String code, final GlobRepository repository) {
    String url = LicenseConstants.getServerUrl(LicenseConstants.REQUEST_FOR_REGISTER);
    Http.Post postRequest = Http.utf8Post(url)
      .setHeader(LicenseConstants.HEADER_MAIL_FROM, mail)
      .setHeader(LicenseConstants.HEADER_CODE, code)
      .setHeader(LicenseConstants.HEADER_REPO_ID, Encoder.byteToString(repoId))
      .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"));
    try {
      HttpResponse response = postRequest.executeWithRetry();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        SwingUtilities.invokeLater(new ComputeRegisterResponse(repository, response));
      }
      else {
        updateRepository(repository, LicenseActivationState.ACTIVATION_FAILED_HTTP_REQUEST);
      }
    }
    catch (final Exception e) {
      ConnectionStatus.checkException(repository, e);
      updateRepository(repository, LicenseActivationState.ACTIVATION_FAILED_CAN_NOT_CONNECT);
      // pas de stack (juste les message) risque de faciliter le piratage
      Thread thread = new Thread() {
        public void run() {
          Throwable f = e;
          while (f != null) {
            Log.write("[User] For activation : " + f.getMessage());
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
      postRequest.dispose();
    }
  }

  synchronized private boolean sendUserStatusUpdateRequest(byte[] repoId, String mail, String signature,
                                                           long launchCount, String activationCode) throws IOException {
    this.repoId = repoId;
    Http.Post postRequest =
      createNewConfigRequest(repoId, mail, signature, launchCount, activationCode,
                             LicenseConstants.getServerUrl(LicenseConstants.REQUEST_FOR_CONFIG));
    try {
      HttpResponse response = postRequest.executeWithRetry();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != 200) {
        Log.write("UserConfigService unexpected reponse " + statusCode + " ");
        return false;
      }

      Header configVersionHeader = response.getFirstHeader(LicenseConstants.HEADER_NEW_CONFIG_VERSION);
      if (configVersionHeader != null) {
        long newConfigVersion = Long.parseLong(configVersionHeader.getValue());
        if (localConfigVersion < newConfigVersion) {
          configReceivedCallback = new ConfigReceivedCallback(this, directory, repository);
          dowloadConfigThread =
            new DownloadThread(LicenseConstants.getFtpServerUrl(), AppPaths.getBankConfigPath(),
                               generateConfigJarName(newConfigVersion), newConfigVersion, configReceivedCallback);
          dowloadConfigThread.start();
        }
      }

      Header jarVersionHeader = response.getFirstHeader(LicenseConstants.HEADER_NEW_JAR_VERSION);
      if (jarVersionHeader != null) {
        long newJarVersion = Long.parseLong(jarVersionHeader.getValue());
        if (localJarVersion < newJarVersion) {
          jarReceive = new JarReceivedCallback(directory, repository, dataAccess);
          dowloadJarThread =
            new DownloadThread(LicenseConstants.getFtpServerUrl(), AppPaths.getJarPath(),
                               generatePicsouJarName(newJarVersion), newJarVersion, jarReceive);
          dowloadJarThread.start();
        }
      }

      Header validityHeader = response.getFirstHeader(LicenseConstants.HEADER_IS_VALID);
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
    finally {
      postRequest.dispose();
    }
  }

  // return a translated message
  synchronized public String sendNewCodeRequest(String mail) {
    Http.Post postRequest = Http.utf8Post(LicenseConstants.getServerUrl(LicenseConstants.REQUEST_FOR_MAIL))
      .setHeader(LicenseConstants.HEADER_MAIL_FROM, mail)
      .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"));
    try {
      HttpResponse response = postRequest.executeWithRetry();
      ConnectionStatus.setOk(repository);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        Header status = response.getFirstHeader(LicenseConstants.HEADER_STATUS);
        if (status == null) {
          return Lang.get("license.mail.send.error");
        }
        if (status.getValue().equalsIgnoreCase(LicenseConstants.HEADER_MAIL_SENT)) {
          return Lang.get("license.mail.sent");
        }
        if (status.getValue().equalsIgnoreCase(LicenseConstants.HEADER_MAIL_SENT_FAILED)) {
          return Lang.get("license.mail.sent.failed");
        }
        if (status.getValue().equalsIgnoreCase(LicenseConstants.HEADER_MAIL_UNKNOWN)) {
          return Lang.get("license.mail.unknown");
        }
        return Lang.get("license.mail.error");
      }
      else {
        return Lang.get("license.mail.send.error");
      }
    }
    catch (IOException e) {
      ConnectionStatus.checkException(repository, e);
      return Lang.get("license.mail.send.error");
    }
    catch (Exception e) {
      ConnectionStatus.checkException(repository, e);
      return Lang.get("license.mail.send.error");
    }
    finally {
      postRequest.dispose();
    }
  }

  private Http.Post createNewConfigRequest(byte[] repoId, String mail, String signature, long launchCount, String activationCode, String url) {
    Http.Post postRequest = Http.utf8Post(url);
    postRequest
      .setHeader(LicenseConstants.HEADER_CONFIG_VERSION, Long.toString(localConfigVersion))
      .setHeader(LicenseConstants.HEADER_JAR_VERSION, Long.toString(localJarVersion))
      .setHeader(LicenseConstants.HEADER_APPLICATION_VERSION, applicationVersion)
      .setHeader(LicenseConstants.HEADER_REPO_ID, Encoder.byteToString(repoId))
      .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"));

    if (signature != null && signature.length() > 1 && mail != null && activationCode != null) {
      postRequest
        .setHeader(LicenseConstants.HEADER_MAIL_FROM, mail)
        .setHeader(LicenseConstants.HEADER_SIGNATURE, signature)
        .setHeader(LicenseConstants.HEADER_CODE, activationCode)
        .setHeader(LicenseConstants.HEADER_COUNT, Long.toString(launchCount));
    }
    return postRequest;
  }

  private boolean checkMailSent(HttpResponse response) {
    Header header = response.getFirstHeader(LicenseConstants.HEADER_MAIL_SENT);
    return header != null && header.getValue().equals("true");
  }

  private void updateRepository(final GlobRepository repository, final LicenseActivationState cause) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, cause.getId());
      }
    });
  }

  private void computeResponse(GlobRepository repository, HttpResponse response) {
    repository.startChangeSet();
    try {
      Header header = response.getFirstHeader(LicenseConstants.HEADER_MAIL_UNKNOWN);
      if (header != null && "true".equalsIgnoreCase(header.getValue())) {
        repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.ACTIVATION_FAILED_MAIL_UNKNOWN.getId());
      }
      else {
        Header signature = response.getFirstHeader(LicenseConstants.HEADER_SIGNATURE);
        if (signature != null) {
          String value = signature.getValue();
          repository.update(User.KEY, User.SIGNATURE, Encoder.stringToByte(value));
        }
        else {
          Header isMailSentHeader = response.getFirstHeader(LicenseConstants.HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT);
          if (isMailSentHeader != null && "true".equalsIgnoreCase(isMailSentHeader.getValue())) {
            repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.ACTIVATION_FAILED_MAIL_SENT.getId());
          }
          else {
            repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.ACTIVATION_FAILED_MAIL_NOT_SENT.getId());
          }
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void sendUsageData(String msg) throws IOException {
    Http.utf8Post(LicenseConstants.getServerUrl(LicenseConstants.SEND_USE_INFO))
      .setHeader(LicenseConstants.HEADER_USE_INFO, msg)
      .execute();
  }


  public int downloadStep() {
    waitEndOfConfigRequest(3000);
    if (dowloadJarThread != null) {
      return dowloadJarThread.step();
    }
    return -1;
  }

  @Inline
  public static void check(Directory directory, GlobRepository repository) {
    UserConfigService userConfigService = directory.get(UserConfigService.class);
    userConfigService.updateUserValidity(repository, directory);
  }

  synchronized private void updateUserValidity(GlobRepository repository, Directory directory) {
    userState = userState.updateUserValidity(repository, directory);
  }

  public boolean loadConfig(Directory directory, GlobRepository repository) {
    boolean configLoaded = false;
    if (configReceivedCallback != null) {
      configLoaded = configReceivedCallback.set(directory, repository);
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
    return userState.isVerifiedServerValidity();
  }

  @Inline
  public static boolean waitEndOfConfigRequest(Directory directory, int timeout) {
    UserConfigService userConfigService = directory.get(UserConfigService.class);
    return userConfigService.waitEndOfConfigRequest(timeout);
  }

  public boolean waitEndOfConfigRequest(int timeout) {
    long l = System.currentTimeMillis() + timeout;
    synchronized (this) {
      while (!isVerifiedServerValidity() && !Thread.currentThread().isInterrupted()
             && (timeout < 0 || System.currentTimeMillis() < l)) {
        try {
          wait(200);
        }
        catch (InterruptedException e) {
          return false;
        }
      }
    }
    return isVerifiedServerValidity();
  }

  public static String generatePicsouJarName(long newVersion) {
    String name = Long.toString(newVersion);
    return LicenseConstants.APPNAME + name + ".jar";
  }

  public static String generateConfigJarName(long newVersion) {
    String name = Long.toString(newVersion);
    return "config" + name + ".jar";
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
        Log.write("[User] Failed to read " + name, e);
      }
      catch (Exception e) {
        Log.write("[User] Failed to load class " + name, e);
      }
    }
  }

  public boolean loadConfigFile(File jarFile, long version, final GlobRepository repository, final Directory directory) {
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
                Log.write("[User] Failed to get entry for " + className, e);
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
      Log.write("[User] Error for " + jarFile.getAbsolutePath(), e);
      return false;
    }
  }

  private static class UserStateFactory {
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

  private class ComputeRegisterResponse implements Runnable {
    private final GlobRepository repository;
    private HttpResponse response;

    ComputeRegisterResponse(GlobRepository repository, HttpResponse response) {
      this.repository = repository;
      this.response = response;
    }

    public void run() {
      computeResponse(repository, response);
    }
  }

  public void setLang(String lang) {
    dataAccess.setLang(lang);
  }
}
