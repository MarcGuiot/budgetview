package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.index.UniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.Range;
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
  @Required
  public static LinkField SERIES;

  @DefaultBoolean(true)
  public static BooleanField ACTIVE;

  public static StringField IMAGE_PATH;

  public static UniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(Project.class, "project");
    loader.defineUniqueIndex(SERIES_INDEX, SERIES);
  }

  public static Glob findProject(Glob series, GlobRepository repository) {
    if (series == null) {
      return null;
    }
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
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Project.NAME));
      output.writeInteger(fieldValues.get(Project.SERIES));
      output.writeBoolean(fieldValues.get(Project.ACTIVE));
      output.writeUtf8String(fieldValues.get(Project.IMAGE_PATH));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES, input.readInteger());
      fieldSetter.set(Project.ACTIVE, input.readBoolean());
      fieldSetter.set(Project.IMAGE_PATH, input.readUtf8String());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES, input.readInteger());
      input.readDouble(); // Project.TOTAL_AMOUNT not used anymore;
    }
  }
}
