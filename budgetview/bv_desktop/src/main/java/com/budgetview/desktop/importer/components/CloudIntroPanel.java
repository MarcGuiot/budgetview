package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloudIntroPanel implements Disposable {
  private final PicsouDialog dialog;
  private ImportController controller;
  private final LocalGlobRepository repository;
  private final Directory directory;
  private JPanel panel;
  private DisposableGroup disposables = new DisposableGroup();

  public CloudIntroPanel(PicsouDialog dialog, ImportController controller, LocalGlobRepository localRepository, Directory localDirectory) {
    this.dialog = dialog;
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

    builder.add("openCloudSynchro", new AbstractAction("Goto synchro") {
      public void actionPerformed(ActionEvent e) {
        if (CloudDesktopUser.isTrue(CloudDesktopUser.REGISTERED, repository)) {
          controller.showCloudBankSelection();
        }
        else {
          controller.showCloudSignup();
        }
      }
    });

    final AbstractAction refreshAction = new AbstractAction("Refresh") {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudRefresh();
      }
    };
    builder.add("refreshCloud", refreshAction);
    BooleanFieldListener listener = BooleanFieldListener.install(CloudDesktopUser.KEY, CloudDesktopUser.SYNCHRO_ENABLED, repository, new BooleanListener() {
      public void apply(boolean active) {
        refreshAction.setEnabled(active);
      }
    });
    disposables.add(listener);

    return builder.load();
  }

  public void dispose() {
    disposables.dispose();
  }

  public void requestFocus() {
  }
}
