package org.designup.picsou.gui.time;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TimeView extends View {
  private TimeViewPanel viewPanel;
  private JButton gotoPrevButton;
  private JButton gotoNextButton;
  private JButton gotoFirstButton;
  private JButton gotoLastButton;

  public TimeView(GlobRepository globRepository, Directory directory) {
    super(globRepository, directory);
    viewPanel = new TimeViewPanel(globRepository, directory);
    createNavigationButtons();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("month", viewPanel);
    builder.add(gotoFirstButton, gotoLastButton, gotoNextButton, gotoPrevButton);
  }

  public void selectLastMonth() {
    viewPanel.selectLastMonth();
  }

  private void createNavigationButtons() {
    gotoFirstButton = new JButton(new NavigationAction("First") {
      public void actionPerformed(ActionEvent e) {
        viewPanel.goToFirst();
      }
    });
    Gui.configureIconButton(gotoFirstButton, "firstPeriod", new Dimension(19, 19));

    gotoLastButton = new JButton(new NavigationAction("Last") {
      public void actionPerformed(ActionEvent e) {
        viewPanel.goToLast();
      }
    });
    Gui.configureIconButton(gotoLastButton, "lastPeriod", new Dimension(19, 19));

    gotoPrevButton = new JButton(new NavigationAction("Prev") {

      public void actionPerformed(ActionEvent e) {
        viewPanel.goToPrevious();
      }
    });
    Gui.configureIconButton(gotoPrevButton, "prevPeriod", new Dimension(19, 19));

    gotoNextButton = new JButton(new NavigationAction("Next") {

      public void actionPerformed(ActionEvent e) {
        viewPanel.goToNext();
      }
    });
    Gui.configureIconButton(gotoNextButton, "nextPeriod", new Dimension(19, 19));
  }

  private abstract class NavigationAction extends AbstractAction {

    public NavigationAction(String name) {
      super(name);
    }

  }
}
