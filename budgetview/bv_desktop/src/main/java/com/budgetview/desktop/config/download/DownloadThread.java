package com.budgetview.desktop.config.download;

import com.budgetview.desktop.Application;
import org.apache.commons.net.ftp.FTPClient;
import org.globsframework.utils.Log;

import java.io.*;

public class DownloadThread extends Thread {
  private File pathToConfig;
  private String ftpUrl;
  private long version;
  private CompletionCallback completionCallback;
  private String fileName;
  private int lastJarSize = 12 * 1024 * 1024;
  private volatile SizeOutputStream stream;
  private volatile File tempFile = null;


  public int step() {
    if (stream != null) {
      if (tempFile == null){
        return 100;
      }
      return stream.count / lastJarSize;
    }
    return -1;
  }

  public interface CompletionCallback {
    void complete(File name, long version);
  }

  public DownloadThread(String ftpUrl, String rootDirectory, String fileName, long version, CompletionCallback completionCallback) {
    this.ftpUrl = ftpUrl;
    this.version = version;
    this.completionCallback = completionCallback;
    setDaemon(true);
    pathToConfig = new File(rootDirectory);
    pathToConfig.mkdirs();
    this.fileName = fileName;
    String fileSuffix = Application.APPNAME + "Jar";
    try {
      tempFile = File.createTempFile(fileSuffix, ".tmp", pathToConfig);
      stream = new SizeOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
    }
    catch (IOException e) {
      throw new RuntimeException("for file " + tempFile.getAbsolutePath(), e);
    }
  }

  public void run() {
    FTPClient client = new FTPClient();
    String[] splitedUrl = ftpUrl.split(":"); // ftp://localhost:234
    if (splitedUrl.length == 3) {
      client.setDefaultPort(Integer.parseInt(splitedUrl[2]));
    }
    try {
      client.connect(splitedUrl[1].substring("//" .length()));
      if (client.isConnected()) {
        if (!client.login("anonymous", "none")) {
          Log.write("Fail to log to ftp server " + (client.getReplyString()));
        }
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();
        if (client.retrieveFile(fileName, stream)) {
          stream.close();
          File targetFile = new File(pathToConfig, fileName);
          if (tempFile.renameTo(targetFile)) {
            tempFile = null;
            completionCallback.complete(targetFile, version);
          }
          else {
            Log.write("Fail to rename from " + tempFile.getName() + " to " + targetFile.getName());
            tempFile.delete();
            tempFile = null;
          }
        }
        else {
          Log.write("Fail to retrieve " + fileName);
          stream.close();
          tempFile.delete();
          tempFile = null;
        }
      }
      else {
        Log.write("Fail to connect to ftp server " + (client.getReplyString()));
      }
    }
    catch (Exception e) {
      Log.write("in download", e);
      try {
        if (client.isConnected()) {
          client.disconnect();
        }
      }
      catch (IOException e1) {
      }
      if (tempFile != null) {
        tempFile.delete();
      }
      if (stream != null){
        try {
          stream.close();
        }
        catch (Exception e1) {
        }
      }
    }
    finally {
      try {
        client.disconnect();
      }
      catch (Exception e) {
      }
    }
  }


  static class SizeOutputStream extends OutputStream {
    private final OutputStream stream;
    int count;

    SizeOutputStream(OutputStream stream) {
      this.stream = stream;
    }

    public void write(int b) throws IOException {
      count++;
      stream.write(b);
    }

    public void close() throws IOException {
      stream.close();
    }
  }
}
