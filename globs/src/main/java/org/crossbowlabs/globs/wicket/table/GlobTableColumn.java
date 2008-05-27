package org.crossbowlabs.globs.wicket.table;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
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
