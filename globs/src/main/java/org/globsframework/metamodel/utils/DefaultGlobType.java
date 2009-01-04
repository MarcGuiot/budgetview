package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.index.Index;
import org.globsframework.metamodel.index.MultiFieldIndex;
import org.globsframework.model.GlobList;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemAlreadyExists;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.lang.annotation.Annotation;
import java.util.*;

public class DefaultGlobType extends AnnotatedPropertyHolder<GlobType> implements MutableGlobType {
  private Map<String, Field> fieldsByName = new TreeMap<String, Field>();
  private Field[] fields;
  private Field[] keyFields = new Field[0];
  private String name;
  private Map<String, Link> outboundLinksByName = new HashMap<String, Link>();
  private List<Link> inboundLinks = new ArrayList<Link>();
  private GlobList constants = new GlobList();
  private Map<String, Index> indices = new HashMap<String, Index>(2, 1);
  private Map<String, MultiFieldIndex> multiFieldIndices = new HashMap<String, MultiFieldIndex>(2, 1);

  public DefaultGlobType(String name, Map<Class<? extends Annotation>, Annotation> annotationsByClass) {
    super(annotationsByClass);
    this.name = name;
  }

  public int getFieldCount() {
    return fieldsByName.size();
  }

  public Field getField(String name) throws ItemNotFound {
    Field field = fieldsByName.get(name);
    if (field == null) {
      throw new ItemNotFound("Field '" + name + "' not found in type: " + this.name);
    }
    return field;
  }

  public boolean hasField(String name) {
    return findField(name) != null;
  }

  public Field findField(String name) {
    return fieldsByName.get(name);
  }

  public Field[] getFields() {
    return fields;
  }

  public Field getField(int index) {
    return fields[index];
  }

  public String getName() {
    return name;
  }

  public void addField(AbstractField field) {
    if (hasField(field.getName())) {
      throw new ItemAlreadyExists("Field " + field.getName() +
                                  " declared twice for type " + getName());
    }
    field.setIndex(fieldsByName.size());
    fieldsByName.put(field.getName(), field);
  }

  public void addKey(Field field) {
    Field[] tmp = new Field[keyFields.length + 1];
    System.arraycopy(keyFields, 0, tmp, 0, keyFields.length);
    keyFields = tmp;
    keyFields[keyFields.length - 1] = field;
  }

  public Field[] getKeyFields() {
    return keyFields;
  }

  public boolean isKeyField(Field field) {
    for (Field currentField : keyFields) {
      if (currentField == field) {
        return true;
      }
    }
    return false;
  }

  public Field[] getFieldsWithAnnotation(Class<? extends Annotation> annotationClass) {
    List<Field> result = new ArrayList<Field>();
    for (Field field : fieldsByName.values()) {
      if (field.hasAnnotation(annotationClass)) {
        result.add(field);
      }
    }
    return result.toArray(new Field[result.size()]);
  }

  public void addConstant(ReadOnlyGlob glob) {
    constants.add(glob);
  }

  public GlobList getConstants() {
    return new GlobList(constants);
  }

  public String toString() {
    return name;
  }

  public void addLink(Link link) {
    outboundLinksByName.put(link.getName(), link);
    DefaultGlobType target = (DefaultGlobType)link.getTargetType();
    target.inboundLinks.add(link);
  }

  public Link[] getOutboundLinks() {
    Collection<Link> links = outboundLinksByName.values();
    return links.toArray(new Link[links.size()]);
  }

  public Link getOutboundLink(String name) throws ItemNotFound {
    Link link = outboundLinksByName.get(name);
    if (link == null) {
      throw new ItemNotFound("Link '" + name + "' is not declared for type " + this.name);
    }
    return link;
  }

  public Link findOutboundLink(String name) {
    return outboundLinksByName.get(name);
  }

  public Link[] getInboundLinks() {
    return inboundLinks.toArray(new Link[inboundLinks.size()]);
  }

  public void completeInit() {
    if (keyFields.length == 0) {
      throw new InvalidParameter("GlobType " + name + " has no key field");
    }
    fields = new Field[fieldsByName.size()];
    for (Field field : fieldsByName.values()) {
      fields[field.getIndex()] = field;
    }
  }

  public void addIndex(Index index) {
    indices.put(index.getName(), index);
  }

  public void addIndex(MultiFieldIndex index) {
    multiFieldIndices.put(index.getName(), index);
  }

  public Collection<Index> getIndices() {
    return indices.values();
  }

  public Collection<MultiFieldIndex> getMultiFieldIndices() {
    return multiFieldIndices.values();
  }

  public Index getIndex(String name) {
    return indices.get(name);
  }

  public MultiFieldIndex getMultiFieldIndex(String name) {
    return multiFieldIndices.get(name);
  }
}
