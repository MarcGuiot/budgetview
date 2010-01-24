package org.globsframework.model.format;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;

public interface DescriptionService {

  Formats getFormats();

  String getLabel(GlobType type);

  String getLabel(Field field);

  String getLabel(Link link);

  GlobStringifier getStringifier(GlobType globType);

  GlobListStringifier getListStringifier(GlobType globType);

  GlobStringifier getStringifier(Field field);

  GlobListStringifier getListStringifier(Field field);
  
  GlobStringifier getStringifier(Link link);

  GlobStringifier getStringifier(LinkField link);

  GlobListStringifier getListStringifier(Link link);

  GlobListStringifier getListStringifier(LinkField link);

  GlobListStringifier getListStringifier(Link link, String textForEmptySelection, String textForMultipleValues);
}
