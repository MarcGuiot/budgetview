package org.globsframework.wicket.table;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import wicket.Component;

import java.io.Serializable;
import java.util.Comparator;

public interface GlobTableColumn extends Serializable {
  String getTitle();

  Component getComponent(String id,
                         String tableId,
                         Key key,
                         MutableFieldValues fieldValues,
                         int rowIndex,
                         Component row,
                         GlobRepository repository,
                         DescriptionService descriptionService);

  Comparator<Glob> getComparator();
}
