package org.designup.picsou.gui.addons.specific;

import org.designup.picsou.gui.addons.AddOn;
import org.designup.picsou.model.AddOns;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class MobileAddOn extends AddOn {
  public MobileAddOn() {
    super(AddOns.MOBILE, "addons/mobile.png");
  }

  protected void processPostActivation(GlobRepository repository, Directory directory) {
  }
}
