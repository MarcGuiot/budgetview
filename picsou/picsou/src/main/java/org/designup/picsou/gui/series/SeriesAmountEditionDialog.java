package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.series.utils.SeriesAmountLabelStringifier;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import static org.globsframework.model.utils.GlobFunctors.update;
import static org.globsframework.model.utils.GlobMatchers.fieldStrictlyGreaterThan;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SeriesAmountEditionDialog {

  private LocalGlobRepository localRepository;
  private Directory directory;
  private AmountEditor amountEditor;
  private SelectionService selectionService;
  private PicsouDialog dialog;
  private JCheckBox propagationCheckBox;
  private GlobRepository parentRepository;

  private Key currentSeries;
  private Integer maxMonth;

  public SeriesAmountEditionDialog(GlobRepository parentRepository, Directory parentDirectory) {

    this.parentRepository = parentRepository;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository).get();

    Directory directory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    directory.add(selectionService);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesAmountEditionDialog.class,
                                                      "/layout/seriesAmountEditionDialog.splits",
                                                      localRepository, directory);

    builder.addLabel("dateLabel", SeriesBudget.TYPE, new SeriesAmountLabelStringifier());

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, localRepository, directory, true, 0.0);
    builder.add("amountEditor", amountEditor.getNumericEditor());
    builder.add("positiveAmounts", amountEditor.getPositiveRadio());
    builder.add("negativeAmounts", amountEditor.getNegativeRadio());

    propagationCheckBox = new JCheckBox();
    builder.add("propagate", propagationCheckBox);

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    OkAction okAction = new OkAction();
    amountEditor.addAction(okAction);
    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.setAutoFocusOnOpen(amountEditor.getNumericEditor().getComponent());
    dialog.pack();
  }

  public void show(Glob series, Set<Integer> months) {
    this.currentSeries = series.getKey();
    this.maxMonth = Utils.max(months);
    loadGlobs(series);
    select(series, months);
    propagationCheckBox.setSelected(true);
    amountEditor.selectAll();
    GuiUtils.showCentered(dialog);
  }

  private void loadGlobs(Glob series) {
    GlobList globsToLoad = new GlobList();
    globsToLoad.add(series);
    globsToLoad.addAll(getBudgets(series));
    localRepository.reset(globsToLoad, Series.TYPE, SeriesBudget.TYPE);
  }

  private GlobList getBudgets(Glob series) {
    return parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
  }

  private void select(Glob series, Set<Integer> monthIds) {
    GlobList selection = new GlobList();
    Integer seriesId = series.get(Series.ID);
    for (Integer monthId : monthIds) {
      selection.addAll(
        localRepository
          .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
          .findByIndex(SeriesBudget.MONTH, monthId)
          .getGlobs());
    }
    selectionService.select(selection, SeriesBudget.TYPE);
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      Glob series = localRepository.get(currentSeries);
      if (series.isTrue(Series.IS_AUTOMATIC) && localRepository.containsChanges()) {
        localRepository.update(currentSeries, Series.IS_AUTOMATIC, false);
      }
      if (propagationCheckBox.isSelected()) {
        final Double amount = amountEditor.getValue();
        localRepository.safeApply(SeriesBudget.TYPE,
                                  fieldStrictlyGreaterThan(SeriesBudget.MONTH, maxMonth),
                                  update(SeriesBudget.AMOUNT, amount));
      }

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

}
