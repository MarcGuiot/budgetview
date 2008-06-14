package org.designup.picsou.gui.categories;

import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;

import java.util.HashMap;
import java.util.Map;

class CategoryExpansionModel implements GlobMatcher, ChangeSetListener {

  private Map<Integer, Boolean> expandedMap = new HashMap<Integer, Boolean>();
  private Map<Integer, Boolean> expandableMap = new HashMap<Integer, Boolean>();
  private GlobRepository repository;
  private CategoryView view;

  public CategoryExpansionModel(GlobRepository repository, CategoryView view) {
    this.repository = repository;
    this.view = view;
    repository.addChangeListener(this);
    for (MasterCategory master : MasterCategory.values()) {
      expandedMap.put(master.getId(), false);
    }
    this.view.setFilter(this);
    updateExpandabilities();
  }

  private void updateExpandabilities() {
    for (MasterCategory master : MasterCategory.values()) {
      Integer categoryId = master.getId();
      boolean expandable = Category.hasChildren(categoryId, repository);
      expandableMap.put(categoryId, expandable);
      if (!expandable) {
        expandedMap.put(categoryId, false);
      }
    }
  }

  public void toggleExpansion(Glob category) {
    if (!isExpandable(category)) {
      return;
    }
    Integer categoryId = category.get(Category.ID);
    Boolean existingValue = expandedMap.get(categoryId);
    expandedMap.put(categoryId, !existingValue.booleanValue());
    view.setFilter(this);
    view.select(category);
  }

  public boolean isExpanded(Glob category) {
    if (!Category.isMaster(category)) {
      return false;
    }
    Integer categoryId = category.get(Category.ID);
    return expandedMap.get(categoryId);
  }

  public boolean isExpandable(Glob category) {
    if (category == null) {
      return false;
    }
    Boolean status = expandableMap.get(category.get(Category.ID));
    if (status == null) {
      return false;
    }
    return status;
  }

  public boolean matches(Glob category, GlobRepository repository) {
    if (Category.isMaster(category)) {
      return true;
    }
    Integer masterId = category.get(Category.MASTER);
    if (masterId == null) {
      return true;
    }
    return expandedMap.get(masterId);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsCreationsOrDeletions(Category.TYPE)) {
      return;
    }
    updateExpandabilities();
    java.util.List<Key> createdList = changeSet.getCreated(Category.TYPE);
    for (Key key : createdList) {
      Glob created = repository.get(key);
      expandedMap.put(created.get(Category.MASTER), true);
    }
    view.setFilter(this);
  }

  public void globsReset(GlobRepository repository, java.util.List<GlobType> changedTypes) {
    if (changedTypes.contains(Category.TYPE)) {
      updateExpandabilities();
    }
  }
}
