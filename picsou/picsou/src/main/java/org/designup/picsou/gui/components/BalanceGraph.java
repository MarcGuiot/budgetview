package org.designup.picsou.gui.components;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
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
  private String messagePrefix = "balanceGraph.tooltip";
  private GlobType statType;
  private LinkField monthField;
  private DoubleField inRemainingField;
  private DoubleField inField;
  private DoubleField outField;
  private DoubleField outRemainingField;

  public BalanceGraph(GlobRepository repository, Directory directory, final GlobType statType, final LinkField monthField,
                      final DoubleField inField, final DoubleField inRemainingField,
                      final DoubleField outField, final DoubleField outRemainingField) {
    this.repository = repository;
    this.statType = statType;
    this.monthField = monthField;
    this.inField = inField;
    this.inRemainingField = inRemainingField;
    this.outField = outField;
    this.outRemainingField = outRemainingField;
    setOpaque(false);
    final SelectionService selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, statType);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BalanceGraph.this.statType)) {
          update(selectionService.getSelection(statType));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(selectionService.getSelection(statType));
      }
    });
  }

  public void selectionUpdated(GlobSelection selection) {
    update(selection.getAll(statType));
  }

  private void update(GlobList stats) {
    double income = 0;
    double expenses = 0;
    for (Glob stat : stats) {
      income += stat.get(inField) + stat.get(inRemainingField);
      expenses += stat.get(outField) + stat.get(outRemainingField);
    }

    setToolTipText(Lang.get(messagePrefix,
                            Formatting.toString(income),
                            Formatting.toString(-expenses)));

    income = Math.abs(income);
    expenses = Math.abs(expenses);


    double max = Math.max(income, expenses);
    if (max == 0) {
      incomePercent = 0;
      expensesPercent = 0;
      return;
    }

    incomePercent = income / max;
    expensesPercent = expenses / max;

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

  public void setMessagePrefix(String messagePrefix) {
    this.messagePrefix = messagePrefix;
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
