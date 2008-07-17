package org.designup.picsou.gui.license;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class LicenseDialog {

  public LicenseDialog(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      repository, directory);
//    builder.addEditor()
  }


}
