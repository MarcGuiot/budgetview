package org.designup.picsou.gui.help;

import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HelpService {

  private HelpDialog dialog;
  private GlobRepository repository;
  private Directory directory;
  private HelpSource source;
  private Window lastOwner;

  public HelpService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.source = new I18NHelpSource();
  }

  public void setSource(HelpSource source) {
    this.source = source;
  }

  public void show(String helpRef, Window owner) {
    if ((dialog != null) && dialog.isVisible()) {
      if (owner != lastOwner) {
        dialog.close();
        dialog = null;
        lastOwner = null;
      }
    }
    if ((dialog == null) || (lastOwner != owner)) {
      dialog = new HelpDialog(source, repository, directory, owner);
      lastOwner = owner;
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