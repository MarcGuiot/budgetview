package org.designup.picsou.gui.mobile;

import org.designup.picsou.gui.mobile.utils.AbstractMobileAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;

public class EditMobileAccountAction extends AbstractMobileAction {

  private boolean mobileEnabled;

  public EditMobileAccountAction(GlobRepository repository, Directory directory) {
    super(Lang.get("mobile.user.create.action.name"), repository, directory);
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
