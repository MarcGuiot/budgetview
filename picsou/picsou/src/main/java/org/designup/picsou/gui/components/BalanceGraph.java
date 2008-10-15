package org.designup.picsou.gui.components;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class BalanceGraph extends JPanel implements GlobSelectionListener {
  private Color receivedColorTop = Color.GREEN.darker();
  private Color receivedColorBottom = Color.GREEN.brighter();
  private Color spentColorTop = Color.RED.darker();
  private Color spentColorBottom = Color.RED.brighter();
  private Color borderColor = Color.GRAY;
  private double incomePercent = 0.0;
  private double expensesPercent = 0.0;
  private GlobRepository repository;
  private Directory directory;

  public BalanceGraph(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    setOpaque(false);
    final SelectionService selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Month.TYPE);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BalanceStat.TYPE)) {
          update(selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID));
      }
    });
  }

  public void selectionUpdated(GlobSelection selection) {
    update(selection.getAll(Month.TYPE).getValueSet(Month.ID));
  }

  private void update(Set<Integer> monthIds) {
    double received = 0;
    double spent = 0;
    for (Glob stat : repository.getAll(BalanceStat.TYPE, GlobMatchers.fieldIn(BalanceStat.MONTH_ID, monthIds))) {
      received += stat.get(BalanceStat.INCOME) + stat.get(BalanceStat.INCOME_REMAINING);
      spent += stat.get(BalanceStat.EXPENSE) + stat.get(BalanceStat.EXPENSE_REMAINING);
    }

    received = Math.abs(received);
    spent = Math.abs(spent);

    double max = Math.max(received, spent);
    if (max == 0) {
      incomePercent = 0;
      expensesPercent = 0;
      return;
    }

    incomePercent = received / max;
    expensesPercent = spent / max;

    repaint();
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;

    if ((incomePercent == 0) && (expensesPercent == 0)) {
      return;
    }

    int h = getHeight() - 1;
    int w = getWidth() - 1;

    int middle = w / 2;
    int incomeHeight = (int)(h * incomePercent);
    int spentHeight = (int)(h * expensesPercent);

    g2.setPaint(new GradientPaint(0, 0, receivedColorTop,
                                  0, incomeHeight, receivedColorBottom));
    g2.fillRect(0, h - incomeHeight, middle, incomeHeight);
    g2.setColor(borderColor);
    g2.drawRect(0, h - incomeHeight, middle, incomeHeight);

    g2.setColor(spentColorTop);
    g2.setPaint(new GradientPaint(0, 0, spentColorTop,
                                  0, spentHeight, spentColorBottom));
    g2.fillRect(middle, h - spentHeight, middle, spentHeight);
    g2.setColor(borderColor);
    g2.drawRect(middle, h - spentHeight, middle, spentHeight);
  }

  public void setSpentColorTop(Color spentColorTop) {
    this.spentColorTop = spentColorTop;
  }

  public void setSpentColorBottom(Color spentColorBottom) {
    this.spentColorBottom = spentColorBottom;
  }

  public void setReceivedColorTop(Color receivedColorTop) {
    this.receivedColorTop = receivedColorTop;
  }

  public void setReceivedColorBottom(Color receivedColorBottom) {
    this.receivedColorBottom = receivedColorBottom;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public double getIncomePercent() {
    return incomePercent;
  }

  public double getExpensesPercent() {
    return expensesPercent;
  }
}
