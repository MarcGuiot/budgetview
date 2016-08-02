package com.budgetview.desktop.accounts;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.model.Account;
import com.budgetview.model.Bank;
import com.budgetview.utils.HtmlBuilder;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class AccountEditionPanel extends AbstractAccountPanel<GlobRepository> {
  private CardHandler cards;
  private GlobsPanelBuilder builder;

  public AccountEditionPanel(Window owner, GlobRepository repository, Directory parentDirectory) {
    super(repository, parentDirectory);
    createPanel(owner);
  }

  private void createPanel(Window owner) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountEditionPanel.splits", localRepository,
                                    localDirectory);
    createComponents(builder, owner, Account.LAST_IMPORT_POSITION);
    cards = builder.addCardHandler("cards");
    cards.show("editable");
    builder.addHtmlView("readOnlyDescription", Account.TYPE, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.size() != 1) {
          return "";
        }
        return getReadOnlyDescription(list.get(0), repository);
      }
    });
    builder.load();
  }

  private String getReadOnlyDescription(Glob account, GlobRepository repository) {

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
    Date date = account.get(Account.POSITION_DATE);
    if (position != null) {
      String positionLabel = Formatting.toString(position);
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

  public void setEditable(boolean editable) {
    cards.show(editable ? "editable" : "readonly");
    super.setEditable(editable);
  }

  public void dispose() {
    builder.dispose();
  }
}
