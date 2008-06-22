package org.globsframework.gui.splits.styles;

import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Selector {

  public final String typeName;
  public final String componentName;
  public final String styleClassName;

  public static final String FORMAT_MESSAGE =
    "Selector format must be a sequence of:  <type>  [<type>]#<name>  [<type>].<styleClass>";

  private static final Pattern pattern = Pattern.compile("[\\w]*[\\.#]?[\\w]*");

  public Selector(String typeName, String componentName, String styleClassName) {
    this.typeName = Strings.nullIfEmpty(typeName);
    this.componentName = Strings.nullIfEmpty(componentName);
    this.styleClassName = Strings.nullIfEmpty(styleClassName);
  }

  public boolean matches(Selector other) {
    if ((typeName != null) && !typeName.equals(other.typeName)) {
      return false;
    }
    if ((componentName != null) && !componentName.equals(other.componentName)) {
      return false;
    }
    if ((styleClassName != null) && !styleClassName.equals(other.styleClassName)) {
      return false;
    }
    return true;
  }

  public static Selector[] parseSequence(String selector) {
    List<Selector> result = new ArrayList<Selector>();
    String[] parts = selector.trim().split("[\\s]+");
    for (String part : parts) {
      result.add(parseSingle(part));
    }
    return result.toArray(new Selector[result.size()]);
  }

  public static Selector parseSingle(String part) {
    Matcher matcher = pattern.matcher(part);
    if (!matcher.matches()) {
      throwInvalidFormat(part);
    }
    if (part.contains(".")) {
      int index = part.indexOf(".");
      String type = part.substring(0, index);
      String styleClass = part.substring(index + 1);
      if (Strings.isNullOrEmpty(styleClass)) {
        throwInvalidFormat(part);
      }
      return new Selector(type, null, styleClass);
    }
    else if (part.contains("#")) {
      int index = part.indexOf("#");
      String type = part.substring(0, index);
      String componentName = part.substring(index + 1);
      if (Strings.isNullOrEmpty(componentName)) {
        throwInvalidFormat(part);
      }
      return new Selector(type, componentName, null);
    }
    else {
      return new Selector(part, null, null);
    }
  }

  private static void throwInvalidFormat(String selector) {
    throw new InvalidFormat("Invalid selector: '" + selector + "' - " + FORMAT_MESSAGE);
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (Strings.isNotEmpty(typeName)) {
      builder.append(typeName);
    }
    if (Strings.isNotEmpty(componentName)) {
      builder.append('#').append(componentName);
    }
    if (Strings.isNotEmpty(styleClassName)) {
      builder.append('.').append(styleClassName);
    }
    return builder.toString();
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Selector selector = (Selector)o;

    if (componentName != null ? !componentName.equals(selector.componentName) : selector.componentName != null) {
      return false;
    }
    if (styleClassName != null ? !styleClassName.equals(selector.styleClassName) : selector.styleClassName != null) {
      return false;
    }
    if (typeName != null ? !typeName.equals(selector.typeName) : selector.typeName != null) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = (typeName != null ? typeName.hashCode() : 0);
    result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
    result = 31 * result + (styleClassName != null ? styleClassName.hashCode() : 0);
    return result;
  }
}
