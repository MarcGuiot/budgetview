package org.crossbowlabs.globs.model.format;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;

public interface DescriptionService {

  Formats getFormats();

  String getLabel(GlobType type);

  String getLabel(Field field);

  String getLabel(Link link);

  GlobStringifier getStringifier(GlobType globType);

  GlobStringifier getStringifier(Field field);

  GlobStringifier getStringifier(Link link);
}
