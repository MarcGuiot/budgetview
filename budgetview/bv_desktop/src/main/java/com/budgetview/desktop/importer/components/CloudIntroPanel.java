package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class CloudIntroPanel implements Disposable {
  private JPanel initialPanel = new JPanel();
  private JPanel refreshPanel = new JPanel();
  private JPanel editPanel = new JPanel();
  private ImportController controller;
  private final LocalGlobRepository repository;
  private final Directory directory;
  private JPanel panel;
  private DisposableGroup disposables = new DisposableGroup();
  private ProgressPanel progressPanel;

  public CloudIntroPanel(ImportController controller, LocalGlobRepository localRepository, Directory localDirectory) {
    this.controller = controller;
    this.repository = localRepository;
    this.directory = localDirectory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/components/cloudIntroPanel.splits", repository, directory);

    builder.add("initial", initialPanel);
    builder.add("refresh", refreshPanel);
    builder.add("edit", editPanel);

    JEditorPane initialMessage = GuiUtils.createReadOnlyHtmlComponent(Lang.get("import.fileSelection.cloud.initial.message"));
    initialMessage.addHyperlinkListener(new HyperlinkHandler(directory));
    builder.add("initialMessage", initialMessage);

    final AbstractAction openSynchroAction = new AbstractAction(Lang.get("import.fileSelection.cloud.initial.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudSignup();
      }
    };
    builder.add("openCloudSynchro", openSynchroAction);

    final AbstractAction refreshAction = new AbstractAction(Lang.get("import.fileSelection.cloud.refresh.button")) {
      public void actionPerformed(ActionEvent e) {
        GlobList connections = repository.getAll(CloudProviderConnection.TYPE, fieldEquals(CloudProviderConnection.INITIALIZED, false));
        if (connections.isEmpty()) {
          controller.showCloudDownload();
        }
        else {
          controller.showCloudFirstDownload(connections.getFirst());
        }
      }
    };
    builder.add("refreshCloud", refreshAction);

    final AbstractAction editAction = new AbstractAction(Lang.get("import.fileSelection.cloud.edition.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudEdition();
      }
    };
    builder.add("editCloudConnections", editAction);

    BooleanFieldListener listener = BooleanFieldListener.install(CloudDesktopUser.KEY, CloudDesktopUser.REGISTERED, repository, new BooleanListener() {
      public void apply(boolean registered) {
        initialPanel.setVisible(!registered);
        openSynchroAction.setEnabled(!registered);
        refreshPanel.setVisible(registered);
        refreshAction.setEnabled(registered);
        editPanel.setVisible(registered);
        editAction.setEnabled(registered);
      }
    });
    disposables.add(listener);

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder.load();
  }

  public void dispose() {
    disposables.dispose();
  }

  public void requestFocus() {
  }
}
