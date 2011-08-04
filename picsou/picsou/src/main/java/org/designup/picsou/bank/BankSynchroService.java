package org.designup.picsou.bank;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Utils;
import org.designup.picsou.bank.importer.sg.SG;
import org.designup.picsou.bank.importer.OtherBank;
import org.designup.picsou.bank.importer.creditmutuel.CreditMutuelArkea;

import java.util.Map;
import java.util.HashMap;

public class BankSynchroService {
  private Map<Integer, BankSynchro> banks = new HashMap<Integer, BankSynchro>();

  public interface BankSynchro {
    void show(Directory directory, GlobRepository repository);
  }

  public BankSynchroService() {
    register(SG.SG_ID, new SG.Init());
    register(CreditMutuelArkea.ID, new CreditMutuelArkea.Init());
    Utils.beginRemove();
    register(OtherBank.ID, new OtherBank.Init());
    Utils.endRemove();
  }

  public void register(Integer bankId, BankSynchro synchro){
    banks.put(bankId, synchro);
  }

  public void show(Integer bankId, Directory directory, GlobRepository repository){
    BankSynchro synchro = banks.get(bankId);
    if (synchro != null){
      synchro.show(directory, repository);
    }
  }
}
