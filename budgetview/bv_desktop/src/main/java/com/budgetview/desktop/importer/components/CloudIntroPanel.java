package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.utils.Lang;
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
  private JPanel initialPanel = new JPanel();
  private JPanel refreshPanel = new JPanel();
  private JPanel addPanel = new JPanel();
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

    builder.add("initial", initialPanel);
    builder.add("refresh", refreshPanel);
    builder.add("add", addPanel);

    BooleanFieldListener listener = BooleanFieldListener.install(CloudDesktopUser.KEY, CloudDesktopUser.REGISTERED, repository, new BooleanListener() {
      public void apply(boolean registered) {
        initialPanel.setVisible(!registered);
        refreshPanel.setVisible(registered);
        addPanel.setVisible(registered);
      }
    });
    disposables.add(listener);

    builder.add("openCloudSynchro", new AbstractAction(Lang.get("import.fileSelection.cloud.initial.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudSignup();
      }
    });

    builder.add("refreshCloud", new AbstractAction(Lang.get("import.fileSelection.cloud.refresh.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudRefresh();
      }
    });

    builder.add("addCloudConnection", new AbstractAction(Lang.get("import.fileSelection.cloud.edition.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudEdition();
      }
    });

    return builder.load();
  }

  public void dispose() {
    disposables.dispose();
  }

  public void requestFocus() {
  }
}
