package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ImportSeriesDialog {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;
  private boolean importSeries = false;

  public ImportSeriesDialog(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean show(Set<Key> series) {
    dialog = PicsouDialog.create(parent, directory);
    LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Series.TYPE, BudgetArea.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/components/importSeriesDialog.splits",
                                                      localRepository, directory);

    builder.addRepeat("series", Series.TYPE,
                      GlobMatchers.keyIn(series),
                      new GlobFieldComparator(Series.NAME), new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, Glob item) {
        JTextField name = GlobTextEditor.init(Series.NAME, repository, directory)
          .forceSelection(item.getKey())
          .setName("series_" + item.get(Series.NAME))
          .getComponent();
        cellBuilder.add("name", name);

        GlobLinkComboEditor comboView = GlobLinkComboEditor.init(Series.BUDGET_AREA, repository, directory)
          .setEmptyOptionLabel(Lang.get("import.series.uncategorized"))
          .setFilter(GlobMatchers.fieldIn(BudgetArea.ID, BudgetArea.INCOME.getId(),
                                          BudgetArea.RECURRING.getId(),
                                          BudgetArea.VARIABLE.getId()))
          .setName("choice_" + item.get(Series.NAME))
          .forceSelection(item.getKey());
        cellBuilder.add("choice", comboView.getComponent());
      }
    });

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(localRepository),
                               new CancelAction());
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
    return importSeries;
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
      repository.commitChanges(true);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("import.series.cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      importSeries = false;
      dialog.setVisible(false);
    }
  }
}
