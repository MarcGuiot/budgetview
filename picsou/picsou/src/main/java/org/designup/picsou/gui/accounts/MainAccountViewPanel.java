package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class MainAccountViewPanel extends AccountViewPanel {

  private JButton estimatedPositionButton = new JButton();
  private JLabel estimatedPositionDateLabel = new JLabel();
  private AmountColors amountColors;
  private EstimatedPositionDetailsDialog estimatedPositionDetailsDialog;

  public MainAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), Account.MAIN_SUMMARY_ACCOUNT_ID);

    this.estimatedPositionButton.setAction(new OpenDetailsAction(directory));
    HyperlinkButtonUI hyperlinkButtonUI = new HyperlinkButtonUI();
    hyperlinkButtonUI.setAutoHideIfDisabled(false);
    this.estimatedPositionButton.setUI(hyperlinkButtonUI);
    this.amountColors = new AmountColors(directory);
    this.estimatedPositionDetailsDialog =
      new EstimatedPositionDetailsDialog(repository, directory);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BalanceStat.TYPE) ||
            changeSet.containsUpdates(AccountPositionThreshold.THRESHOLD)) {
          updateEstimatedPosition();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(BalanceStat.TYPE) ||
            changedTypes.contains(AccountPositionThreshold.TYPE)) {
          updateEstimatedPosition();
        }
      }
    });
  }

  private static GlobMatcher createMatcher() {
    return and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
               not(fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID)),
               not(fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)));
  }

  protected void setEstimatedPositionLabels(Double amount, String date) {
    estimatedPositionButton.setText(Formatting.toString(amount));
    estimatedPositionDateLabel.setText(date);

    boolean visible = amount != null;
    estimatedPositionButton.setVisible(visible);
    estimatedPositionDateLabel.setVisible(visible);

    if (amount != null) {
      double diff = amount - AccountPositionThreshold.getValue(repository);
      estimatedPositionButton.setForeground(amountColors.get(diff));
    }
  }

  protected JLabel getEstimatedAccountPositionLabel(Glob account) {
    return Gui.createInvisibleLabel();
  }

  protected JLabel getEstimatedAccountPositionDateLabel(Glob account) {
    return Gui.createInvisibleLabel();
  }

  protected JComponent getEstimatedPositionComponent() {
    return estimatedPositionButton;
  }

  protected JComponent getEstimatedPositionDateComponent() {
    return estimatedPositionDateLabel;
  }

  protected Glob getBalanceStat(Integer lastSelectedMonthId) {
    return repository.find(Key.create(BalanceStat.TYPE, lastSelectedMonthId));
  }

  protected Double getEndOfMonthPosition(Glob balanceStat) {
    return balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION);
  }

  protected boolean showPositionThreshold() {
    return true;
  }

  private class OpenDetailsAction extends AbstractAction {

    private GlobList selectedMonths;

    private OpenDetailsAction(Directory directory) {
      directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          selectedMonths = selection.getAll(Month.TYPE);
          selectedMonths.sort(Month.ID);

          if (selectedMonths.isEmpty()) {
            setEnabled(false);
            return;
          }

          Integer monthId = selectedMonths.getLast().get(Month.ID);
          Integer currentMonthId = CurrentMonth.get(repository);
          boolean enabled = monthId >= currentMonthId;
          setEnabled(enabled);
        }
      }, Month.TYPE);

    }

    public void actionPerformed(ActionEvent e) {
      estimatedPositionDetailsDialog.show(selectedMonths);
    }
  }
}
