package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
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

import com.jidesoft.swing.InfiniteProgressPanel;

public class LicenseExpirationDialog {
  private PicsouDialog dialog;
  private JLabel response = new JLabel();
  private LocalGlobRepository localGlobRepository;
  private AbstractAction sendAction;
  private GlobsPanelBuilder builder;
  private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel();
  private Thread sendRequestThread;

  public LicenseExpirationDialog(Window parent, final GlobRepository repository, final Directory directory) {
    localGlobRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(User.TYPE).get();
    builder = new GlobsPanelBuilder(getClass(), "/layout/general/licenseExpirationDialog.splits",
                          localGlobRepository, directory);

    builder.add("mailResponse", response);
    sendAction = new AbstractAction(Lang.get("license.mail.request.send")) {
      public void actionPerformed(ActionEvent e) {
        sendAction.setEnabled(false);
        progressPanel.setVisible(true);
        progressPanel.start();
        final Glob user = localGlobRepository.get(User.KEY);
        final String mail = user.get(User.EMAIL);
        if (Strings.isNotEmpty(mail) && sendRequestThread == null) {
          sendRequestThread = new Thread() {
            public void run() {
              final String response = directory.get(ConfigService.class).askForNewCodeByMail(mail);
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  requestDone(response);
                }
              });
            }
          };
          sendRequestThread.setDaemon(true);
          sendRequestThread.start();
        }
      }
    };
    builder.add("sendMail", sendAction);
    builder.add("sendState", progressPanel);
    GlobTextEditor mailEditor = GlobTextEditor.init(User.EMAIL, localGlobRepository, directory)
      .setNotifyOnKeyPressed(true)
      .forceSelection(User.KEY);
    builder.add("mailAdress", mailEditor);
    localGlobRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          response.setVisible(false);
          sendAction.setEnabled(Strings.isNotEmpty(repository.get(User.KEY).get(User.EMAIL)));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        sendAction.setEnabled(Strings.isNotEmpty(repository.get(User.KEY).get(User.EMAIL)));
      }
    });
    sendAction.setEnabled(Strings.isNotEmpty(localGlobRepository.get(User.KEY).get(User.EMAIL)));
    dialog = PicsouDialog.createWithButton(parent, builder.<JPanel>load(), new ValidateAction(), directory);
    dialog.pack();
  }

  private void requestDone(String response) {
    this.response.setText(response);
    this.response.setVisible(true);
    end();
  }

  private void end() {
    sendRequestThread = null;
    this.progressPanel.setVisible(false);
    sendAction.setEnabled(true);
    progressPanel.stop();
  }

  public void show() {
    dialog.showCentered();
    end();
    builder.dispose();
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
