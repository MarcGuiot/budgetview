package org.designup.picsou.gui.budget.dialogs;

import org.designup.picsou.gui.budget.wizard.PositionThresholdIndicator;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PositionThresholdDialog {

  private Directory directory;
  private LocalGlobRepository localRepository;
  private PicsouDialog dialog;
  private Directory parentDirectory;
  private JTextField thresholdField;
  private JScrollPane scrollPane = new JScrollPane();

  public PositionThresholdDialog(GlobRepository repository, Directory parentDirectory) {
    this(parentDirectory.get(JFrame.class), repository, parentDirectory);
  }

  public PositionThresholdDialog(Window owner, GlobRepository repository, Directory parentDirectory) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(BudgetStat.TYPE)
        .copy(AccountPositionThreshold.TYPE)
        .copy(Month.TYPE)
        .copy(CurrentMonth.TYPE)
        .get();

    this.parentDirectory = parentDirectory;
    this.directory = createDirectory(parentDirectory);
    createDialog(owner);
  }

  public void createDialog(Window owner) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/budget/dialogs/positionThresholdDialog.splits", localRepository, directory);

    builder.add("textTop", Gui.createHelpTextComponent("budgetWizard/threshold1.html"));
    builder.add("textBottom", Gui.createHelpTextComponent("budgetWizard/threshold2.html"));

    builder.add("scroll", scrollPane);

    builder.addLabel("estimatedPosition", BudgetStat.TYPE,
                     new EspectedPositionStringifier());

    builder.add("thresholdIndicator",
                new PositionThresholdIndicator(localRepository, directory,
                                               "budgetSummaryDialog.threshold.top",
                                               "budgetSummaryDialog.threshold.bottom",
                                               "budgetSummaryDialog.threshold.border"));

    thresholdField = builder.addEditor("thresholdField", AccountPositionThreshold.THRESHOLD)
      .forceSelection(AccountPositionThreshold.KEY)
      .setNotifyOnKeyPressed(true)
      .setValueForNull(0.00)
      .getComponent();

    builder.addLabel("thresholdMessage", BudgetStat.TYPE, new ThresholdStringifier())
      .setUpdateMatcher(ChangeSetMatchers.changesForKey(AccountPositionThreshold.KEY));

    JPanel panel = builder.load();
    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButtons(panel, new OkAction(), new CancelAction(dialog));
    dialog.pack();
  }

  public void show() {
    updateBeforeDisplay();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        thresholdField.requestFocus();
      }
    });
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GuiUtils.scrollToTop(scrollPane);
      }
    });

    GuiUtils.showCentered(dialog);
  }

  public void updateBeforeDisplay() {
    localRepository.rollback();

    GlobList selectedMonths = getSelectedMonths();
    selectStats(selectedMonths);
  }

  private GlobList getSelectedMonths() {
    return parentDirectory.get(SelectionService.class).getSelection(Month.TYPE).sort(Month.ID);
  }

  private void selectStats(GlobList selectedMonths) {
    GlobList stats = new GlobList();
    for (Glob month : selectedMonths) {
      stats.add(localRepository.find(Key.create(BudgetStat.TYPE, month.get(Month.ID))));
    }
    directory.get(SelectionService.class).select(stats, BudgetStat.TYPE);
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  private Glob getLastBudgetStat(GlobList list) {
    list.sort(BudgetStat.MONTH);
    return list.getLast();
  }

  private class EspectedPositionStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(list);
      if (budgetStat == null) {
        return "";
      }
      return Lang.get("budgetThresholdPage.position",
                      Formatting.toString(budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION)));
    }
  }

  private class ThresholdStringifier implements GlobListStringifier {
    public String toString(GlobList stats, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(stats);
      if (budgetStat == null) {
        return "";
      }
      Double position = budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
      Double threshold = AccountPositionThreshold.getValue(repository);
      double diff = Amounts.diff(position, threshold);
      if (diff < 0) {
        return Lang.get("budgetThresholdPage.negative");
      }
      else if (diff > 0) {
        return Lang.get("budgetThresholdPage.positive");
      }
      return Lang.get("budgetThresholdPage.equal");
    }
  }

  private class OkAction extends AbstractAction {
    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }
}