package org.designup.picsou.gui.messages;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.AccountPositionError;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MessageView extends View {
  private ShowErrorAction showErrorAction;
  private GlobRepository repository;

  public MessageView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.repository = repository;
    repository.addChangeListener(new AbstractChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(AccountPositionError.TYPE)) {
          updateButton(repository);
        }
      }
    });
  }

  private void updateButton(GlobRepository repository) {
    boolean hasError = false;
    GlobList messages = repository.getAll(AccountPositionError.TYPE);
    for (Glob glob : messages) {
      if (!glob.get(AccountPositionError.CLEARED)) {
        hasError = true;
      }
    }
    if (hasError) {
      showErrorAction.putValue(Action.NAME, Lang.get("messages.button.error"));
      showErrorAction.setEnabled(true);
    }
    else {
      if (messages.isEmpty()) {
        showErrorAction.putValue(Action.NAME, Lang.get("messages.button.no.msg"));
        showErrorAction.setEnabled(false);
      }
      else {
        showErrorAction.putValue(Action.NAME, Lang.get("messages.button.msg"));
        showErrorAction.setEnabled(true);
      }
    }
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    showErrorAction = new ShowErrorAction();
    updateButton(repository);
    JPanel panel = new JPanel();
    panel.setName("MessagesView");
    JButton messageButton = new JButton(showErrorAction);
    panel.add(messageButton);
    builder.add(panel);
  }

  private class ShowErrorAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      MessagesDialog dialog = new MessagesDialog(directory, repository);
      dialog.show();
    }
  }
}
