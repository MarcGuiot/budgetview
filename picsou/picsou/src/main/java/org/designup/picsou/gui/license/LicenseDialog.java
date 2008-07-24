package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.BeginRemove;
import org.designup.picsou.utils.EndRemove;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LicenseDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private SelectionService selectionService;

  public LicenseDialog(Window parent, GlobRepository repository, Directory directory) {
    directory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    directory.add(selectionService);
    LocalGlobRepositoryBuilder localGlobRepositoryBuilder = LocalGlobRepositoryBuilder.init(repository)
      .copy(User.TYPE);
    @BeginRemove
    int a;
    localGlobRepositoryBuilder.copy(UserPreferences.TYPE);
    @EndRemove
    int b;
    this.localRepository = localGlobRepositoryBuilder.get();
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/LicenseDialog.splits",
                                                      localRepository, directory);
    builder.addEditor("mail", User.MAIL);
    builder.addEditor("code", User.ACTIVATION_CODE);
    dialog = PicsouDialog.createWithButtons(parent, builder.<JPanel>load(),
                                            new ValidAction(),
                                            new CancelAction());
    dialog.pack();
  }

  public void show() {
    localRepository.rollback();
    selectionService.select(localRepository.get(User.KEY));
    dialog.setVisible(true);
  }


  private class ValidAction extends AbstractAction {
    public ValidAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      Glob user = localRepository.get(User.KEY);
      @BeginRemove
      int a;
      if (user.get(User.MAIL).equals("admin")) {
        localRepository.update(UserPreferences.key, UserPreferences.FUTURE_MONTH_COUNT, 24);
      }
      @EndRemove
      int b;
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
