package org.designup.picsou.gui.components.tips;

import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DetailsTipFactory {

  private Directory directory;

  public DetailsTipFactory(Directory directory) {
    this.directory = directory;
  }

  public void show(JComponent component, String text) {
    DetailsTip tip = new DetailsTip(component, text, directory);
    tip.show();
  }
}
