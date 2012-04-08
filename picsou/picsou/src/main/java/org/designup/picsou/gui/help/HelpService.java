package org.designup.picsou.gui.help;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.Bank;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Functor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HelpService {

  private HelpDialog dialog;
  private GlobRepository repository;
  private Directory directory;
  private HelpSource source;
  private Window lastOwner;

  private static final String BANK_SITES = "bankSites";
  private Map<String, String> bankTitles = new HashMap<String, String>();

  public HelpService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.source = new I18NHelpSource();
  }

  public void setSource(HelpSource source) {
    this.source = source;
  }

  public void show(String helpRef, Window owner) {
    show(helpRef, owner, Functor.NULL);
  }

  public void show(String helpRef, Window owner, Functor onCloseCallback) {
    if ((dialog != null) && dialog.isVisible()) {
      if (owner != lastOwner) {
        dialog.close();
        dialog.dispose();
        dialog = null;
        lastOwner = null;
      }
    }
    if ((dialog == null) || (lastOwner != owner)) {
      dialog = new HelpDialog(source, repository, directory, owner);
      lastOwner = owner;
    }
    dialog.show(helpRef, onCloseCallback);
  }

  public boolean hasBankHelp(Glob bank) {
    return Strings.isNotEmpty(source.findContent(getBankSiteRef(bank)));
  }

  public void showBankHelp(Glob bank, Window owner) {
    String siteRef = getBankSiteRef(bank);
    String bankName = directory.get(DescriptionService.class).getStringifier(Bank.TYPE)
      .toString(bank, repository);

    bankTitles.put(siteRef, Lang.get("help.bankDownloadInstructions", bankName));
    show(siteRef, owner);
  }

  private String getBankSiteRef(Glob bank) {
    return BANK_SITES + "." + bank.get(Bank.NAME).replaceAll("[\\s]+", "_").toLowerCase();
  }

  public void reset(){
    dialog = null;
  }

  private class I18NHelpSource implements HelpSource {
    public String getTitle(String ref) {
      String bankRef = bankTitles.get(ref);
      if (bankRef != null) {
        return bankRef;
      }
      return Lang.get("help." + ref);
    }

    public String getContent(String ref) {
      return Lang.getHelpFile(getFilePath(ref));
    }

    public String findContent(String ref) {
      return Lang.findHelpFile(getFilePath(ref));
    }

    private String getFilePath(String ref) {
      return ref.replaceAll("\\.", "/") + ".html";
    }
  }
}