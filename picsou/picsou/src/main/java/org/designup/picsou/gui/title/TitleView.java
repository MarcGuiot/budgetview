package org.designup.picsou.gui.title;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TitleView extends View implements GlobSelectionListener {

  private JLabel label = new JLabel();
  private GlobList categories = GlobList.EMPTY;
  private Integer[] months = {};
  private GlobStringifier categoryStringifier;

  public TitleView(GlobRepository globRepository, Directory directory) {
    super(globRepository, directory);
    directory.get(SelectionService.class).addListener(this, Category.TYPE, Month.TYPE);
    categoryStringifier = directory.get(DescriptionService.class).getStringifier(Category.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("title", label);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Category.TYPE)) {
      categories = selection.getAll(Category.TYPE);
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      months = selection.getAll(Month.TYPE).getSortedArray(Month.ID);
    }
    updateLabel();
  }

  private void updateLabel() {
    if (categories.size() == 0) {
      label.setText(Lang.get("title.nocategory"));
      return;
    }
    if (months.length == 0) {
      label.setText(Lang.get("title.noperiod"));
      return;
    }
    String monthDesc = getMonthDesc();

    label.setText(getCategoryDesc() + (monthDesc.length() > 0 ? (" - " + monthDesc) : ""));
  }

  private String getCategoryDesc() {
    String categoryDesc = "";
    if (categories.size() > 1) {
      categoryDesc = Lang.get("title.multicategories");
    }
    else {
      Glob category = categories.get(0);
      if (category.get(Category.ID).equals(Category.ALL)) {
        categoryDesc = Lang.get("title.allcategories");
      }
      else if (category.get(Category.ID).equals(Category.NONE)) {
        categoryDesc = Lang.get("title.categorynone");
      }
      else {
        categoryDesc = Lang.get("category") + " '" + categoryStringifier.toString(category, repository) + "'";
      }
    }
    return categoryDesc;
  }

  private String getMonthDesc() {
    if (months.length == 1) {
      return Month.getLabel(months[0]);
    }
    return "";
  }
}
