package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.signpost.actions.SetSignpostStatusAction;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesBudgetChangeSetVisitor;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SeriesAmountEditionDialog {

  private SeriesAmountEditionPanel editionPanel;
  private JLabel seriesNameLabel = new JLabel();
  private PicsouDialog dialog;

  private LocalGlobRepository localRepository;
  private GlobRepository parentRepository;
  private Directory localDirectory;

  private SeriesEditionDialog seriesEditionDialog;
  private GlobStringifier seriesNameStringifier;

  public SeriesAmountEditionDialog(GlobRepository parentRepository,
                                   Directory parentDirectory,
                                   SeriesEditionDialog seriesEditionDialog) {

    this.parentRepository = parentRepository;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository).get();

    localDirectory = new DefaultDirectory(parentDirectory);
    localDirectory.add(new SelectionService());

    this.seriesEditionDialog = seriesEditionDialog;

    this.seriesNameStringifier = parentDirectory.get(DescriptionService.class).getStringifier(Series.TYPE);

    createDialog();
  }

  private void createDialog() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesAmountEditionDialog.class,
                                                      "/layout/series/seriesAmountEditionDialog.splits",
                                                      localRepository, localDirectory);

    builder.add("seriesName", seriesNameLabel);

    editionPanel = new SeriesAmountEditionPanel(localRepository, localDirectory, new SeriesEditor());
    builder.add("editionPanel", editionPanel.getPanel());

    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);

    OkAction okAction = new OkAction();
    editionPanel.setOkAction(okAction);
    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.setAutoFocusOnOpen(editionPanel.getFocusComponent());
    dialog.addOnWindowClosedAction(new SetSignpostStatusAction(SignpostStatus.SERIES_AMOUNT_CLOSED,
                                                               SignpostStatus.SERIES_AMOUNT_SHOWN,
                                                               parentRepository));
    dialog.pack();
  }

  public void show(Glob series, Set<Integer> months) {
//    if (series != null && series.isTrue(Series.IS_MIRROR)) {
//      series = parentRepository.findLinkTarget(series, Series.MIRROR_SERIES);
//    }

    seriesNameLabel.setText(seriesNameStringifier.toString(series, localRepository));

    editionPanel.clear();
    loadGlobs(series);
    editionPanel.selectMonths(months);
    localDirectory.get(SelectionService.class).select(series);

    GuiUtils.showCentered(dialog);
  }

  private void loadGlobs(Glob series) {
    GlobList globsToLoad = new GlobList();
    globsToLoad.add(series);
    globsToLoad.addAll(getLinkedAccounts(series));
    globsToLoad.addAll(getBudgets(series));
    globsToLoad.addAll(parentRepository.getAll(Month.TYPE));
    globsToLoad.addAll(parentRepository.getAll(CurrentMonth.TYPE));
    localRepository.reset(globsToLoad, Series.TYPE, SeriesBudget.TYPE, Account.TYPE, Month.TYPE, CurrentMonth.TYPE);
  }

  private GlobList getLinkedAccounts(Glob series) {
    GlobList accounts = new GlobList();
    accounts.addNotNull(parentRepository.findLinkTarget(series, Series.FROM_ACCOUNT),
                        parentRepository.findLinkTarget(series, Series.TO_ACCOUNT));
    return accounts;
  }

  private GlobList getBudgets(Glob series) {
    GlobList globs = parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
    Integer value = series.get(Series.MIRROR_SERIES);
    if (value != null) {
      globs.addAll(parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, value).getGlobs());
    }
    return globs;
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {

//      ChangeSet changeSet = localRepository.getCurrentChanges();
//      localRepository.startChangeSet();
//      try {
//        changeSet.safeVisit(SeriesBudget.TYPE, new UpdateMirrorSeriesBudgetChangeSetVisitor(localRepository));
//      }
//      finally {
//        localRepository.completeChangeSet();
//      }

      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.rollback();
      dialog.setVisible(false);
    }
  }

  private class SeriesEditor implements SeriesAmountEditionPanel.SeriesEditorAccess {

    public void openSeriesEditor(Key seriesKey, Set<Integer> selectedMonthIds) {
      localRepository.rollback();
      dialog.setVisible(false);
      seriesEditionDialog.show(parentRepository.get(seriesKey), selectedMonthIds);

    }
  }
}
