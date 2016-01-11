package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.components.BorderlessTextField;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ImportedSeries;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ImportSeriesDialog {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;
  private boolean importSeries = false;
  private LocalGlobRepository localRepository;

  public ImportSeriesDialog(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean show(Set<Key> importedSeriesKeys) {
    dialog = PicsouDialog.create(this, parent, directory);
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(ImportedSeries.TYPE, Series.TYPE, SubSeries.TYPE, BudgetArea.TYPE)
      .get();

    updateToKnownSeries(importedSeriesKeys, localRepository);
    if (importedSeriesKeys.isEmpty()) {
      localRepository.commitChanges(true);
      return true;
    }

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/components/importSeriesDialog.splits",
                                                      localRepository, directory);

    builder.addRepeat("series", ImportedSeries.TYPE,
                      GlobMatchers.keyIn(importedSeriesKeys),
                      new GlobFieldComparator(ImportedSeries.NAME), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, Glob item) {

        JTextField nameField = GlobTextEditor.init(ImportedSeries.NAME, localRepository, directory)
          .forceSelection(item.getKey())
          .setName("series_" + item.get(ImportedSeries.NAME))
          .getComponent();
        BorderlessTextField.install(nameField);
        cellBuilder.add("name", nameField);

        GlobLinkComboEditor comboView = GlobLinkComboEditor.init(ImportedSeries.BUDGET_AREA, localRepository, directory)
          .setEmptyOptionLabel(Lang.get("import.series.uncategorized"))
          .setFilter(fieldIn(BudgetArea.ID,
                             BudgetArea.INCOME.getId(),
                             BudgetArea.RECURRING.getId(),
                             BudgetArea.VARIABLE.getId()))
          .setComparator(new GlobFieldComparator(BudgetArea.ID))
          .setName("choice_" + item.get(ImportedSeries.NAME))
          .forceSelection(item.getKey());
        cellBuilder.add("choice", comboView.getComponent());
      }
    });

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(localRepository),
                               new DoNotImportAction());
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
    if (importSeries) {
      createSeries(importedSeriesKeys, localRepository);
      localRepository.commitChanges(true);
    }
    return importSeries;
  }

  private void createSeries(Set<Key> importedSeriesKeys, LocalGlobRepository localRepository) {
    for (Key importedSeriesKey : importedSeriesKeys) {
      Glob importedSeries = localRepository.get(importedSeriesKey);
      Integer budgetArea = importedSeries.get(ImportedSeries.BUDGET_AREA);
      String name = importedSeries.get(ImportedSeries.NAME);
      if (Strings.isNotEmpty(name)) {
        String[] splitted = name.split(":");
        String seriesName = splitted[0];
        String subSeriesName = splitted.length == 2 ? splitted[1] : null;
        if (budgetArea != null) {
          Glob subSeries = null;
          Glob series =
            localRepository.getAll(Series.TYPE,
                                   GlobMatchers.and(
                                     GlobMatchers.fieldEquals(Series.BUDGET_AREA, budgetArea),
                                     GlobMatchers.fieldEquals(Series.NAME, seriesName))).getFirst();
          if (series != null) {
            if (subSeriesName != null) {
              subSeries = localRepository.getAll(SubSeries.TYPE, GlobMatchers.and(
                GlobMatchers.fieldEquals(SubSeries.SERIES, series.get(Series.ID)),
                GlobMatchers.fieldEquals(SubSeries.NAME, subSeriesName)
              )).getFirst();
              if (subSeries == null) {
                subSeries = localRepository.create(SubSeries.TYPE,
                                                   FieldValue.value(SubSeries.NAME, subSeriesName),
                                                   FieldValue.value(SubSeries.SERIES, series.get(Series.ID)));
              }
            }
          }
          else {
            series = localRepository.create(Series.TYPE,
                                            FieldValue.value(Series.IS_AUTOMATIC,
                                                             BudgetArea.RECURRING.getId().equals(importedSeries.get(ImportedSeries.BUDGET_AREA))),
                                            FieldValue.value(Series.BUDGET_AREA, importedSeries.get(ImportedSeries.BUDGET_AREA)),
                                            FieldValue.value(Series.NAME, seriesName));
            if (subSeriesName != null) {
              subSeries = localRepository.create(SubSeries.TYPE,
                                                 FieldValue.value(SubSeries.NAME, subSeriesName),
                                                 FieldValue.value(SubSeries.SERIES, series.get(Series.ID)));
            }
          }
          localRepository.update(importedSeriesKey,
                                 FieldValue.value(ImportedSeries.SERIES, series.get(Series.ID)),
                                 FieldValue.value(ImportedSeries.SUB_SERIES, subSeries != null ? subSeries.get(SubSeries.ID) : null));
        }
      }
    }
  }

  private void updateToKnownSeries(Set<Key> importedSeries, LocalGlobRepository localRepository) {
    for (Iterator<Key> it = importedSeries.iterator(); it.hasNext(); ) {
      Key importedSerie = it.next();
      Glob glob = localRepository.get(importedSerie);
      String name = glob.get(ImportedSeries.NAME);
      String[] splited = name.split(":");
      Integer seriesId = null;
      Integer subSeriesId = null;
      Integer budgetArea = null;
      boolean duplicate = false;
      if (splited.length == 1) {
        GlobList series = localRepository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.NAME, splited[0]));
        for (Glob sery : series) {
          if (seriesId != null) {
            duplicate = true;
          }
          seriesId = sery.get(Series.ID);
          subSeriesId = null;
          budgetArea = sery.get(Series.BUDGET_AREA);
        }
      }
      else if (splited.length == 2) {
        GlobList series = localRepository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.NAME, splited[0]));
        for (Glob sery : series) {
          GlobList subSeries = localRepository.findLinkedTo(sery, SubSeries.SERIES);
          for (Glob subSery : subSeries) {
            if (subSery.get(SubSeries.NAME).equals(splited[1])) {
              if (seriesId != null) {
                duplicate = true;
              }
              seriesId = sery.get(Series.ID);
              subSeriesId = subSery.get(SubSeries.ID);
              budgetArea = sery.get(Series.BUDGET_AREA);
            }
          }
        }
      }
      if (!duplicate && budgetArea != null) {
        localRepository.update(glob.getKey(),
                               FieldValue.value(ImportedSeries.SERIES, seriesId),
                               FieldValue.value(ImportedSeries.SUB_SERIES, subSeriesId),
                               FieldValue.value(ImportedSeries.BUDGET_AREA, budgetArea));

        it.remove();
      }
    }
  }

  private class ValidateAction extends AbstractAction {
    private LocalGlobRepository repository;

    public ValidateAction(LocalGlobRepository repository) {
      super(Lang.get("import.series.ok"));
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      importSeries = true;
      dialog.setVisible(false);
    }
  }

  private class DoNotImportAction extends AbstractAction {
    public DoNotImportAction() {
      super(Lang.get("import.series.doNotImport"));
    }

    public void actionPerformed(ActionEvent e) {
      importSeries = false;
      dialog.setVisible(false);
    }
  }
}
