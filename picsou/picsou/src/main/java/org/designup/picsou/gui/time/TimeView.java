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
    gotoFirstButton = new JButton(new FirstPeriodNavigationAction(viewPanel));
    Gui.configureIconButton(gotoFirstButton, "firstPeriod", new Dimension(19, 19));

    gotoLastButton = new JButton(new LastNavigationAction(viewPanel));
    Gui.configureIconButton(gotoLastButton, "lastPeriod", new Dimension(19, 19));

    gotoPrevButton = new JButton(new PreviousNavigationAction(viewPanel));
    Gui.configureIconButton(gotoPrevButton, "prevPeriod", new Dimension(19, 19));

    gotoNextButton = new JButton(new NextNavigationAction(viewPanel));
    Gui.configureIconButton(gotoNextButton, "nextPeriod", new Dimension(19, 19));
  }

  private static abstract class NavigationAction extends AbstractAction {

    public NavigationAction(String name) {
      super(name);
    }
  }

  private static class FirstPeriodNavigationAction extends NavigationAction {
    private TimeViewPanel viewPanel;

    public FirstPeriodNavigationAction(TimeViewPanel viewPanel) {
      super("First");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.goToFirst();
    }
  }

  private static class LastNavigationAction extends NavigationAction {
    private TimeViewPanel viewPanel;

    public LastNavigationAction(TimeViewPanel viewPanel) {
      super("Last");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.goToLast();
    }
  }

  private static class PreviousNavigationAction extends NavigationAction {
    private TimeViewPanel viewPanel;

    public PreviousNavigationAction(TimeViewPanel viewPanel) {
      super("Prev");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.goToPrevious();
    }
  }

  private static class NextNavigationAction extends NavigationAction {
    private TimeViewPanel viewPanel;

    public NextNavigationAction(TimeViewPanel viewPanel) {
      super("Next");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.goToNext();
    }
  }
}
