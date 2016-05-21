package com.budgetview.gui.preferences.panes;

import com.budgetview.gui.components.dialogs.MessageDialog;
import com.budgetview.gui.components.dialogs.MessageType;
import com.budgetview.gui.preferences.PreferencesPane;
import com.budgetview.gui.preferences.PreferencesResult;
import com.budgetview.gui.startup.AppPaths;
import com.budgetview.gui.preferences.PreferencesDialog;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class StorageDirPane implements PreferencesPane {
  private final Window dialog;
  private final GlobRepository repository;
  private final Directory directory;

  private final JLabel storageDir;
  private final AbstractAction revertToDefaultDirAction;
  private final JLabel storageDirMessage;
  private final JEditorPane updateMessage;
  private JPanel panel;
  private String currentDataDir;

  public StorageDirPane(Window dialog, GlobRepository repository, Directory directory) {
    this.dialog = dialog;
    this.repository = repository;
    this.directory = directory;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(PreferencesDialog.class,
                                                      "/layout/general/preferences/storageDirPane.splits",
                                                      repository, directory);

    storageDir = new JLabel();

    storageDirMessage = new JLabel();
    builder.add("storageDirMessage", storageDirMessage);

    builder.add("storageDir", storageDir);
    builder.add("browseStorageDir", new BrowseDataPath());
    revertToDefaultDirAction = new RevertToDefaultDir();
    builder.add("revertToDefault", revertToDefaultDirAction);

    updateMessage = GuiUtils.createReadOnlyHtmlComponent(Lang.get("data.path.prefs.updateMessage"));
    builder.add("updateMessage", updateMessage);
    updateMessage.setVisible(false);

    panel = builder.load();
  }

  public void prepareForDisplay() {
    currentDataDir = AppPaths.getCurrentStoragePath();
    storageDir.setText(currentDataDir);
    update();
  }

  private void update() {
    String newStorageDir = storageDir.getText();
    revertToDefaultDirAction.setEnabled(!AppPaths.isDefaultDataPath(newStorageDir));
    boolean messageShown = updateMessage.isVisible();
    boolean dirChanged = !Utils.equal(newStorageDir, currentDataDir);
    storageDirMessage.setText(Lang.get(dirChanged ? "data.path.prefs.nextPath" : "data.path.prefs.currentPath"));
    updateMessage.setVisible(dirChanged);
    if (messageShown != dirChanged) {
      GuiUtils.revalidate(updateMessage);
    }
  }

  public JPanel getPanel() {
    return panel;
  }

  public void processCancel() {
  }

  private class RevertToDefaultDir extends AbstractAction {
    public RevertToDefaultDir() {
      super(Lang.get("data.path.revert"));
    }

    public void actionPerformed(ActionEvent e) {
      storageDir.setText(AppPaths.getDefaultDataPath());
      update();
    }
  }

  private class BrowseDataPath extends AbstractAction {
    private BrowseDataPath() {
      super(Lang.get("data.path.browse"));
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      String path = AppPaths.getRedirect();
      if (path != null && new File(path).exists()) {
        chooser.setCurrentDirectory(new File(path));
      }
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      int returnVal = chooser.showOpenDialog(dialog);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        if (file != null) {
          storageDir.setText(file.getAbsolutePath());
          update();
        }
      }
    }
  }

  public void validate(PreferencesResult result) {
    if (!AppPaths.isNewDataDir(storageDir.getText().trim())) {
      return;
    }

    String newPath = storageDir.getText();
    boolean overwrite = false;
    if (!AppPaths.isEmptyDataDir(newPath)) {
      OverwriteConfirmationDialog confirmation = new OverwriteConfirmationDialog(dialog, repository, directory);
      confirmation.show();
      if (confirmation.wasCancelled()) {
        result.preventClose();
        return;
      }
      overwrite = confirmation.overwriteSelected();
    }

    Ref<String> message = new Ref<String>();
    if (!AppPaths.moveDataDirTo(newPath, message, overwrite)) {
      result.preventClose();
      MessageDialog.showMessage("data.path.title", MessageType.ERROR, dialog, directory, message.get());
      return;
    }

    MessageDialog.show("data.path.title", MessageType.INFO, dialog, directory, "data.path.exit");
    result.exitAfterClose();
  }

  public void postValidate() {
  }

}
