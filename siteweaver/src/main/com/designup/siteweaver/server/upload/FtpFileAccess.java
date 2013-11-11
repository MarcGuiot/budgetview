package com.designup.siteweaver.server.upload;

import com.designup.siteweaver.utils.FileTree;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FtpFileAccess extends AbstractFileAccess {
  private String rootPath;
  private FTPClient ftp;
  private final String[] TEXT_EXTENSIONS = {".html", ".css", ".js", ".htaccess", ".txt"};
  private FileTree fileTree;

  public FtpFileAccess(String hostname, String rootPath, String user, String password) throws IOException {
    this.rootPath = normalizeRootPath(rootPath);
    ftp = new FTPClient();
    ftp.connect(hostname);
    int reply = ftp.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      ftp.disconnect();
      throw new IOException("Exception in connecting to FTP Server");
    }
    ftp.login(user, password);
    fileTree = new FileTree();
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

  public List<FileHandle> listAllFiles() throws IOException {
    List<FileHandle> result = new ArrayList<FileHandle>();
    listDirectory(rootPath, 0, result);
    return result;
  }

  private void listDirectory(String currentDir,
                             int level,
                             List<FileHandle> result) throws IOException {
    FTPFile[] subFiles = ftp.listFiles(currentDir);
    if (subFiles != null && subFiles.length > 0) {
      for (FTPFile subFile : subFiles) {
        String currentFileName = subFile.getName();
        if (currentFileName.equals(".") || currentFileName.equals("..")) {
          continue;
        }
        if (subFile.isDirectory()) {
          listDirectory(currentDir + "/" + currentFileName, level + 1, result);
        }
        else {
          String path = currentDir + "/" + currentFileName;
          if (rootPath.length() > 1) {
            path = path.substring(rootPath.length());
          }
          result.add(new FileHandle(path, subFile.getTimestamp().getTimeInMillis()));
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
        ftp.makeDirectory(name);
        ftp.changeWorkingDirectory(name);
      }

      public void enterDirectory(String name) throws IOException {
        System.out.println("cd " + name);
        ftp.changeWorkingDirectory(name);
      }

      public void gotoParentDirectory() throws IOException {
        System.out.println("cd ..");
        ftp.changeToParentDirectory();
      }

      public void updateFile(String name, InputStream inputStream) throws IOException {
        System.out.println("upload " + name);
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
        ftp.deleteFile(name);
      }
    });
    System.out.println("done");
    fileTree = new FileTree();
  }

  public void dispose() throws IOException {
    ftp.logout();
    ftp.disconnect();
    ftp = null;
  }

  private boolean isTextField(String fileName) {
    for (String extension : TEXT_EXTENSIONS) {
      if (fileName.endsWith(extension)) {
        return true;
      }
    }
    return false;
  }
}
