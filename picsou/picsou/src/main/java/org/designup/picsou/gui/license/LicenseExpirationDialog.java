package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class LicenseExpirationDialog {
  private PicsouDialog dialog;
  private JLabel response = new JLabel();
  private LocalGlobRepository localGlobRepository;
  private AbstractAction sendAction;

  public LicenseExpirationDialog(Window parent, final GlobRepository repository, final Directory directory) {
    localGlobRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(User.TYPE).get();
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/licenseExpirationDialog.splits",
                            localGlobRepository, directory);

    builder.add("mailResponse", response);
    final Glob user = repository.get(User.KEY);
    sendAction = new AbstractAction(Lang.get("license.mail.request.send")) {
      public void actionPerformed(ActionEvent e) {
        final String mail = user.get(User.MAIL);
        if (mail != null) {
          Thread thread = new Thread() {
            public void run() {
              final String response = directory.get(ConfigService.class).askForNewCodeByMail(mail);
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  LicenseExpirationDialog.this.response.setText(response);
                  LicenseExpirationDialog.this.response.setVisible(true);
                }
              });
            }
          };
          thread.setDaemon(true);
          thread.start();
        }
      }
    };
    builder.add("sendMail", sendAction);
    builder.add("mailAdress",
                GlobTextEditor.init(User.MAIL, localGlobRepository, directory).forceSelection(user));
    localGlobRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          response.setVisible(false);
          sendAction.setEnabled(Strings.isNotEmpty(repository.get(User.KEY).get(User.MAIL)));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        sendAction.setEnabled(Strings.isNotEmpty(repository.get(User.KEY).get(User.MAIL)));
      }
    });
    dialog = PicsouDialog.createWithButton(parent, builder.<JPanel>load(), new ValidateAction(), directory);
    dialog.pack();
  }

  public void show() {
    GuiUtils.center(dialog);
    dialog.setVisible(true);
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      localGlobRepository.commitChanges(true);
    }
  }
}
