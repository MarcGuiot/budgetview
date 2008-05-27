package org.designup.picsou.gui.title;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.designup.picsou.gui.View;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;

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

  public void registerComponents(SplitsBuilder builder) {
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
      int month = Month.toMonth(months[0]);
      int year = Month.toYear(months[0]);
      return Lang.get("month." + Month.toMonth(month) + ".long").toLowerCase() + " " + year;
    }
    return "";
  }

}
