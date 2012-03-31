package org.designup.picsou.gui.transactions.utils;

import org.designup.picsou.gui.description.stringifiers.AccountListStringifier;
import org.designup.picsou.gui.description.stringifiers.MonthListStringifier;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

public class LegendStringifier implements GlobListStringifier {

  private GlobListStringifier accountStringifier =
    new AccountListStringifier(Lang.get("accountList.main"));
  private GlobListStringifier monthStringifier =
    new MonthListStringifier();
  private SelectionService selectionService;

  public LegendStringifier(Directory directory) {
    selectionService = directory.get(SelectionService.class);
  }

  public String toString(GlobList list, GlobRepository repository) {
    StringBuilder builder = new StringBuilder();
    builder.append(accountStringifier.toString(list, repository));
    builder.append(" - ");
    builder.append(getMonthSelectionString(repository));
    return builder.toString();
  }

  private String getMonthSelectionString(GlobRepository repository) {
    GlobList months = selectionService.getSelection(Month.TYPE);
    return monthStringifier.toString(months, repository).toLowerCase();
  }


}
