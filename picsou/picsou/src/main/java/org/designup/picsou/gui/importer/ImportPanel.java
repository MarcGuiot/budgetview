package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ImportPanel extends View {

  private boolean showSignpost;

  public ImportPanel(boolean showSignpost, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.showSignpost = showSignpost;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importPanel.splits", repository, directory);

    JButton button = new JButton(ImportFileAction.init(Lang.get("importPanel.import.label"), repository, directory, null));
    builder.add("importButton", button);

    if (showSignpost) {
      final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
      importSignpost.attach(button);
    }

    parentBuilder.add("importPanel", builder);
  }
}