package org.designup.picsou.bank;

import org.designup.picsou.bank.importer.OtherBank;
import org.designup.picsou.bank.importer.creditmutuel.CreditMutuelArkea;
import org.designup.picsou.bank.importer.ofx.OfxDownloadPage;
import org.designup.picsou.bank.importer.sg.SG;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class BankSynchroService {
  private Map<Integer, BankSynchro> banks = new HashMap<Integer, BankSynchro>();
  static public boolean SHOW_SYNCHRO =
    System.getProperty("budgetview.synchro", "false").equalsIgnoreCase("true");

  public interface BankSynchro {
    GlobList show(Directory directory, GlobRepository repository);
  }

  public BankSynchroService() {
    register(SG.SG_ID, new SG.Init());
    register(CreditMutuelArkea.ID, new CreditMutuelArkea.Init());
    register(OtherBank.ID, new OtherBank.Init());
  }

  public void register(Integer bankId, BankSynchro synchro) {
    banks.put(bankId, synchro);
  }

  public GlobList show(GlobList realAccounts, Directory directory, GlobRepository repository) {
    Map<String, Glob> realAccountByUrl = new HashMap<String, Glob>();
    Map<Integer, Glob> bankToRealAccount = new HashMap<Integer, Glob>();
    for (Glob account : realAccounts) {
      Glob bank = repository.findLinkTarget(account, RealAccount.BANK);
      if (bank != null){
        if (bank.isTrue(Bank.OFX_DOWNLOAD)){
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
        new OfxDownloadPage(repository, directory, glob.get(RealAccount.BANK), glob.get(RealAccount.URL),
                        glob.get(RealAccount.ORG), glob.get(RealAccount.FID));
      download.init();
      importedAccount.addAll(download.show());
    }
    for (Integer bankId : bankToRealAccount.keySet()) {
      BankSynchro synchro = banks.get(bankId);
      if (synchro != null) {
        importedAccount.addAll(synchro.show(directory, repository));
      }
    }
    for (Iterator<Glob> iterator = importedAccount.iterator(); iterator.hasNext();) {
      Glob glob = iterator.next();
      if (glob.get(RealAccount.ACCOUNT) == null){
        iterator.remove();
      }
    }
    return importedAccount;
  }

  public GlobList show(Integer bankId, Directory directory, GlobRepository repository) {
    BankSynchro synchro = banks.get(bankId);
    if (synchro != null) {
      GlobList importedAccount = synchro.show(directory, repository);
      filterRemoveAccountWithNoImport(importedAccount);
      return importedAccount;
    }
    else {
      Glob glob = repository.find(Key.create(Bank.TYPE, bankId));
      if (glob != null) {
        if (glob.isTrue(Bank.OFX_DOWNLOAD)) {
          OfxDownloadPage download =
            new OfxDownloadPage(repository, directory, bankId, glob.get(Bank.DOWNLOAD_URL),
                            glob.get(Bank.ORG), glob.get(Bank.FID));
          download.init();
          GlobList importedAccount = download.show();
          filterRemoveAccountWithNoImport(importedAccount);
          return importedAccount;
        }
      }
    }
    return GlobList.EMPTY;
  }

  private void filterRemoveAccountWithNoImport(GlobList importedAccount) {
    for (Iterator<Glob> iterator = importedAccount.iterator(); iterator.hasNext();) {
      Glob glob = iterator.next();
      if (glob.get(RealAccount.ACCOUNT) != null && glob.get(RealAccount.FILE_NAME) == null){
        iterator.remove();
      }
    }
  }
}
