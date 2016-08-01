package com.budgetview.gui.config;

import com.budgetview.bank.BankPluginService;
import com.budgetview.client.ConnectionStatus;
import com.budgetview.client.ServerAccess;
import com.budgetview.client.http.Http;
import com.budgetview.gui.config.download.ConfigReceivedCallback;
import com.budgetview.gui.config.download.DownloadThread;
import com.budgetview.gui.config.states.AnonymousUser;
import com.budgetview.gui.config.states.CompletedUserState;
import com.budgetview.gui.config.states.LocallyInvalidUser;
import com.budgetview.gui.config.states.LocallyValidUser;
import com.budgetview.gui.startup.AppPaths;
import com.budgetview.gui.utils.KeyService;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.io.importer.analyzer.TransactionAnalyzerFactory;
import com.budgetview.model.AppVersionInformation;
import com.budgetview.model.LicenseActivationState;
import com.budgetview.model.User;
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

public class ConfigService {

  private long localJarVersion = -1;
  private long localConfigVersion = -1;
  private String applicationVersion;
  private UserState userState = null;
  private DownloadThread dowloadJarThread;
  private DownloadThread dowloadConfigThread;
  private ConfigReceivedCallback configReceive;
  private JarReceivedCallback jarReceive;
  private File currentConfigFile;
  private byte[] repoId;
  private Directory directory = null;
  private GlobRepository repository = null;
  private ServerAccess serverAccess;

  public ConfigService(String applicationVersion, Long jarVersion, Long localConfigVersion, File currentConfigFile) {
    this.currentConfigFile = currentConfigFile;
    this.applicationVersion = applicationVersion;
    this.localJarVersion = jarVersion;
    this.localConfigVersion = localConfigVersion;
  }

  synchronized public boolean loadConfigFileFromLastestJar(Directory directory, GlobRepository repository) {
    return loadConfig(directory, repository);
  }

  // return a translated message
  synchronized public String askForNewCodeByMail(String mail) {
    Http.Post postRequest = Http.utf8Post(LicenseConstants.getLicenseServerUrl(LicenseConstants.REQUEST_FOR_MAIL))
      .setHeader(LicenseConstants.HEADER_MAIL, mail)
      .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"));
    try {
      HttpResponse response = postRequest.executeWithRetry();
      ConnectionStatus.setOk(repository);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        Header status = response.getFirstHeader(LicenseConstants.HEADER_STATUS);
        if (status != null) {
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
        return Lang.get("license.mail.send.error");
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

  synchronized private boolean sendRequestForNewConfig(byte[] repoId, String mail, String signature,
                                                       long launchCount, String activationCode) throws IOException {
    this.repoId = repoId;
    Http.Post postRequest =
      createNewConfigRequest(repoId, mail, signature, launchCount, activationCode,
                             LicenseConstants.getLicenseServerUrl(LicenseConstants.REQUEST_FOR_CONFIG));
    try {
      HttpResponse response = postRequest.executeWithRetry();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        Header configVersionHeader = response.getFirstHeader(LicenseConstants.HEADER_NEW_CONFIG_VERSION);
        if (configVersionHeader != null) {
          long newConfigVersion = Long.parseLong(configVersionHeader.getValue());
          if (localConfigVersion < newConfigVersion) {
            configReceive = new ConfigReceivedCallback(this, directory, repository);
            dowloadConfigThread =
              new DownloadThread(LicenseConstants.getFtpServerUrl(), AppPaths.getBankConfigPath(),
                                 generateConfigJarName(newConfigVersion), newConfigVersion, configReceive);
            dowloadConfigThread.start();
          }
        }
        Header jarVersionHeader = response.getFirstHeader(LicenseConstants.HEADER_NEW_JAR_VERSION);
        if (jarVersionHeader != null) {
          long newJarVersion = Long.parseLong(jarVersionHeader.getValue());
          if (localJarVersion < newJarVersion) {
            jarReceive = new JarReceivedCallback(directory, repository, serverAccess);
            dowloadJarThread =
              new DownloadThread(LicenseConstants.getFtpServerUrl(), AppPaths.getJarPath(),
                                 generatePicsouJarName(newJarVersion), newJarVersion, jarReceive);
            dowloadJarThread.start();
          }
        }
        Header validityHeader = response.getFirstHeader(LicenseConstants.HEADER_IS_VALIDE);
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
        .setHeader(LicenseConstants.HEADER_MAIL, mail)
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

  synchronized public void sendRegistration(String mail, String code, final GlobRepository repository) {
    Utils.beginRemove();
    if (!LicenseConstants.isLicenseServerUrlSet()) {
      return;
    }
    Utils.endRemove();
    String url = LicenseConstants.getLicenseServerUrl(LicenseConstants.REQUEST_FOR_REGISTER);
    Http.Post postRequest = createRegisterRequest(mail, code, url);
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
      postRequest.dispose();
    }
  }

  private Http.Post createRegisterRequest(String mail, String code, String url) {
    return Http.utf8Post(url)
      .setHeader(LicenseConstants.HEADER_MAIL, mail)
      .setHeader(LicenseConstants.HEADER_CODE, code)
      .setHeader(LicenseConstants.HEADER_REPO_ID, Encoder.byteToString(repoId))
      .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"));
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
    Http.utf8Post(LicenseConstants.getLicenseServerUrl(LicenseConstants.SEND_USE_INFO))
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

  synchronized public boolean update(final byte[] repoId, final long launchCount, byte[] mailInBytes,
                                     byte[] signatureInByte, final String activationCode,
                                     ServerAccess serverAccess, boolean dataInMemory) {
    this.serverAccess = serverAccess;
    if (dataInMemory) {
      final String mail = mailInBytes == null ? null : new String(mailInBytes);
      userState = UserStateFactory.localValidSignature(mail);
      return true;
    }
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
    if (LicenseConstants.isLicenseServerUrlSet()) {
      // le thread est inliné pour eviter de copier (donc de rendre visible) les variables (repoId, ...)
      // dans des donnée membres
      Thread request = new Thread() {
        {
          setDaemon(true);
        }

        public void run() {
          if (!LicenseConstants.isLicenseServerUrlSet()) {
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
                Thread.sleep(LicenseConstants.RETRY_PERIOD);
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
    if (!LicenseConstants.isLicenseServerUrlSet()) {
      userState = new CompletedUserState("local");
      return true;
    }
    Utils.endRemove();
    return userState.isVerifiedServerValidity();
  }

  @Inline
  public static boolean waitEndOfConfigRequest(Directory directory, int timeout) {
    ConfigService configService = directory.get(ConfigService.class);
    return configService.waitEndOfConfigRequest(timeout);
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
        Log.write("failed to read " + name, e);
      }
      catch (Exception e) {
        Log.write("failed to load class " + name, e);
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
      Log.write("for " + jarFile.getAbsolutePath(), e);
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
    serverAccess.setLang(lang);
  }
}
