package com.designup.siteweaver.server.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoOpFileAccess extends AbstractFileAccess {

  public List<FileHandle> listAllFiles() throws IOException {
    return new ArrayList<FileHandle>();
  }

  public void setApplyChanges(boolean applyChanges) {
    super.setApplyChanges(applyChanges);
    System.out.println("  Apply changes: " + applyChanges);
  }

  public void uploadText(String path, String content) {
    notifyUploadText(path, content);
    System.out.println("  Upload text: " + path + " --> " + content);
  }

  public void uploadFile(String path, File file) {
    notifyUploadFile(path, file);
    System.out.println("  Upload file: " + path + " --> " + file.getAbsolutePath());
  }

  public void delete(String path) {
    notifyDelete(path);
    System.out.println("  Delete file: " + path);
  }

  public void complete() throws IOException {
    System.out.println("  Complete");
  }

  public void dispose() throws IOException {
    System.out.println("  Dispose");
  }
}
