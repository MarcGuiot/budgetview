package com.designup.siteweaver.server.upload;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileAccess {

  List<FileHandle> listAllFiles() throws IOException;

  void addListener(FileAccessListener listener);

  void removeListener(FileAccessListener listener);

  void setApplyChanges(boolean applyChanges);

  void uploadText(String path, String content) throws IOException;

  void uploadFile(String path, File file) throws IOException;

  void delete(String path) throws IOException;

  void complete() throws IOException;

  void dispose() throws IOException;
}
