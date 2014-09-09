package com.designup.siteweaver.server.upload;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.model.FileFunctor;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.PageFunctor;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.server.utils.StringOutput;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SiteUploader {

  private Site site;
  private FileAccess access;

  public SiteUploader(Site site, FileAccess access) {
    this.site = site;
    this.access = access;
  }

  public void run() throws IOException {
    final Map<String, FileHandle> remoteHandles = new HashMap<String, FileHandle>();
    for (FileHandle handle : access.listAllFiles()) {
      if (!site.ignoreTargetPath(handle.path)) {
        remoteHandles.put(handle.path, handle);
      }
    }
    site.processPages(new PageFunctor() {
      public void process(Page page) throws Exception {
        String targetPath = site.getTargetPath(page);
        long localTimestamp = site.getLastModified(page);
        FileHandle handle = remoteHandles.get(targetPath);
        if (handle == null || localTimestamp > handle.timestamp) {
          StringOutput output = new StringOutput();
          SiteGenerator.run(site, page, output);
          access.uploadText(targetPath, output.getText());
        }
        if (handle != null) {
          remoteHandles.remove(targetPath);
        }
      }
    });
    site.processFiles(new FileFunctor() {
      public void process(File inputFile, String targetPath) throws IOException {
        if (".DS_Store".equals(inputFile.getName()) || site.ignoreTargetPath(targetPath)) {
          return;
        }

        long localTimestamp = inputFile.lastModified();
        FileHandle handle = remoteHandles.get(targetPath);
        if (handle == null || localTimestamp > handle.timestamp) {
          access.uploadFile(targetPath, inputFile);
        }
        if (handle != null) {
          remoteHandles.remove(targetPath);
        }
      }
    });
    for (String path : remoteHandles.keySet()) {
      if (!path.endsWith(".htaccess")) {
        access.delete(path);
      }
    }
    access.complete();
  }
}
