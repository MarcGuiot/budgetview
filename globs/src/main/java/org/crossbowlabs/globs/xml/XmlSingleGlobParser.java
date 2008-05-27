package org.crossbowlabs.globs.xml;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.MutableGlob;
import org.crossbowlabs.globs.model.impl.DefaultGlob;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.xml.sax.Attributes;

public class XmlSingleGlobParser {
  private static FieldConverter fieldConverter = new FieldConverter();

  private XmlSingleGlobParser() {
  }

  public interface GlobAdder {
    void add(MutableGlob mo);
  }

  public static void parse(String tagName,
                           Attributes xmlAttrs,
                           GlobModel globModel,
                           GlobAdder globAdder) throws Exception {
    GlobType globType = globModel.getType(tagName);
    DefaultGlob glob = new DefaultGlob(globType);
    processFields(glob, xmlAttrs, globType);
    globAdder.add(glob);
  }

  private static void processFields(DefaultGlob glob, Attributes xmlAttrs, GlobType globType) {
    int length = xmlAttrs.getLength();
    for (int i = 0; i < length; i++) {
      String name = xmlAttrs.getQName(i);
      String xmlValue = xmlAttrs.getValue(i);
      Field field = globType.findField(name);
      if (field == null) {
        throw new InvalidParameter("Unknown field '" + name + "' for type '" + globType.getName() + "'");
      }
      glob.setObject(field, fieldConverter.toObject(field, xmlValue));
    }
  }
}
