package com.budgetview.desktop.addons.specific;

import com.budgetview.desktop.addons.AddOn;
import com.budgetview.model.AddOns;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class MobileAddOn extends AddOn {
  public MobileAddOn() {
    super(AddOns.MOBILE, "addons/mobile.png");
  }

  protected void processPostActivation(GlobRepository repository, Directory directory) {
  }
}
