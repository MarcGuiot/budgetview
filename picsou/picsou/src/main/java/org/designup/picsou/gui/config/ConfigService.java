package org.designup.picsou.gui.config;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.client.exceptions.BadConnection;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

public class ConfigService {
  private HttpClient httpClient;
  private File pathToConfig;
  private File configFile;
  private long localVersion;
  private Integer applicationVersion;
  private Integer lauchCount;

  public ConfigService(Integer applicationversion) {
    applicationVersion = applicationversion;
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

    Arrays.sort(files, new Comparator<File>() {
      public int compare(File f1, File f2) {
        String s1 = f1.getName();
        String s2 = f2.getName();
        return new Long(Long.parseLong(s1.substring(0, s1.indexOf("."))))
          .compareTo(Long.parseLong(s2.substring(0, s2.indexOf("."))));
      }
    });
    long localVersion = -1;
    if (files.length >= 1) {
      configFile = files[files.length - 1];
      this.localVersion = localVersion;
      this.localVersion = Long.parseLong(configFile.getName().substring(0, configFile.getName().indexOf(".")));
    }

    startCheckForNewVersion();
  }

  private void startCheckForNewVersion() {
    Thread thread = new ConfigRequest();
    thread.start();
  }

  public void loadConfig(GlobRepository repository) {
    Glob user = repository.get(Key.create(User.TYPE, User.SINGLETON_ID));
    lauchCount = user.get(User.LAUNCH_COUNT);
    synchronized (this) {
      notify();
    }
  }

  private void sendRequestForNewConfig(String url) {
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
        int newVersion = -1;
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

  private void sendRequestForLicence() {
  }

  private class ConfigRequest extends Thread {

    public void run() {
      sendRequestForNewConfig("");
      sendRequestForLicence();
    }

  }
}
