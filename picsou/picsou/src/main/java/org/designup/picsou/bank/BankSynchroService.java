package org.designup.picsou.bank;

import org.designup.picsou.bank.connectors.OtherBankConnector;
import org.designup.picsou.bank.connectors.cic.CicConnector;
import org.designup.picsou.bank.connectors.creditmutuel.CreditMutuelArkeaConnector;
import org.designup.picsou.bank.connectors.labanquepostale.LaBanquePostaleConnector;
import org.designup.picsou.bank.connectors.ofx.OfxDownloadPage;
import org.designup.picsou.bank.connectors.sg.SgConnector;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BankSynchroService {
  private Map<Integer, BankConnectorFactory> banks = new HashMap<Integer, BankConnectorFactory>();
  static public boolean SHOW_SYNCHRO =
    System.getProperty("budgetview.synchro", "false").equalsIgnoreCase("true");

  public BankSynchroService() {
    register(SgConnector.BANK_ID, new SgConnector.Factory());
    register(CreditMutuelArkeaConnector.BANK_ID, new CreditMutuelArkeaConnector.Factory());
    register(CicConnector.BANK_ID, new CicConnector.Factory());
    register(LaBanquePostaleConnector.BANK_ID, new LaBanquePostaleConnector.Factory());
    register(OtherBankConnector.BANK_ID, new OtherBankConnector.Factory());
  }

  public void register(Integer bankId, BankConnectorFactory connectorDisplay) {
    banks.put(bankId, connectorDisplay);
  }

  public List<BankConnector> getConnectors(GlobList realAccounts, Window parent, GlobRepository repository, Directory directory) {
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

    List<BankConnector> connectors = new ArrayList<BankConnector>();
    for (Glob glob : realAccountByUrl.values()) {
      connectors.add(new OfxDownloadPage(repository, directory, glob.get(RealAccount.BANK), glob.get(RealAccount.URL),
                                         glob.get(RealAccount.ORG), glob.get(RealAccount.FID)));
    }
    for (Integer bankId : bankToRealAccount.keySet()) {
      BankConnectorFactory factory = banks.get(bankId);
      if (factory != null) {
        connectors.add(factory.create(repository, directory));
      }
    }
    return connectors;
  }

  public BankConnector getConnector(Integer bankId, Window parent, GlobRepository repository, Directory directory) {
    BankConnectorFactory factory = banks.get(bankId);
    if (factory != null) {
      return factory.create(repository, directory);
    }

    Glob bank = repository.find(Key.create(Bank.TYPE, bankId));
    if ((bank != null) && bank.isTrue(Bank.OFX_DOWNLOAD)) {
      return new OfxDownloadPage(repository, directory, bankId, bank.get(Bank.DOWNLOAD_URL),
                            bank.get(Bank.ORG), bank.get(Bank.FID));
    }
    return null;
  }
}
