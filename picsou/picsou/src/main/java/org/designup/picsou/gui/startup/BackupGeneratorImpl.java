package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.BackupGenerator;
import org.designup.picsou.gui.PicsouInit;

import java.io.IOException;
import java.io.InputStream;

public class BackupGeneratorImpl implements BackupGenerator {
  private PicsouInit picsouInit;

  public BackupGeneratorImpl(PicsouInit picsouInit) {
    this.picsouInit = picsouInit;
  }

  public void generateIn(String backupFile) throws IOException {
    picsouInit.generateBackupIn(backupFile);
  }

  public void restore(InputStream stream) {
    picsouInit.restore(stream);
  }
}