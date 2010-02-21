package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.time.selectable.Selectable;
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
    viewPanel.register(new TimeViewPanel.VisibilityListener() {

      public void change(Selectable first, Selectable last) {
        gotoFirstButton.setVisible(first.getVisibility() != Selectable.Visibility.FULLY);
        gotoPrevButton.setVisible(first.getVisibility() != Selectable.Visibility.FULLY);
        gotoLastButton.setVisible(last.getVisibility() != Selectable.Visibility.FULLY);
        gotoNextButton.setVisible(last.getVisibility() != Selectable.Visibility.FULLY);
      }
    });
    createNavigationButtons();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("timeView", viewPanel);
    builder.add(gotoFirstButton, gotoLastButton, gotoNextButton, gotoPrevButton);
  }

  public void selectCurrentMonth() {
    viewPanel.selectMonth(directory.get(TimeService.class).getCurrentMonthId());
  }

  private void createNavigationButtons() {
    gotoFirstButton = new JButton(new FirstMonthNavigationAction(viewPanel));
    Gui.configureIconButton(gotoFirstButton, "firstPeriod", new Dimension(19, 19));

    gotoLastButton = new JButton(new LastMonthNavigationAction(viewPanel));
    Gui.configureIconButton(gotoLastButton, "lastPeriod", new Dimension(19, 19));

    gotoPrevButton = new JButton(new PreviousMonthNavigationAction(viewPanel));
    Gui.configureIconButton(gotoPrevButton, "prevPeriod", new Dimension(19, 19));

    gotoNextButton = new JButton(new NextMonthNavigationAction(viewPanel));
    Gui.configureIconButton(gotoNextButton, "nextPeriod", new Dimension(19, 19));
  }

  public void centerToSelected() {
    viewPanel.centerToSelected();
  }

  private static abstract class MonthNavigationAction extends AbstractAction {

    public MonthNavigationAction(String name) {
      super(name);
    }
  }

  private static class FirstMonthNavigationAction extends MonthNavigationAction {
    private TimeViewPanel viewPanel;

    public FirstMonthNavigationAction(TimeViewPanel viewPanel) {
      super("First");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.gotoFirst();
    }
  }

  private static class LastMonthNavigationAction extends MonthNavigationAction {
    private TimeViewPanel viewPanel;

    public LastMonthNavigationAction(TimeViewPanel viewPanel) {
      super("Last");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.gotoLast();
    }
  }

  private static class PreviousMonthNavigationAction extends MonthNavigationAction {
    private TimeViewPanel viewPanel;

    public PreviousMonthNavigationAction(TimeViewPanel viewPanel) {
      super("Prev");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.gotoPrevious();
    }
  }

  private static class NextMonthNavigationAction extends MonthNavigationAction {
    private TimeViewPanel viewPanel;

    public NextMonthNavigationAction(TimeViewPanel viewPanel) {
      super("Next");
      this.viewPanel = viewPanel;
    }

    public void actionPerformed(ActionEvent e) {
      viewPanel.gotoNext();
    }
  }
}
