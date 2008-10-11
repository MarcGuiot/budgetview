package org.designup.picsou.gui.description;

import org.designup.picsou.gui.utils.AmountColors;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

public class ColoredAmountStringifier implements GlobListStringifier {

  private DoubleField field;
  private AmountColors colors;
  private boolean forcePlus;

  public ColoredAmountStringifier(DoubleField field, boolean forcePlus, Directory directory) {
    this.field = field;
    this.forcePlus = forcePlus;
    this.colors = new AmountColors(directory);
  }

  public String toString(GlobList globs, GlobRepository repository) {
    if (globs.isEmpty()) {
      return "0";
    }
    double total = 0;
    for (Glob glob : globs) {
      total += glob.get(field);
    }

    StringBuilder builder = new StringBuilder();
    builder
      .append("<font color='")
      .append(colors.get(total))
      .append("'>");

    if (total == 0) {
      builder.append("0");
    }
    else if (forcePlus && (total > 0)) {
      builder.append("+").append(PicsouDescriptionService.DECIMAL_FORMAT.format(total));
    }
    else {
      builder.append(PicsouDescriptionService.DECIMAL_FORMAT.format(total));
    }

    builder.append("</font>");

    return builder.toString();

  }
}
