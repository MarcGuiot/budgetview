package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Project;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

public class ProjectAmountStringifier implements GlobListStringifier {

  public String toString(GlobList list, GlobRepository repository) {
    double total = 0.0;
    for (Glob project : list) {
      total += project.get(Project.TOTAL_AMOUNT);
    }
    return Formatting.toString(total, BudgetArea.EXTRAS);
  }
}
