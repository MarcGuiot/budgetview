package org.designup.picsou.gui.components;

import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.gui.splits.components.ArrowIcon;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class MonthSlider extends JPanel implements Disposable, ChangeSetListener {
  public static final int SPACE = 2;

  private JButton label = new JButton(new LabelAction());
  private JButton previous = new JButton(new PreviousMonthAction());
  private JButton next = new JButton(new NextMonthAction());

  private Key key;
  private MonthSliderAdapter adapter;
  private GlobRepository repository;
  private Directory directory;
  private boolean fixedWidth;

  private ArrowIcon previousButtonIcon;
  private ArrowIcon previousButtonRolloverIcon;
  private ArrowIcon nextButtonIcon;
  private ArrowIcon nextButtonRolloverIcon;

  public MonthSlider(MonthSliderAdapter adapter, GlobRepository repository, Directory directory) {
    this.adapter = adapter;
    this.repository = repository;
    this.directory = directory;

    HyperlinkButtonUI buttonUI = new HyperlinkButtonUI();
    buttonUI.setUnderline(false);
    this.label.setUI(buttonUI);

    initArrowButton(previous);
    this.previousButtonIcon = new ArrowIcon(10, 10, ArrowIcon.Orientation.LEFT);
    this.previous.setIcon(previousButtonIcon);
    this.previousButtonRolloverIcon = new ArrowIcon(10, 10, ArrowIcon.Orientation.LEFT);
    this.previous.setRolloverIcon(previousButtonRolloverIcon);

    initArrowButton(next);
    this.nextButtonIcon = new ArrowIcon(10, 10, ArrowIcon.Orientation.RIGHT);
    this.next.setIcon(nextButtonIcon);
    this.nextButtonRolloverIcon = new ArrowIcon(10, 10, ArrowIcon.Orientation.RIGHT);
    this.next.setRolloverIcon(nextButtonRolloverIcon);

    this.label.setName("month");
    this.previous.setName("previousMonth");
    this.next.setName("nextMonth");

    setOpaque(false);
    setLayout(new PanelLayout());
    add(previous);
    add(label);
    add(next);

    updatePreferredSize();

    repository.addChangeListener(this);
    updateLabel();
  }

  public void setKey(Key key) {
    this.key = key;
    updateLabel();
  }

  public void setFixedWidth(boolean fixed) {
    this.fixedWidth = fixed;
  }

  private void initArrowButton(JButton button) {
    button.setUI(new BasicButtonUI());
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setOpaque(false);
    button.setBorder(null);
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setBorderPainted(false);
  }

  private void updatePreferredSize() {
    FontMetrics metrics = label.getFontMetrics(label.getFont());

    int width = (int)previous.getPreferredSize().getWidth() +
                SPACE +
                metrics.stringWidth(adapter.getMaxText()) +
                SPACE +
                (int)next.getPreferredSize().getWidth();

    Dimension size = new Dimension(width, label.getPreferredSize().height);
    setPreferredSize(size);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (key != null && changeSet.containsChanges(key)) {
      updateLabel();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (key != null && changedTypes.contains(key.getGlobType())) {
      updateLabel();
    }
  }

  private void updateLabel() {
    Glob glob = repository.find(key);
    setButtonsEnabled(glob != null);
    String month = adapter.getText(glob, repository);
    label.setText(month);
  }

  private void setButtonsEnabled(boolean enabled) {
    label.setEnabled(enabled);
    previous.setEnabled(enabled);
    previous.setVisible(enabled);
    next.setEnabled(enabled);
    next.setVisible(enabled);
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  public void setFont(Font font) {
    if (label == null) {
      return;
    }
    label.setFont(font);
    updatePreferredSize();
  }

  public void setForeground(Color color) {
    if (label == null) {
      return;
    }
    label.setForeground(color);
  }

  public void setIconColor(Color color) {
    previousButtonIcon.setColor(color);
    nextButtonIcon.setColor(color);
  }

  public void setRolloverIconColor(Color color) {
    previousButtonRolloverIcon.setColor(color);
    nextButtonRolloverIcon.setColor(color);
  }

  private class LabelAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      MonthChooserDialog monthChooser = new MonthChooserDialog(GuiUtils.getEnclosingWindow(MonthSlider.this), directory);
      Glob glob = repository.get(key);
      Integer currentMonthId = adapter.getCurrentMonth(glob, repository);
      int selectedMonthId = monthChooser.show(currentMonthId,
                                              MonthRangeBound.NONE,
                                              CurrentMonth.getLastMonth(repository));
      if (selectedMonthId > 0) {
        adapter.setMonth(glob, selectedMonthId, repository);
      }
    }
  }

  private class PreviousMonthAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      Glob glob = repository.get(key);
      adapter.setMonth(glob, Month.previous(adapter.getCurrentMonth(glob, repository)), repository);
    }
  }

  private class NextMonthAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      Glob glob = repository.get(key);
      adapter.setMonth(glob, Month.next(adapter.getCurrentMonth(glob, repository)), repository);
    }
  }

  private class PanelLayout implements LayoutManager {
    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
      return parent.getPreferredSize();
    }

    public Dimension minimumLayoutSize(Container parent) {
      return parent.getPreferredSize();
    }

    public void layoutContainer(Container parent) {
      int panelWidth = getPreferredSize().width - 1;
      int panelHeight = getPreferredSize().height - 1;

      previous.setBounds(0,
                         panelHeight / 2 - previous.getPreferredSize().height / 2,
                         previous.getPreferredSize().width, previous.getPreferredSize().height);

      if (fixedWidth) {
        label.setBounds(panelWidth / 2 - label.getPreferredSize().width / 2,
                        panelHeight / 2 - label.getPreferredSize().height / 2,
                        label.getPreferredSize().width, label.getPreferredSize().height);

        Rectangle nextBounds = new Rectangle(panelWidth - next.getPreferredSize().width,
                                             panelHeight / 2 - next.getPreferredSize().height / 2,
                                             next.getPreferredSize().width, next.getPreferredSize().height);
        next.setBounds(nextBounds);
      }
      else {
        int labelX = previous.getPreferredSize().width + SPACE;
        label.setBounds(labelX,
                        panelHeight / 2 - label.getPreferredSize().height / 2,
                        label.getPreferredSize().width, label.getPreferredSize().height);

        Rectangle nextBounds = new Rectangle(labelX + label.getPreferredSize().width + SPACE,
                                             panelHeight / 2 - next.getPreferredSize().height / 2,
                                             next.getPreferredSize().width, next.getPreferredSize().height);
        next.setBounds(nextBounds);
      }
    }
  }
}
