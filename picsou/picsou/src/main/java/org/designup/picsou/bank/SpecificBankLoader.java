package org.designup.picsou.bank;

import org.designup.picsou.bank.specific.BankPopulaire;
import org.designup.picsou.bank.specific.LaPoste;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SpecificBankLoader {
  public SpecificBankLoader() {
  }

  public void load(GlobRepository repository, Directory directory) {
    new BankPopulaire(repository, directory);
    new LaPoste(repository, directory);
  }
}
