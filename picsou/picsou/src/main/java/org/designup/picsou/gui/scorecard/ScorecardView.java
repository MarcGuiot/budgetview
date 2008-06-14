package org.designup.picsou.gui.scorecard;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.transactions.TransactionSelection;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.PicsouDescriptionService;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class ScorecardView extends View implements GlobSelectionListener {

  private JEditorPane editorPane = new JEditorPane();
  private TransactionSelection transactionSelection;

  private Color defaultColor;
  private Color incomeColor;
  private Color expensesColor;

  public ScorecardView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    transactionSelection.addListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("scorecard", editorPane);
  }

  public void colorsChanged(ColorSource colorSource) {
    super.colorsChanged(colorSource);
    defaultColor = colorSource.get(PicsouColors.SCORECARD_TEXT);
    incomeColor = colorSource.get(PicsouColors.SCORECARD_INCOME);
    expensesColor = colorSource.get(PicsouColors.SCORECARD_EXPENSES);
  }

  public void selectionUpdated(GlobSelection selection) {
    update(transactionSelection.getSelectedMonthStats());
  }

  private void update(GlobList monthStats) {
    StringBuilder builder = new StringBuilder();
    builder.append("<html><table border='0' style='color:white'>");

    double income = sum(monthStats, MonthStat.INCOME);
    addRow("income", income, 0, defaultColor, builder);

    double expenses = sum(monthStats, MonthStat.EXPENSES);
    addRow("expenses", expenses, 0, defaultColor, builder);

    double cashflow = income - expenses;
    addRow("cashflow", cashflow, 0, getColor(cashflow), builder);

    addRow("dispensable", sum(monthStats, MonthStat.DISPENSABLE), 10, defaultColor, builder);

    builder.append("</table></html>");

    editorPane.setText(builder.toString());
  }

  private void addRow(String key, double value, int topMargin, Color color, StringBuilder builder) {
    String marginTop = "margin-top:" + topMargin + "px;";
    String weight = "font-weight: bold;";
    String textColor = "color:#" + Colors.toString(color) + ";";
    builder.append("<tr>" +
                   "<td style='text-align:right;margin-right=10px:font-size:9pt;" + marginTop + "'>" +
                   Lang.get(key) +
                   "</td>" +
                   "<td style='text-align:right;" + textColor + marginTop + weight + "'>" +
                   PicsouDescriptionService.DECIMAL_FORMAT.format(value) + " " + Gui.EURO +
                   "</td>" +
                   "</tr>");
  }

  private Color getColor(double cashflow) {
    if (cashflow < 0) {
      return expensesColor;
    }
    if (cashflow > 0) {
      return incomeColor;
    }
    return defaultColor;
  }

  private double sum(GlobList monthStats, DoubleField field) {
    double sum = 0;
    for (Glob stat : monthStats) {
      sum += stat.get(field);
    }
    return sum;
  }
}
