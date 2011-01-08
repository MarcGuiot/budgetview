package org.designup.picsou.gui.components.tips;

import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DetailsTipFactory {

  private GlobRepository repository;
  private Directory directory;

  public DetailsTipFactory(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(JComponent component, String text) {
    DetailsTip tip = new DetailsTip(component, text, directory);
    tip.show();
  }
}
