package org.designup.picsou.gui;

import java.io.IOException;
import java.io.InputStream;

public interface BackupGenerator {
  void generateIn(String path) throws IOException;

  void restore(InputStream stream);
}
