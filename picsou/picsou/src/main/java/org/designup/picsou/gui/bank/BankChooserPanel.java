package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.bank.actions.AddBankAction;
import org.designup.picsou.gui.bank.actions.DeleteBankAction;
import org.designup.picsou.gui.bank.actions.EditBankAction;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.Country;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEqualsIgnoreCase;

public class BankChooserPanel {

  private JPanel panel;
  private GlobListViewFilter filter;
  private DeleteBankAction deleteBankAction;
  private GlobsPanelBuilder builder;
  private final GlobMatcher baseMatcher;
  private JPopupButton countryButton;

  public BankChooserPanel(GlobRepository repository,
                          Directory directory,
                          Action validateAction,
                          GlobMatcher matcher,
                          Window owner) {

    builder = new GlobsPanelBuilder(getClass(),
                                    "/layout/bank/bankChooserPanel.splits",
                                    repository, directory);

    GlobListView bankListView = builder.addList("bankList", Bank.TYPE)
      .addDoubleClickAction(validateAction);
    filter = GlobListViewFilter.init(bankListView).setIgnoreAccents(true);
    if (matcher == null) {
      filter.setDefaultValue(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID));
      baseMatcher = GlobMatchers.ALL;
    }
    else {
      baseMatcher = matcher;
    }
    filter.setDefaultMatcher(baseMatcher);
    builder.add("bankEditor", filter);

    JPopupMenu countryMenu = new JPopupMenu();
    countryMenu.add(new ShowCountryAction(null));
    countryMenu.addSeparator();
    for (Country country : Country.values()) {
      countryMenu.add(new ShowCountryAction(country));
    }
    countryButton = new JPopupButton("", countryMenu);
    builder.add("countrySelector", countryButton);
    setCountry(getDefaultCountry(repository));

    JPopupMenu actionMenu = new JPopupMenu();
    actionMenu.add(new EditBankAction(owner, repository, directory));
    deleteBankAction = new DeleteBankAction(owner, repository, directory);
    actionMenu.add(deleteBankAction);
    actionMenu.addSeparator();
    actionMenu.add(new AddBankAction(owner, repository, builder.getDirectory()));
    builder.add("bankActions", new JPopupButton(Lang.get("budgetView.actions"), actionMenu));

    panel = builder.load();
  }

  private Country getDefaultCountry(GlobRepository repository) {
    Set<String> countries =
      repository.getAll(Account.TYPE, AccountMatchers.userCreatedAccounts())
        .getTargets(Account.BANK, repository)
        .getValueSet(Bank.COUNTRY);
    if (countries.isEmpty() && "fr".equals(Lang.getLocale().getLanguage())) {
      return Country.FRANCE;
    }
    if (countries.size() == 1) {
      return Country.get(countries.iterator().next());
    }
    return null;
  }

  public void setExcludedAccounts(Set<Integer> excludedAccountIds) {
    deleteBankAction.setExcludedAccounts(excludedAccountIds);
  }

  public void requestFocus() {
    filter.getComponent().requestFocus();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void dispose() {
    builder.dispose();
  }

  private class ShowCountryAction extends AbstractAction {
    private Country country;

    public ShowCountryAction(Country country) {
      super(country == null ? Country.getLabelForAll() : country.getLabel());
      this.country = country;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      setCountry(country);
    }
  }

  public void setCountry(Country country) {
    if (country == null) {
      countryButton.setText(Country.getLabelForAll());
      filter.setDefaultMatcher(baseMatcher);
    }
    else {
      countryButton.setText(country.getLabel());
      filter.setDefaultMatcher(and(fieldEqualsIgnoreCase(Bank.COUNTRY, country.getCode()), baseMatcher));
    }
  }
}
