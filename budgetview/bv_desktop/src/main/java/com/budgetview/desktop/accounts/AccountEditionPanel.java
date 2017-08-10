package com.budgetview.desktop.accounts;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.model.Account;
import com.budgetview.model.Bank;
import com.budgetview.utils.HtmlBuilder;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class AccountEditionPanel extends AbstractAccountPanel<GlobRepository> {
  private CardHandler cards;
  private GlobsPanelBuilder builder;
  private JEditorPane readOnlyDescription;

  public AccountEditionPanel(Window owner, GlobRepository repository, Directory parentDirectory) {
    super(repository, parentDirectory);
    createPanel(owner);
  }

  private void createPanel(Window owner) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountEditionPanel.splits", localRepository,
                                    localDirectory);

    createComponents(builder, owner, Account.LAST_IMPORT_POSITION);

    readOnlyDescription = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("readOnlyDescription", readOnlyDescription);

    cards = builder.addCardHandler("cards");
    cards.show("edition");

    builder.load();
  }

  private String getReadOnlyDescription(Glob account, GlobRepository repository) {

    if (account == null) {
      return "";
    }

    HtmlBuilder html = new HtmlBuilder();

    String number = account.get(Account.NUMBER);
    if (Strings.isNotEmpty(number)) {
      html.appendParagraph(Lang.get("account.readOnly.number", number));
    }

    Glob bank = repository.findLinkTarget(account, Account.BANK);
    if (bank != null) {
      String bankName = localDirectory.get(DescriptionService.class).getStringifier(Bank.TYPE)
        .toString(bank, repository);
      if (Strings.isNotEmpty(bankName)) {
        html.appendParagraph(Lang.get("account.readOnly.bank", bankName));
      }
    }

    Double position = account.get(Account.POSITION_WITH_PENDING);
    if (position != null) {
      String positionLabel = Formatting.toString(position);
      Date date = account.get(Account.POSITION_DATE);
      if (date != null) {
        String dateLabel = Formatting.toString(date);
        html.appendParagraph(Lang.get("account.readOnly.positionAndDate", positionLabel, dateLabel));
      }
      else {
        html.appendParagraph(Lang.get("account.readOnly.position", positionLabel));
      }
    }

    return html.toString();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setAccount(Glob account) {
    super.setAccount(account);
    readOnlyDescription.setText(getReadOnlyDescription(account, localRepository));
  }

  public void clearAccount(String text) {
    readOnlyDescription.setText(text);
    cards.show("description");
  }

  public void setEditable(boolean editable) {
    cards.show(editable ? "edition" : "description");
    super.setEditable(editable);
  }

  public void dispose() {
    builder.dispose();
  }
}
