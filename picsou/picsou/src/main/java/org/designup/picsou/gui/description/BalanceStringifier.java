package org.designup.picsou.gui.description;

import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
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
  private ColorService colorService;

  public BalanceStringifier(GlobRepository repository, Directory directory) {
    this.repository = repository;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public String toString(GlobList transactions, GlobRepository repository) {
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
      .append(stringifyNumber(total, this.repository))
      .append("</font>")
      .toString();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    positiveAmountColor = colorLocator.get(PicsouColors.BALANCE_POSITIVE);
    negativeAmountColor = colorLocator.get(PicsouColors.BALANCE_NEGATIVE);
  }

  private String stringifyNumber(double value, GlobRepository globRepository) {
    Glob globForNumber = GlobBuilder.init(Transaction.TYPE).set(Transaction.AMOUNT, value).get();
    String amount = amountStringifier.toString(globForNumber, globRepository);
    return value > 0 ? "+" + amount : amount;
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
