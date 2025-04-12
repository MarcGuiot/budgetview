package com.designup.siteweaver.model;

import java.io.File;
import java.io.IOException;

public interface FileFunctor {
  void process(File inputFile, String targetPath) throws IOException;
}
