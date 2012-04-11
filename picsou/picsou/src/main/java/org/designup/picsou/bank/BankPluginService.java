package org.designup.picsou.bank;

import org.designup.picsou.bank.specific.AbstractBankPlugin;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BankPluginService {
  private Map<Integer, BankPlugin> specific = new HashMap<Integer, BankPlugin>();
  private BankPlugin defaultPlugin = new AbstractBankPlugin() {

    public boolean apply(Glob importedAccount, Glob account, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
      Glob bank = localRepository.find(Key.create(Bank.TYPE, account.get(Account.BANK)));
      if (bank.get(Bank.INVALID_POSITION)) {
//      localRepository.update(account.getKey(), Account.);
      }
      else {
        String amount = importedAccount.get(RealAccount.POSITION);
        if (Strings.isNotEmpty(amount)) {
          localRepository.update(account.getKey(),
                                 FieldValue.value(Account.POSITION, Amounts.extractAmount(amount)),
                                 FieldValue.value(Account.POSITION_DATE, importedAccount.get(RealAccount.POSITION_DATE)),
                                 FieldValue.value(Account.TRANSACTION_ID, null));
        }
      }

      return super.apply(importedAccount, account, transactions, referenceRepository, localRepository, changeSet);
    }

    public int getVersion() {
      return 0;
    }
  };

  public boolean apply(Glob account, Glob currentImportedAcount, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    int bankId = account.get(Account.BANK);
    BankPlugin bankPlugin = specific.get(bankId);
    if (bankPlugin == null) {
      bankPlugin = defaultPlugin;
    }
    return bankPlugin.apply(currentImportedAcount, account, transactions, referenceRepository, localRepository, changeSet);
  }

  public void add(Integer bankId, BankPlugin bankPlugin) {
    BankPlugin actualPlugin = specific.get(bankId);
    if (actualPlugin == null || actualPlugin.getVersion() < bankPlugin.getVersion()) {
      specific.put(bankId, bankPlugin);
    }
  }

  public void postApply(Glob importedAccount, Glob account, GlobList transactions, GlobRepository referenceRepository, GlobRepository localRepository, ChangeSet importChangeSet) {
    Integer bankId = account.get(RealAccount.BANK);
    Glob bank = referenceRepository.findLinkTarget(account, Account.BANK);
    /*
    boolean isOfx = transactions.getFirst().get(ImportedTransaction.IS_OFX);
    if (bank.get(Bank.INVERT_AMOUNT) && account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
      for (Glob transaction : transactions) {
        localRepository.update(transaction.getKey(), ImportedTransaction.AMOUNT,
                               -transaction.get(ImportedTransaction.AMOUNT));
      }
    }
    replace(transactions, localRepository, bank, Bank.CHANGE_NAME_OR_M_MATCH,
            isOfx ? ImportedTransaction.OFX_NAME : ImportedTransaction.QIF_M,
            OfxTransactionFinalizer.NAME_REGEXP, Bank.CHANGE_MEMO_OR_P_REPLACE);

    replace(transactions, localRepository, bank, Bank.CHANGE_MEMO_OR_P_MATCH,
            isOfx ? ImportedTransaction.OFX_NAME : ImportedTransaction.QIF_P,
            OfxTransactionFinalizer.MEMO_REGEXP, Bank.CHANGE_NAME_OR_M_REPLACE);

    if (bank.get(Bank.INVERT_LABEL)) {
      for (Glob transaction : transactions) {
        StringField memoField = isOfx ? ImportedTransaction.OFX_MEMO : ImportedTransaction.QIF_P;
        StringField nameField = isOfx ? ImportedTransaction.OFX_NAME : ImportedTransaction.QIF_M;
        String memoValue = transaction.get(memoField);
        localRepository.update(transaction.getKey(), memoField, transaction.get(nameField));
        localRepository.update(transaction.getKey(), nameField, memoValue);
      }
    }
    */


    BankPlugin bankPlugin = specific.get(bankId);
    if (bankPlugin == null) {
      bankPlugin = defaultPlugin;
    }
    bankPlugin.postApply(transactions, account, referenceRepository, localRepository, importChangeSet);
  }

  private void replace(GlobList transactions, GlobRepository localRepository, Glob bank,
                       final StringField match, final StringField nameField,
                       final Pattern regexp, final StringField replace) {
    String nameRegex = bank.get(match);
    if (Strings.isNotEmpty(nameRegex)) {
      Pattern pattern = Pattern.compile(nameRegex);
      for (Glob transaction : transactions) {
        String name = transaction.get(nameField);
        if (Strings.isNotEmpty(name)) {
          String tmp = Utils.replace(pattern, regexp, name, bank.get(replace));
          if (tmp != null) {
            name = tmp;
          }
        }
        localRepository.update(transaction.getKey(), nameField, name);
      }
    }
  }
}
