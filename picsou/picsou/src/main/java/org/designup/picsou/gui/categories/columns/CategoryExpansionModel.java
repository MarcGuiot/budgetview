package org.designup.picsou.gui.categories.columns;

import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.components.expansion.TableExpansionModel;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.Category;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

public class CategoryExpansionModel extends TableExpansionModel {

  public CategoryExpansionModel(GlobRepository repository, CategoryView view) {
    super(Category.TYPE, Category.ID, repository, view);
  }

  protected GlobMatcher getMasterMatcher() {
    return PicsouMatchers.masterCategories();
  }

  protected boolean hasChildren(Integer id, GlobRepository repository) {
    return Category.hasChildren(id, repository);
  }

  public boolean isMaster(Glob glob) {
    return Category.isMaster(glob);
  }

  protected Integer getMasterId(Glob glob) {
    return glob.get(Category.MASTER);
  }

  public boolean isExpansionDisabled(Glob category) {
    return Category.isAll(category) || Category.isNone(category);
  }
}
