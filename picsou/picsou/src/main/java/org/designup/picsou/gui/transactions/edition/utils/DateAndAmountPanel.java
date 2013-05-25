package org.designup.picsou.gui.transactions.edition.utils;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.description.stringifiers.MonthFieldListStringifier;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.*;

import static org.globsframework.model.FieldValue.value;

public class DateAndAmountPanel {
  private final Window dialog;
  private GlobRepository repository;
  private Directory localDirectory;
  private SelectionService localSelectionService = new SelectionService();

  private JPanel panel;
  private AmountEditor amountEditor;
  private JTextField amountField;
  private GlobNumericEditor dayEditor;
  private JTextField dayField;

  private boolean enabled;

  public DateAndAmountPanel(Window dialog, GlobRepository repository, Directory parentDirectory) {
    this.dialog = dialog;
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(parentDirectory);
    this.localDirectory.add(localSelectionService);
    createPanel();
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactions/utils/dateAndAmountPanel.splits",
                            repository, localDirectory);

    amountEditor = new AmountEditor(Transaction.AMOUNT, repository, localDirectory, false, null)
      .update(false, false);
    amountField = amountEditor.getNumericEditor().getComponent();
    builder.add("amountEditor", amountEditor.getPanel());

    dayEditor = builder.addEditor("day", Transaction.DAY);
    dayField = dayEditor.getComponent();

    builder.addButton("month",
                      Transaction.TYPE,
                      new MonthFieldListStringifier(Transaction.MONTH),
                      new EditMonthCallback());

    panel = builder.load();
  }

  public void show(GlobList transactions) {
    localSelectionService.select(transactions, Transaction.TYPE);
    setEnabled(true);
  }

  public void hide() {
    localSelectionService.clear(Transaction.TYPE);
    setEnabled(false);
  }

  private void setEnabled(boolean enabled) {
    this.enabled = enabled;
    panel.setVisible(enabled);
  }

  public void apply() {
    amountField.postActionEvent();
    dayField.postActionEvent();
  }

  public boolean validateFields() {
    if (!enabled) {
      return true;
    }

    if (Strings.isNullOrEmpty(dayField.getText())) {
      ErrorTip.showLeft(dayField,
                        Lang.get("transaction.edition.day.error"),
                        localDirectory);
      return false;
    }

    if (Strings.isNullOrEmpty(amountField.getText())) {
      ErrorTip.showLeft(amountField,
                        Lang.get("transaction.edition.amount.error"),
                        localDirectory);
      return false;
    }

    return true;
  }

  public JPanel getPanel() {
    return panel;
  }

  private class EditMonthCallback implements GlobListFunctor {

    public void run(GlobList list, GlobRepository repository) {
      MonthChooserDialog monthChooser = new MonthChooserDialog(dialog, localDirectory);
      GlobList selectedTransactions = localSelectionService.getSelection(Transaction.TYPE);
      if (selectedTransactions.isEmpty()) {
        throw new UnexpectedApplicationState("Cannot call EditMonthCallback without a selection");
      }
      Integer currentMonthId = selectedTransactions.getFirst().get(Transaction.MONTH);
      int selectedMonthId = monthChooser.show(currentMonthId,
                                              MonthRangeBound.LOWER,
                                              CurrentMonth.getLastMonth(repository));
      if (selectedMonthId < 0) {
        return;
      }
      updateMonth(repository, selectedTransactions, selectedMonthId);
    }

    private void updateMonth(GlobRepository repository, GlobList transactions, Integer currentMonth) {
      repository.startChangeSet();
      try {
        for (Glob transaction : transactions) {
          repository.update(transaction.getKey(),
                            value(Transaction.MONTH, currentMonth),
                            value(Transaction.BUDGET_MONTH, currentMonth),
                            value(Transaction.POSITION_MONTH, currentMonth),
                            value(Transaction.BANK_MONTH, currentMonth));
        }
      }
      finally {
        repository.completeChangeSet();
      }
    }
  }
}