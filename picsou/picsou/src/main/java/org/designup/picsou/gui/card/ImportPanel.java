package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.utils.Lang;
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


    Action action = ImportFileAction.init(Lang.get("importPanel.button"), repository, directory, null);
    JButton button = new JButton(action);
    builder.add("import", button);
    
    final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
    importSignpost.attach(button);

    parentBuilder.add("importPanel", builder);
  }
}