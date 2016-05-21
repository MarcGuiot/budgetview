package com.budgetview.bank;

import com.budgetview.bank.plugins.BanquePopulaire;
import com.budgetview.bank.plugins.LaPoste;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SpecificBankLoader {
  public SpecificBankLoader() {
  }

  public void load(GlobRepository repository, Directory directory) {
    new BanquePopulaire(repository, directory);
    new LaPoste(repository, directory);
  }
}
