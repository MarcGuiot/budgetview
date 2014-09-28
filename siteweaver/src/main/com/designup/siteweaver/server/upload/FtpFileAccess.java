package com.designup.siteweaver.server.upload;

import com.designup.siteweaver.utils.FileTree;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FtpFileAccess extends AbstractFileAccess {
  private final String hostname;
  private final String user;
  private final String password;
  private String rootPath;
  private FTPClient ftp;
  private final String[] TEXT_EXTENSIONS = {".html", ".css", ".js", ".htaccess", ".txt"};
  private FileTree fileTree;

  public FtpFileAccess(String hostname, String rootPath, String user, String password) {
    this.hostname = hostname;
    this.user = user;
    this.password = password;
    this.rootPath = normalizeRootPath(rootPath);
  }

  private FTPClient getFTP() throws IOException {
    if (ftp == null) {
      initFTP();
    }
    return ftp;
  }

  public void initFTP() throws IOException {
    ftp = new FTPClient();
    ftp.connect(hostname);
    int reply = ftp.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      ftp.disconnect();
      throw new IOException("Exception in connecting to FTP Server");
    }
    boolean loginAccepted = ftp.login(user, password);
    if (!loginAccepted) {
      ftp.disconnect();
      throw new IOException("FTP login failed for server '" + hostname + "', user '" + user + "'");
    }
    fileTree = new FileTree();
  }

  public List<FileHandle> listAllFiles() throws IOException {
    System.out.print("Listing remote files");
    List<FileHandle> result = new ArrayList<FileHandle>();
    listDirectory(rootPath, 0, result, getTimeOffset());
    System.out.println();
    return result;
  }

  private long getTimeOffset() throws IOException {
    FTPClient ftp = getFTP();
      ftp.setFileType(FTP.ASCII_FILE_TYPE);
    long result = 0;
    try {
      String tmpFile = "siteweaver_timestamp.txt";
      ftp.storeFile(tmpFile, new ByteArrayInputStream("test".getBytes("UTF-8")));
      for (FTPFile ftpFile : ftp.listFiles(tmpFile)) {
        if (ftpFile.getName().equals(tmpFile)) {
          Calendar remote = ftpFile.getTimestamp();
          Calendar local = Calendar.getInstance();
          result = local.getTimeInMillis() - remote.getTimeInMillis();
          break;
        }
      }
      ftp.deleteFile(tmpFile);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private void listDirectory(String currentDir,
                             int level,
                             List<FileHandle> result,
                             long timeOffset) throws IOException {
    FTPFile[] subFiles = getFTP().listFiles(currentDir);
    System.out.print(".");
    if (subFiles != null && subFiles.length > 0) {
      for (FTPFile subFile : subFiles) {
        String currentFileName = subFile.getName();
        if (currentFileName.equals(".") || currentFileName.equals("..")) {
          continue;
        }
        if (subFile.isDirectory()) {
          listDirectory(currentDir + "/" + currentFileName, level + 1, result, timeOffset);
        }
        else {
          String path = currentDir + "/" + currentFileName;
          if (rootPath.length() > 1) {
            path = path.substring(rootPath.length());
          }
          long timestamp = subFile.getTimestamp().getTimeInMillis() + timeOffset;
          if (subFile.getSize() == 0) {
            timestamp = 0;
          }
          result.add(new FileHandle(path, timestamp));
        }
      }
    }
  }

  public void uploadText(String path, String content) throws IOException {
    String remotePath = rootPath + path;
    notifyUploadText(remotePath, content);
    if (!applyChanges) {
      return;
    }
    fileTree.update(remotePath, new ByteArrayInputStream(content.getBytes("UTF-8")));
  }

  public void uploadFile(String path, File file) throws IOException {
    String remotePath = rootPath + path;
    notifyUploadFile(remotePath, file);
    if (!applyChanges) {
      return;
    }
    fileTree.update(remotePath, new FileInputStream(file));
  }

  public void delete(String path) throws IOException {
    String remotePath = rootPath + path;
    notifyDelete(remotePath);
    if (!applyChanges) {
      return;
    }
    fileTree.delete(remotePath);
  }

  public void complete() throws IOException {
    fileTree.apply(new FileTree.Functor() {
      public void createDirectory(String name) throws IOException {
        System.out.println("mkdir " + name);
        FTPClient ftp = getFTP();
        ftp.makeDirectory(name);
        ftp.changeWorkingDirectory(name);
      }

      public void enterDirectory(String name) throws IOException {
        System.out.println("cd " + name);
        FTPClient ftp = getFTP();
        ftp.changeWorkingDirectory(name);
      }

      public void gotoParentDirectory() throws IOException {
        System.out.println("cd ..");
        FTPClient ftp = getFTP();
        ftp.changeToParentDirectory();
      }

      public void updateFile(String name, InputStream inputStream) throws IOException {
        System.out.println("upload " + name);
        FTPClient ftp = getFTP();
        ftp.deleteFile(name);
        if (isTextField(name)) {
          ftp.setFileType(FTP.ASCII_FILE_TYPE);
        }
        else {
          ftp.setFileType(FTP.BINARY_FILE_TYPE);
        }
        try {
          ftp.storeFile(name, inputStream);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
        finally {
          if (inputStream != null) {
            inputStream.close();
          }
        }
      }

      public void deleteFile(String name) throws IOException {
        System.out.println("delete " + name);
        FTPClient ftp = getFTP();
        ftp.deleteFile(name);
      }
    });
    System.out.println("done");
    fileTree = new FileTree();
  }

  public void dispose() throws IOException {
    if (ftp != null) {
      ftp.logout();
      ftp.disconnect();
      ftp = null;
    }
  }

  private boolean isTextField(String fileName) {
    for (String extension : TEXT_EXTENSIONS) {
      if (fileName.endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

  private String normalizeRootPath(String rootPath) {
    if (rootPath.length() == 0) {
      return "";
    }
    if (!rootPath.startsWith("/")) {
      return "/" + rootPath;
    }
    return rootPath;
  }
}
