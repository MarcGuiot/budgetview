package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorSource;
import org.crossbowlabs.splits.color.Colors;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;

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
