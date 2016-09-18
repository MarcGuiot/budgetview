package com.budgetview.desktop.importer.series;

import com.budgetview.desktop.components.BorderlessTextField;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.model.ImportedSeries;
import com.budgetview.model.Series;
import com.budgetview.model.SubSeries;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import static org.globsframework.model.utils.GlobMatchers.keyIn;

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
    System.out.println("ImportSeriesDialog.show");
    dialog = PicsouDialog.create(this, parent, directory);
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(ImportedSeries.TYPE, Series.TYPE, SubSeries.TYPE, BudgetArea.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/components/importSeriesDialog.splits",
                                                      localRepository, directory);

    builder.addRepeat("series", ImportedSeries.TYPE, keyIn(importedSeriesKeys),
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
      SeriesImporter.createSeries(importedSeriesKeys, localRepository);
      localRepository.commitChanges(true);
    }
    return importSeries;
  }

  private class ValidateAction extends AbstractAction {
    private LocalGlobRepository repository;

    public ValidateAction(LocalGlobRepository repository) {
      super(Lang.get("import.series.ok"));
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      System.out.println("ImportSeriesDialog.ValidateAction.actionPerformed");
      importSeries = true;
      dialog.setVisible(false);
    }
  }

  private class DoNotImportAction extends AbstractAction {
    public DoNotImportAction() {
      super(Lang.get("import.series.doNotImport"));
    }

    public void actionPerformed(ActionEvent e) {
      System.out.println("ImportSeriesDialog.DoNotImportAction.actionPerformed");
      importSeries = false;
      dialog.setVisible(false);
    }
  }
}
