package org.designup.picsou.gui.config;

import org.apache.commons.net.ftp.FTPClient;
import org.globsframework.utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadThread extends Thread {
  private File pathToConfig;
  private String ftpUrl;
  private long version;
  private Completed completed;
  private String fileName;

  public interface Completed {
    void complete(File name, long version);
  }

  public DownloadThread(String ftpUrl, String rootDirectory, String fileName, long version, Completed completed) {
    this.ftpUrl = ftpUrl;
    this.version = version;
    this.completed = completed;
    setDaemon(true);
    pathToConfig = new File(rootDirectory);
    pathToConfig.mkdirs();
    this.fileName = fileName;
  }

  public void run() {
    File tempFile = null;
    FTPClient client = new FTPClient();
    String[] splitedUrl = ftpUrl.split(":"); // ftp://localhost:234
    if (splitedUrl.length == 3) {
      client.setDefaultPort(Integer.parseInt(splitedUrl[2]));
    }
    try {
      client.connect(splitedUrl[1].substring("//".length()));
      if (client.isConnected()) {
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        String fileSuffix = "picsouJar";
        tempFile = File.createTempFile(fileSuffix, ".tmp", pathToConfig);
        if (client.retrieveFile(fileName, new FileOutputStream(tempFile))) {
          File targetFile = new File(pathToConfig, fileName);
          if (tempFile.renameTo(targetFile)) {
            tempFile = null;
            completed.complete(targetFile, version);
          }
          else {
            tempFile.delete();
            tempFile = null;
          }
        }
        else {
          tempFile.delete();
          tempFile = null;
        }
      }
      else {
        Log.write("Fail to connect to ftp server " + (client.getReplyString()));
      }
    }
    catch (Exception e) {
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
    }
  }
}
