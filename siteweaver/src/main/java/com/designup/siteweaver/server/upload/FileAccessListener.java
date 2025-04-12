package com.designup.siteweaver.server.upload;

import java.io.File;

public interface FileAccessListener {
  void processUploadText(String path, String content);

  void processUploadFile(String path, File file);

  void processDelete(String path);
}
