package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class BalanceStringifier implements GlobListStringifier, ColorChangeListener {
  private GlobStringifier amountStringifier;

  private Color positiveAmountColor;
  private Color negativeAmountColor;

  private GlobRepository repository;

  public BalanceStringifier(GlobRepository repository, Directory directory) {
    this.repository = repository;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    directory.get(ColorService.class).addListener(this);
  }

  public String toString(GlobList transactions) {
    if (transactions.isEmpty()) {
      return " ";
    }
    double total = 0;
    for (Glob glob : transactions) {
      total += glob.get(Transaction.AMOUNT);
    }
    return new StringBuilder()
      .append("<html><font size='3' color='")
      .append(Colors.toString(total > 0 ? positiveAmountColor : negativeAmountColor))
      .append("'>")
      .append(stringifyNumber(total, repository))
      .append("</font>")
      .toString();
  }

  public void colorsChanged(ColorSource colorSource) {
    positiveAmountColor = colorSource.get(PicsouColors.BALANCE_POSITIVE);
    negativeAmountColor = colorSource.get(PicsouColors.BALANCE_NEGATIVE);
  }

  private String stringifyNumber(double value, GlobRepository globRepository) {
    Glob globForNumber = GlobBuilder.init(Transaction.TYPE).set(Transaction.AMOUNT, value).get();
    String amount = amountStringifier.toString(globForNumber, globRepository);
    return value > 0 ? "+" + amount : amount;
  }
}
