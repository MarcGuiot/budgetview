package com.budgetview.desktop.mobile;

import com.budgetview.desktop.mobile.utils.AbstractMobileAction;
import com.budgetview.model.AddOns;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;

public class EditMobileAccountAction extends AbstractMobileAction {

  private boolean mobileEnabled;

  public EditMobileAccountAction(final GlobRepository repository, Directory directory) {
    super(Lang.get("mobile.user.create.action.name"), repository, directory);
    repository.addChangeListener(new KeyChangeListener(AddOns.KEY) {
      public void update() {
        doUpdate(repository);
      }
    });
    doUpdate(repository);
  }

  public void doUpdate(GlobRepository repository) {
    this.setEnabled(AddOns.isEnabled(AddOns.MOBILE, repository));
  }

  protected void processMobileStatusChange(boolean enabled) {
    this.mobileEnabled = enabled;
  }

  public void actionPerformed(ActionEvent e) {
    if (!mobileEnabled) {
      CreateMobileAccountDialog dialog = new CreateMobileAccountDialog(directory, repository);
      dialog.show();
    }
    else {
      EditMobileAccountDialog dialog = new EditMobileAccountDialog(directory, repository);
      dialog.show();
    }
  }
}
