package org.designup.picsou.gui.license.promotion;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

class PurchaseLicenseAction extends BrowsingAction {

  public PurchaseLicenseAction(Directory directory) {
    super(Lang.get("license.premium.buy.label"), directory);
  }

  protected String getUrl() {
    return Lang.get("license.premium.buy.url");
  }
}
