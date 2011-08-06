package org.designup.picsou.gui.components.tips;

import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowDetailsTipAction extends AbstractAction {

  private JComponent component;
  private Directory directory;

  public ShowDetailsTipAction(JComponent component, Directory directory) {
    this.component = component;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    DetailsTip tip = new DetailsTip((JComponent)actionEvent.getSource(), component.getToolTipText(), directory);
    tip.show();
  }
}
