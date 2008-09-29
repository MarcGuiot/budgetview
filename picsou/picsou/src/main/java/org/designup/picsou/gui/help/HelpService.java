package org.designup.picsou.gui.help;

import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobRepository;

public class HelpService {

  private HelpDialog dialog;
  private GlobRepository repository;
  private Directory directory;

  public HelpService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(String helpRef) {
    if (dialog == null) {
      dialog = new HelpDialog(repository, directory);
    }
    dialog.show(helpRef);
  }
}
