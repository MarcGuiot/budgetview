package org.designup.picsou.gui.importer.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ProjectItem;
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
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ImportSeriesPanel {
  private GlobRepository repository;
  private Directory directory;
  private Collection<Key> series;
  private GlobsPanelBuilder builder;

  public ImportSeriesPanel(GlobRepository repository, Directory directory, Collection<Key> series) {
    this.repository = repository;
    this.directory = directory;
    this.series = series;
  }


  public JPanel getPanel() {
    builder = new GlobsPanelBuilder(getClass(),
                                    "/layout/importexport/components/importSeriesPanel.splits",
                                    repository, directory);

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
    return builder.load();
  }

  public void dispose() {
    builder.dispose();
  }
}
;