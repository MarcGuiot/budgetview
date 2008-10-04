package org.designup.picsou.gui.browsing;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.exceptions.IOFailure;

import java.io.IOException;

public class MacBrowsingService extends BrowsingService {
  public void launchBrowser(String url) {
    try {
      MRJAdapter.openURL(url);
    }
    catch (IOException e) {
      throw new IOFailure(Lang.get("browsing.error"), e);
    }
  }
}
