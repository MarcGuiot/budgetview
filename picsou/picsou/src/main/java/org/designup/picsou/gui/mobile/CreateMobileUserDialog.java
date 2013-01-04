package org.designup.picsou.gui.mobile;

import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CreateMobileUserDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localGlobRepository;
  private final DefaultDirectory localDirectory;
  private final SelectionService selectionService;
  private Glob userPreference;
  private Future<String> submit;

  public CreateMobileUserDialog(Directory directory, GlobRepository repository) {
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(SelectionService.class, selectionService);
    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);
    localGlobRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(UserPreferences.TYPE).get();
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/createAccountDialog.splits",
                                                      repository, localDirectory);

    final CreateMobileUserPanel panel = new CreateMobileUserPanel(localDirectory, localGlobRepository);

    builder.add("createAccountPanel", panel.create());
    dialog.addPanelWithButtons(builder.<JPanel>load(), new ValidateCreateMobileAccountAction(panel),
                               new AbstractAction(Lang.get("cancel")) {
                                 public void actionPerformed(ActionEvent e) {
                                   dialog.setVisible(false);
                                 }
                               }
    );
  }

  public void show() {
    userPreference = localGlobRepository.get(UserPreferences.KEY);
    selectionService.select(userPreference);
    dialog.pack();
    dialog.showCentered();
  }

  private class ValidateCreateMobileAccountAction extends AbstractAction {
    private final CreateMobileUserPanel panel;

    public ValidateCreateMobileAccountAction(CreateMobileUserPanel panel) {
      super(Lang.get("ok"));
      this.panel = panel;
    }

    public void actionPerformed(ActionEvent e) {
      if (submit != null){
        return;
      }
      panel.startProgress();
      submit = localDirectory.get(ExecutorService.class)
        .submit(new Callable<String>() {
          public String call() throws Exception {
            Ref<String> messageRef = new Ref<String>();
            boolean isOk = localDirectory.get(ConfigService.class)
              .createMobileAccount(userPreference.get(UserPreferences.MAIL_FOR_MOBILE)
                , messageRef);
            panel.stopProgress();
            if (dialog.isVisible()) {
              if (isOk) {
                localGlobRepository.commitChanges(true);
                dialog.setVisible(false);
              }
              else {
                panel.setMessage(messageRef.get());
              }
            }
            return null;
          }
        });
    }
  }
}
