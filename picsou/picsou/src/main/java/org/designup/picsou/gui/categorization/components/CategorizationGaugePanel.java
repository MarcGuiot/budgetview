package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.EditorKit;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

public class CategorizationGaugePanel implements GlobSelectionListener, ChangeSetListener {
  private GlobRepository repository;
  private Directory directory;

  private Set<Integer> selectedMonthIds = Collections.emptySet();

  private DecimalFormat format = new DecimalFormat("0");

  private JLabel levelLabel;
  private Gauge gauge;
  private JPanel panel;
  private JEditorPane progressMessage;

  public CategorizationGaugePanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    createPanel();
    createProgressMessage();
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
    repository.addChangeListener(this);
  }

  public void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationGaugePanel.splits",
                                                      repository, directory);

    gauge = builder.add("gauge", new Gauge());
    levelLabel = builder.add("level", new JLabel());
    panel = builder.add("panel", new JPanel());
    builder.load();

    panel.setVisible(false);
  }

  private void createProgressMessage() {
    progressMessage = new JEditorPane();
    progressMessage.setContentType("text/html");

    PicsouColors.installLinkColor(progressMessage, "mainpanel", "mainpanel.message.link", directory);

    progressMessage.setVisible(false);
  }

  public JPanel getPanel() {
    return panel;
  }

  public JEditorPane getProgressMessage() {
    return progressMessage;
  }

  public void update() {
    GlobList transactionsForSelectedMonths =
      repository.getAll(Transaction.TYPE,
                        and(not(fieldEquals(Transaction.PLANNED, true)),
                            fieldIn(Transaction.MONTH, selectedMonthIds)));

    double total = 0;
    double uncategorized = 0;
    for (Glob transaction : transactionsForSelectedMonths) {
      double amount = Math.abs(transaction.get(Transaction.AMOUNT));
      total += amount;
      if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        uncategorized += amount;
      }
    }

    double percentage = total == 0 ? 1 : uncategorized / total;
    if (percentage > 0 && percentage < 0.01) {
      percentage = 0.01;
    }

    updatePanel(total, percentage);
    updateProgressMessage(total, percentage);
  }

  private void updatePanel(double total, double percentage) {
    if ((total == 0) || (percentage == 0)) {
      panel.setVisible(false);
    }
    else {
      panel.setVisible(true);
      gauge.setValues(percentage, 1.0);
      levelLabel.setText(format.format(100 * percentage) + "%");
    }
  }

  private void updateProgressMessage(double total, double percentage) {
    if (total == 0) {
      progressMessage.setVisible(false);
    }
    else if (percentage == 0) {
      progressMessage.setVisible(true);
      progressMessage.setText(Lang.get("categorization.gauge.message.complete"));
    }
    else if (percentage <= 0.1) {
      progressMessage.setVisible(true);
      progressMessage.setText(Lang.get("categorization.gauge.message.quasi"));
    }
    else {
      progressMessage.setVisible(false);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE)) {
      update();
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    update();
  }
}
