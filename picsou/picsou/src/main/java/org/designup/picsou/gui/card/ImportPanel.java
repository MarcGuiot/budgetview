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

  private Mode mode;
  private boolean showSignpost;

  public enum Mode {
    STANDARD("/layout/importexport/importPanel_standard.splits"),
    COMPACT("/layout/importexport/importPanel_compact.splits");

    final String filePath;

    private Mode(String filePath) {
      this.filePath = filePath;
    }
  }

  public ImportPanel(Mode mode, boolean showSignpost, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.mode = mode;
    this.showSignpost = showSignpost;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), mode.filePath, repository, directory);

    Action action = ImportFileAction.init(Lang.get("import"), repository, directory, null);
    JButton button = new JButton(action);
    builder.add("importButton", button);

    JButton importLabel = new JButton(Lang.get("importPanel.import.label"));
    importLabel.setModel(button.getModel());
    builder.add("importLabel", importLabel);

    if (showSignpost) {
      final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
      importSignpost.attach(button);
    }

    parentBuilder.add("importPanel", builder);
  }
}