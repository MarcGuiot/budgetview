package org.designup.picsou.gui.help;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HelpService {

  private final BrowsingService browsingService;
  private HelpSource source;

  private static final String BANK_SITES = "bankSites";
  private Map<String, String> bankTitles = new HashMap<String, String>();

  public HelpService(GlobRepository repository, Directory directory) {
    this.browsingService = directory.get(BrowsingService.class);
    this.source = new I18NHelpSource();
  }

  public void setSource(HelpSource source) {
    this.source = source;
  }

  public void show(String helpRef) {
    browsingService.launchBrowser(Lang.get("help.url." + helpRef));
  }

  public String getBankHelp(Glob bank) {
    String ref = getBankSiteRef(bank);
    return source.findContent(ref);
  }

  private String getBankSiteRef(Glob bank) {
    return BANK_SITES + "." + bank.get(Bank.NAME).replaceAll("[\\s]+", "_").toLowerCase();
  }

  private class I18NHelpSource implements HelpSource {
    public String getTitle(String ref) {
      String bankRef = bankTitles.get(ref);
      if (bankRef != null) {
        return bankRef;
      }
      return Lang.get("help." + ref);
    }

    public String findContent(String ref) {
      return Lang.findHelpFile(getFilePath(ref));
    }

    private String getFilePath(String ref) {
      return ref.replaceAll("\\.", "/") + ".html";
    }
  }
}