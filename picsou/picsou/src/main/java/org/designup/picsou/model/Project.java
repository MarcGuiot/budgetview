package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Range;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.SortedSet;

public class Project {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  @Required
  @DefaultString("")
  public static StringField NAME;

  @Target(Series.class)
  public static LinkField SERIES;

  @DefaultDouble(0.0)
  @DoublePrecision(4)
  public static DoubleField TOTAL_AMOUNT;

  static {
    GlobTypeLoader.init(Project.class, "project");
  }

  public static Glob findProject(Glob series, GlobRepository repository) {
    GlobList projects = repository.getAll(Project.TYPE, GlobMatchers.linkedTo(series, Project.SERIES));
    if (projects.isEmpty()) {
      return null;
    }
    if (projects.size() > 1) {
      throw new UnexpectedApplicationState("More than 1 project for series " + series + " : " + projects);
    }
    return projects.getFirst();
  }

  public static Range<Integer> getMonthRange(Glob project, GlobRepository repository) {
    GlobList items = repository.findLinkedTo(project, ProjectItem.PROJECT);
    SortedSet<Integer> months = items.getSortedSet(ProjectItem.MONTH);
    if (months.isEmpty()) {
      return null;
    }
    return new Range<Integer>(months.first(), months.last());
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Project.NAME));
      output.writeInteger(fieldValues.get(Project.SERIES));
      output.writeDouble(fieldValues.get(Project.TOTAL_AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES, input.readInteger());
      fieldSetter.set(Project.TOTAL_AMOUNT, input.readDouble());
    }
  }
}
