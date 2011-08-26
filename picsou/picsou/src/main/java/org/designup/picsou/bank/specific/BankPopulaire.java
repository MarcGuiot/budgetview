package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.directory.Directory;

public class BankPopulaire extends AbstractBankPlugin {

  public BankPopulaire(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 10807));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public boolean apply(Glob importedAccount, Glob account, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    String name = transactions.getFirst().get(ImportedTransaction.OFX_NAME);
    if (name != null && name.toUpperCase().startsWith("FACTURETTE CB")) {
        for (Glob glob : transactions) {
          localRepository.update(glob.getKey(),
                                 FieldValue.value(ImportedTransaction.OFX_NAME, glob.get(ImportedTransaction.OFX_MEMO)),
                                 FieldValue.value(ImportedTransaction.OFX_MEMO, null));
        }
        return true;
      }
    return false;
  }

  public int getVersion() {
    return 1;
  }
}
