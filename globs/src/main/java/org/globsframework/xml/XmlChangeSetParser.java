package org.globsframework.xml;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.FieldValuesWithPreviousBuilder;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.saxstack.parser.*;
import org.xml.sax.Attributes;

import java.io.Reader;

public class XmlChangeSetParser {

  private XmlChangeSetParser() {
  }

  public static ChangeSet parse(GlobModel model, Reader reader) {
    RootProxyNode rootNode = new RootProxyNode(model);
    SaxStackParser.parse(new SAXParser(), new XmlBootstrapNode(rootNode, "changes"), reader);
    return rootNode.getChangeSet();
  }

  private static class RootProxyNode extends DefaultXmlNode {
    private FieldConverter fieldConverter = new FieldConverter();
    private MutableChangeSet changeSet = new DefaultChangeSet();
    private GlobModel model;

    private RootProxyNode(GlobModel model) {
      this.model = model;
    }

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) throws ExceptionHolder {
      String typeName = xmlAttrs.getValue("type");
      if (typeName == null) {
        throw new InvalidParameter("Missing attribute 'type' in tag '" + childName + "'");
      }
      GlobType globType = model.getType(typeName);
      FieldValuesWithPreviousBuilder valuesBuilder = FieldValuesWithPreviousBuilder.init(globType);
      final KeyBuilder keyBuilder = KeyBuilder.init(globType);

      processAttributes(keyBuilder, valuesBuilder, xmlAttrs, globType);

      final Key key = keyBuilder.get();
      if ("create".equals(childName)) {
        valuesBuilder.completeWithNulls();
        changeSet.processCreation(key, valuesBuilder.get());
      }
      else if ("update".equals(childName)) {
        valuesBuilder.completePreviousValues();
        changeSet.processUpdate(key, valuesBuilder.get());
      }
      else if ("delete".equals(childName)) {
        valuesBuilder.completeWithNulls();
        changeSet.processDeletion(key, valuesBuilder.get());
      }

      return super.getSubNode(typeName, xmlAttrs);
    }

    private void processAttributes(KeyBuilder keyBuilder,
                                   FieldValuesWithPreviousBuilder valuesBuilder,
                                   Attributes xmlAttrs,
                                   GlobType globType) {
      int length = xmlAttrs.getLength();
      for (int i = 0; i < length; i++) {
        String xmlAttrName = xmlAttrs.getQName(i);
        String xmlValue = xmlAttrs.getValue(i);

        if ("type".equals(xmlAttrName)) {
          continue;
        }

        if (xmlAttrName.startsWith("_")) {
          final String fieldName = xmlAttrName.substring(1);
          Field field = globType.findField(fieldName);
          if (field == null) {
            throw new ItemNotFound(
              "Unknown field '" + xmlAttrName + "' for type '" + globType.getName() + "'");
          }
          Object value = fieldConverter.toObject(field, xmlValue);
          if (field.isKeyField()) {
            throw new InvalidParameter("Cannot declare previous value for key field '" + field.getName() +
                                       "' on type: " + globType);
          }
          valuesBuilder.setPreviousValue(field, value);
        }
        else {
          Field field = globType.findField(xmlAttrName);
          if (field == null) {
            throw new ItemNotFound(
              "Unknown field '" + xmlAttrName + "' for type '" + globType.getName() + "'");
          }
          Object value = fieldConverter.toObject(field, xmlValue);
          if (field.isKeyField()) {
            keyBuilder.setValue(field, value);
          }
          else {
            valuesBuilder.setValue(field, value);
          }
        }
      }
    }

    public ChangeSet getChangeSet() {
      return changeSet;
    }
  }
}
