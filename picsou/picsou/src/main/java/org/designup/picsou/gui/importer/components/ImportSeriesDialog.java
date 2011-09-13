package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
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
    ImportSeriesPanel panel = new ImportSeriesPanel(localRepository, directory, series);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/components/importSeriesDialog.splits",
                                                      localRepository, directory);

    builder.add("seriesRepeatPanel", panel.getPanel());
    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(localRepository),
                               new CancelAction());
    dialog.pack();
    dialog.showCentered();
    panel.dispose();
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
