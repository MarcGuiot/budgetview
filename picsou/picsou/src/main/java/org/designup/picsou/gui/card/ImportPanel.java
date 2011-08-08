package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.bank.importer.SynchronizeAction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ImportPanel extends View {

  public ImportPanel(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importPanel.splits",
                                                      repository, directory);

    Action action = ImportFileAction.init(Lang.get("import"), repository, directory, null);
    JButton button = new JButton(action);
    builder.add("importButton", button);

    JButton importLabel = new JButton();
    importLabel.setModel(button.getModel());
    builder.add("importLabel", Gui.createSyncButton(button));

    Action sync = new SynchronizeAction(repository, directory);
    JButton syncButton = new JButton(sync);
    builder.add("synchroButton", syncButton);

    JButton syncLabel = new JButton();
    syncLabel.setModel(syncButton.getModel());
    builder.add("synchroLabel", Gui.createSyncButton(syncButton));



    final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
    importSignpost.attach(button);

    parentBuilder.add("importPanel", builder);
  }
}