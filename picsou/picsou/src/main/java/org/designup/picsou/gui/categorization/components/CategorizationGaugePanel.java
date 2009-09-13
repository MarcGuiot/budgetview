package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.SetFieldValueAction;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Set;

public class CategorizationGaugePanel implements ChangeSetListener {
  private GlobRepository repository;
  private Directory directory;

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
    repository.addChangeListener(this);
//    update();
  }

  public void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationGaugePanel.splits",
                                                      repository, directory);

    gauge = builder.add("gauge", new Gauge()).getComponent();
    levelLabel = builder.add("level", new JLabel()).getComponent();
    panel = builder.add("panel", new JPanel()).getComponent();
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

  public Action getHideProgressMessageAction() {
    return new SetFieldValueAction(UserPreferences.KEY, UserPreferences.SHOW_CATEGORIZATION_HELP_MESSAGE, false, repository);
  }

  public void update() {
    GlobList transactions =
      repository.getAll(Transaction.TYPE,
                        not(fieldEquals(Transaction.PLANNED, true)));

    double total = 0;
    double uncategorized = 0;
    for (Glob transaction : transactions) {
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
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null){
      return;
    }
    Boolean preference = preferences.isTrue(UserPreferences.SHOW_CATEGORIZATION_HELP_MESSAGE);
    if (!Boolean.TRUE.equals(preference)) {
      progressMessage.setVisible(false);
      return;
    }

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
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(UserPreferences.KEY)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(UserPreferences.TYPE)) {
      update();
    }
  }
}
