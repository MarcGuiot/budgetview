package org.designup.picsou.gui.components;

import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProjectItem;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class MonthSlider extends JPanel implements Disposable, ChangeSetListener {
  private JButton label = new JButton(new LabelAction());
  private JButton previous = new JButton(new PreviousMonthAction());
  private JButton next = new JButton(new NextMonthAction());

  private HyperlinkButtonUI buttonUI;

  private Key key;
  private IntegerField monthField;
  private GlobRepository repository;
  private Directory directory;

  public MonthSlider(Key key, IntegerField monthField, GlobRepository repository, Directory directory) {
    this.key = key;
    this.monthField = monthField;
    this.repository = repository;
    this.directory = directory;

    setOpaque(false);

    this.buttonUI = new HyperlinkButtonUI();
    this.buttonUI.setUnderline(false);
    this.label.setBorder(null);
    this.label.setUI(buttonUI);
    this.previous.setUI(buttonUI);
    this.previous.setBorder(null);
    this.next.setUI(buttonUI);
    this.next.setBorder(null);

    this.label.setName("month");
    this.previous.setName("previousMonth");
    this.next.setName("nextMonth");

    setLayout(new FlowLayout());
    add(previous);
    add(label);
    add(next);

    repository.addChangeListener(this);
    updateLabel();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(key)) {
      updateLabel();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(key.getGlobType())) {
      updateLabel();
    }
  }

  private void updateLabel() {
    Glob glob = repository.find(key);
    setButtonsEnabled(glob != null);
    String month = Month.getShortMonthLabel(glob.get(monthField));
    label.setText(month);
  }

  private void setButtonsEnabled(boolean b) {
    label.setEnabled(b);
    previous.setEnabled(b);
    next.setEnabled(b);
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  public void setForeground(Color color) {
    if (label == null) {
      return;
    }
    label.setForeground(color);
    previous.setForeground(color);
    next.setForeground(color);
  }

  public void setRollover(Color color) {
    buttonUI.setRolloverColor(color);
  }

  private class LabelAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      MonthChooserDialog monthChooser = new MonthChooserDialog(GuiUtils.getEnclosingWindow(MonthSlider.this), directory);
      Integer currentMonthId = repository.get(key).get(monthField);
      int selectedMonthId = monthChooser.show(currentMonthId,
                                              MonthRangeBound.NONE,
                                              CurrentMonth.getLastMonth(repository));
      if (selectedMonthId > 0) {
        repository.update(key, monthField, selectedMonthId);
      }
    }
  }

  private class PreviousMonthAction extends AbstractAction {
    private PreviousMonthAction() {
      super("<");
    }

    public void actionPerformed(ActionEvent e) {
      Glob glob = repository.get(key);
      repository.update(key, monthField, Month.previous(glob.get(monthField)));
    }
  }

  private class NextMonthAction extends AbstractAction {
    private NextMonthAction() {
      super(">");
    }

    public void actionPerformed(ActionEvent e) {
      Glob glob = repository.get(key);
      repository.update(key, monthField, Month.next(glob.get(monthField)));
    }
  }
}
