package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SkipCategorizationPanel {

  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;

  public SkipCategorizationPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    createPanel();
    repository.addChangeListener(new TypeChangeSetListener(SignpostStatus.TYPE) {
      public void update(GlobRepository repository) {
        SkipCategorizationPanel.this.update();
      }
    });
    update();
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/signpost/skipCategorizationPanel.splits",
                            repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        activate();
      }
    });

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  private void update() {
    panel.setVisible(
      SignpostStatus.isCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository) &&
      !SignpostStatus.isCompleted(SignpostStatus.GOTO_BUDGET_DONE, repository) &&
      !SignpostStatus.isCompleted(SignpostStatus.CATEGORIZATION_SKIPPED, repository));
  }

  private void activate() {
    SignpostStatus.setCompleted(SignpostStatus.GOTO_BUDGET_SHOWN, repository);
    SignpostStatus.setCompleted(SignpostStatus.CATEGORIZATION_SKIPPED, repository);
  }
}
