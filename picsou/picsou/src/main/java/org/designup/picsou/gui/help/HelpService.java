package org.designup.picsou.gui.help;

import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobRepository;
import org.designup.picsou.utils.Lang;

public class HelpService {

  private HelpDialog dialog;
  private GlobRepository repository;
  private Directory directory;
  private HelpSource source;

  public HelpService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.source = new I18NHelpSource();
  }

  public void setSource(HelpSource source) {
    this.source = source;
    this.dialog = null;
  }

  public void show(String helpRef) {
    if (dialog == null) {
      dialog = new HelpDialog(source, repository, directory);
    }
    dialog.show(helpRef);
  }

  private class I18NHelpSource implements HelpSource {
    public String getTitle(String ref) {
      return Lang.get("help." + ref);
    }

    public String getContent(String ref) {
      return Lang.getFile(ref + ".html");
    }
  }
}