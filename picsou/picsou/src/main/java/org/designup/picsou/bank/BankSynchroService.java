package org.designup.picsou.bank;

import org.designup.picsou.bank.connectors.OtherBankConnector;
import org.designup.picsou.bank.connectors.americanexpressfr.AmexFrConnector;
import org.designup.picsou.bank.connectors.bnp.BnpConnector;
import org.designup.picsou.bank.connectors.cic.CicConnector;
import org.designup.picsou.bank.connectors.creditagricole.CreditAgricoleConnector;
import org.designup.picsou.bank.connectors.labanquepostale.LaBanquePostaleConnector;
import org.designup.picsou.bank.connectors.ofx.OfxDownloadPage;
import org.designup.picsou.bank.connectors.sg.SgConnector;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.Synchro;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankSynchroService {
  private Map<Integer, BankConnectorFactory> banks = new HashMap<Integer, BankConnectorFactory>();
  static public boolean SHOW_SYNCHRO = true;
  //System.getProperty("budgetview.synchro", "true").equalsIgnoreCase("true");

  public BankSynchroService() {
    if (SHOW_SYNCHRO){
      register(SgConnector.BANK_ID, new SgConnector.Factory());
//    register(CreditMutuelArkeaConnector.BANK_ID, new CreditMutuelArkeaConnector.Factory());
      register(CicConnector.BANK_ID, new CicConnector.Factory());
      register(LaBanquePostaleConnector.BANK_ID, new LaBanquePostaleConnector.Factory());
      register(BnpConnector.BANK_ID, new BnpConnector.Factory());
      CreditAgricoleConnector.register(this);
      register(AmexFrConnector.BANK_ID, new AmexFrConnector.Factory());
    }
    Utils.beginRemove();
    register(OtherBankConnector.BANK_ID, new OtherBankConnector.Factory());
    Utils.endRemove();
  }

  public void register(Integer bankId, BankConnectorFactory connectorDisplay) {
    banks.put(bankId, connectorDisplay);
  }

  public List<BankConnector> getConnectors(GlobList allSynchro, Window parent, GlobRepository repository, Directory directory) {
    Map<Key, Glob> realAccountByUrl = new HashMap<Key, Glob>();
    Map<Key, Integer> bankToRealAccount = new HashMap<Key, Integer>();
    for (Glob synchro : allSynchro) {
      Glob bank = repository.findLinkTarget(synchro, Synchro.BANK);
      if (bank != null) {
        if (bank.isTrue(Bank.OFX_DOWNLOAD)) {
          realAccountByUrl.put(synchro.getKey(), synchro);
        }
        else {
          bankToRealAccount.put(synchro.getKey(), bank.get(Bank.ID));
        }
      }
    }

    List<BankConnector> connectors = new ArrayList<BankConnector>();
    for (Map.Entry<Key, Glob> entry : realAccountByUrl.entrySet()) {
      Glob glob = entry.getValue();

      connectors.add(new OfxDownloadPage(repository, directory, glob.get(RealAccount.BANK), glob.get(RealAccount.URL),
                                         glob.get(RealAccount.ORG), glob.get(RealAccount.FID),
                                         repository.get(entry.getKey())));
    }
    for (Map.Entry<Key, Integer> entry : bankToRealAccount.entrySet()) {
      BankConnectorFactory factory = banks.get(entry.getValue());
      if (factory != null) {
        connectors.add(factory.create(repository, directory, true, repository.get(entry.getKey())));
      }
    }
    return connectors;
  }

  public BankConnector getConnector(Integer bankId, Window parent, GlobRepository repository, Directory directory) {
    BankConnectorFactory factory = banks.get(bankId);
    if (factory != null) {
      return factory.create(repository, directory, false, repository.create(Synchro.TYPE,
                                                                            FieldValue.value(Synchro.BANK, bankId)));
    }

    Glob bank = repository.find(Key.create(Bank.TYPE, bankId));
    if ((bank != null) && bank.isTrue(Bank.OFX_DOWNLOAD)) {
      return new OfxDownloadPage(repository, directory, bankId, bank.get(Bank.DOWNLOAD_URL),
                                 bank.get(Bank.ORG), bank.get(Bank.FID),
                                 repository.create(Synchro.TYPE,
                                                   FieldValue.value(Synchro.BANK, bankId)));
    }
    return null;
  }
}
