package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class InitializationView extends View {

  private JPanel panel = new JPanel();
  private ImportFileAction importFileAction;

  public InitializationView(ImportFileAction importFileAction, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.importFileAction = importFileAction;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/initializationView.splits",
                                                      repository, directory);

    builder.add("initializationPanel", panel);

    builder.add("import", importFileAction);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    registerUpdater();

    parentBuilder.add("initializationView", builder);
  }

  private void registerUpdater() {
    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE) {
      protected void update(GlobRepository repository) {
        updateView();
      }
    });
  }

  private void updateView() {
    panel.setVisible(!repository.contains(Transaction.TYPE));
  }
}
