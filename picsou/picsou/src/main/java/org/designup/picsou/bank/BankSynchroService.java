package org.designup.picsou.bank;

import org.designup.picsou.bank.importer.OtherBank;
import org.designup.picsou.bank.importer.cic.CicConnector;
import org.designup.picsou.bank.importer.creditmutuel.CreditMutuelArkea;
import org.designup.picsou.bank.importer.ofx.OfxDownloadPage;
import org.designup.picsou.bank.importer.sg.SgConnector;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BankSynchroService {
  private Map<Integer, BankConnectorDisplay> banks = new HashMap<Integer, BankConnectorDisplay>();
  static public boolean SHOW_SYNCHRO =
    System.getProperty("budgetview.synchro", "false").equalsIgnoreCase("true");

  public BankSynchroService() {
    register(SgConnector.BANK_ID, new SgConnector.Factory());
    register(CreditMutuelArkea.BANK_ID, new CreditMutuelArkea.Factory());
    register(CicConnector.BANK_ID, new CicConnector.Factory());
    register(OtherBank.BANK_ID, new OtherBank.Factory());
  }

  public void register(Integer bankId, BankConnectorDisplay connectorDisplay) {
    banks.put(bankId, connectorDisplay);
  }

  public GlobList show(Window parent, GlobList realAccounts, Directory directory, GlobRepository repository) {
    Map<String, Glob> realAccountByUrl = new HashMap<String, Glob>();
    Map<Integer, Glob> bankToRealAccount = new HashMap<Integer, Glob>();
    for (Glob account : realAccounts) {
      Glob bank = repository.findLinkTarget(account, RealAccount.BANK);
      if (bank != null) {
        if (bank.isTrue(Bank.OFX_DOWNLOAD)) {
          realAccountByUrl.put(account.get(RealAccount.URL), account);
        }
        else {
          bankToRealAccount.put(bank.get(Bank.ID), account);
        }
      }
    }
    GlobList importedAccount = new GlobList();
    for (Glob glob : realAccountByUrl.values()) {
      OfxDownloadPage download =
        new OfxDownloadPage(parent, repository, directory, glob.get(RealAccount.BANK), glob.get(RealAccount.URL),
                            glob.get(RealAccount.ORG), glob.get(RealAccount.FID));
      download.init();
      importedAccount.addAll(download.show());
    }
    for (Integer bankId : bankToRealAccount.keySet()) {
      BankConnectorDisplay connectorDisplay = banks.get(bankId);
      if (connectorDisplay != null) {
        importedAccount.addAll(connectorDisplay.show(parent, directory, repository));
      }
    }
    for (Iterator<Glob> iterator = importedAccount.iterator(); iterator.hasNext(); ) {
      Glob glob = iterator.next();
      if (glob.get(RealAccount.ACCOUNT) == null) {
        iterator.remove();
      }
    }
    return importedAccount;
  }

  public GlobList show(Window parent, Integer bankId, Directory directory, GlobRepository repository) {
    BankConnectorDisplay connectorDisplay = banks.get(bankId);
    if (connectorDisplay != null) {
      GlobList importedAccount = connectorDisplay.show(parent, directory, repository);
      filterRemoveAccountWithNoImport(importedAccount);
      return importedAccount;
    }

    Glob bank = repository.find(Key.create(Bank.TYPE, bankId));
    if ((bank != null) && bank.isTrue(Bank.OFX_DOWNLOAD)) {
      OfxDownloadPage download =
        new OfxDownloadPage(parent, repository, directory, bankId, bank.get(Bank.DOWNLOAD_URL),
                            bank.get(Bank.ORG), bank.get(Bank.FID));
      download.init();
      GlobList importedAccount = download.show();
      filterRemoveAccountWithNoImport(importedAccount);
      return importedAccount;
    }

    return GlobList.EMPTY;
  }

  private void filterRemoveAccountWithNoImport(GlobList importedAccount) {
    for (Iterator<Glob> iterator = importedAccount.iterator(); iterator.hasNext(); ) {
      Glob glob = iterator.next();
      if (glob.get(RealAccount.ACCOUNT) != null && glob.get(RealAccount.FILE_NAME) == null) {
        iterator.remove();
      }
    }
  }
}
