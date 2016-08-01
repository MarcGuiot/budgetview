package com.budgetview.gui.components.server;

import com.budgetview.gui.components.tips.ErrorTip;
import com.budgetview.model.User;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DisconnectionTip implements Disposable {
  private JComponent component;
  private GlobRepository repository;
  private Directory directory;
  private ErrorTip errorTip = null;
  private ChangeSetListener tipsListener;

  public DisconnectionTip(JComponent component, GlobRepository repository, Directory directory) {
    this.component = component;
    this.repository = repository;
    this.directory = directory;
    tipsListener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsUpdates(User.CONNECTED)) {
          showConnection();
        }
      }
    };
    repository.addChangeListener(tipsListener);
    showConnection();
  }

  private void showConnection() {
    Glob user = repository.get(User.KEY);
    if (!user.isTrue(User.CONNECTED) && errorTip == null) {
      errorTip = ErrorTip.showLeft(component, Lang.get("feedback.notConnected"), directory);
    }

    if (user.isTrue(User.CONNECTED) && errorTip != null) {
      errorTip.dispose();
    }

    component.setEnabled(user.isTrue(User.CONNECTED));
  }


  public void dispose() {
    repository.removeChangeListener(tipsListener);
  }
}
