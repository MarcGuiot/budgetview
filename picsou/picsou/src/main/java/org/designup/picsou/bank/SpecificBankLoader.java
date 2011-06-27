package org.designup.picsou.bank;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.bank.specific.BankPopulaire;
import org.designup.picsou.bank.specific.CaisseEpargne;
import org.designup.picsou.bank.specific.LaPoste;
import org.designup.picsou.bank.specific.Barclays;

public class SpecificBankLoader {
  public SpecificBankLoader() {
  }

  public void load(GlobRepository repository, Directory directory){
    new BankPopulaire(repository, directory);
    new CaisseEpargne(repository, directory);
    new LaPoste(repository, directory);
    new Barclays(repository, directory);
  }
}
