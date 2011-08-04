package org.designup.picsou.gui.components.tips;

import org.designup.picsou.gui.components.charts.Gauge;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowDetailsTipAction extends AbstractAction {

  private Gauge gauge;
  private Directory directory;

  public ShowDetailsTipAction(Gauge gauge, Directory directory) {
    this.gauge = gauge;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    DetailsTip tip = new DetailsTip((JComponent)actionEvent.getSource(), gauge.getTooltip(), directory);
    tip.show();
  }
}
