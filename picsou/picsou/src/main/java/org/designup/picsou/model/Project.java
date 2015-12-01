package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.designup.picsou.gui.model.ProjectStat;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.collections.Range;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class Project {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  @Required
  @DefaultString("")
  public static StringField NAME;

  @Target(SeriesGroup.class)
  @Required
  public static LinkField SERIES_GROUP;

  @DefaultBoolean(true)
  public static BooleanField ACTIVE;

  @Target(Picture.class)
  public static LinkField PICTURE;

  /**
   * @deprecated
   */
  @Target(Series.class)
  public static LinkField SERIES;

  static {
    GlobTypeLoader.init(Project.class, "project");
  }

  public static Glob findProjectForGroup(Glob seriesGroup, GlobRepository repository) {
    return repository.findUnique(Project.TYPE, fieldEquals(Project.SERIES_GROUP, seriesGroup.get(SeriesGroup.ID)));
  }

  public static Glob findProject(Glob series, GlobRepository repository) {
    if (series == null) {
      return null;
    }
    GlobList items = repository.getAll(ProjectItem.TYPE, linkedTo(series, ProjectItem.SERIES));
    if (items.isEmpty()) {
      return null;
    }
    if (items.size() > 1) {
      throw new UnexpectedApplicationState("More than 1 project item for series " + series + " : " + items);
    }
    return repository.findLinkTarget(items.getFirst(), ProjectItem.PROJECT);
  }

  public static Range<Integer> getMonthRange(Glob project, GlobRepository repository) {
    Glob stat = repository.find(org.globsframework.model.Key.create(ProjectStat.TYPE, project.get(Project.ID)));
    if (stat == null) {
      return null;
    }
    Integer firstMonth = stat.get(ProjectStat.FIRST_MONTH);
    Integer lastMonth = stat.get(ProjectStat.LAST_MONTH);
    if (firstMonth == null || lastMonth == null) {
      return null;
    }
    return new Range<Integer>(firstMonth, lastMonth);
  }

  public static void sortItems(Glob project, GlobRepository repository) {
    GlobList items = repository.findLinkedTo(project, ProjectItem.PROJECT)
      .sortSelf(new GlobFieldsComparator(ProjectItem.FIRST_MONTH, true,
                                         ProjectItem.ID, true));
    int sequenceNumber = 0;
    for (Glob item : items) {
      repository.update(item.getKey(), ProjectItem.SEQUENCE_NUMBER, sequenceNumber++);
    }
  }

  public static Set<Integer> getSeriesIds(GlobList projects, GlobRepository repository) {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob project : projects) {
      seriesIds.addAll(getSeriesIds(project, repository));
    }
    return seriesIds;
  }

  public static Set<Integer> getSeriesIds(Glob project, GlobRepository repository) {
    return repository.findLinkedTo(project, ProjectItem.PROJECT).getValueSet(ProjectItem.SERIES);
  }

  public static void duplicate(Glob project, String newProjectName, int offset, GlobRepository repository) {
    repository.startChangeSet();
    try {
      Glob newProject = repository.create(TYPE,
                                          value(NAME, newProjectName),
                                          value(ACTIVE, true),
                                          value(PICTURE, project.get(PICTURE)));
      for (Glob item : repository.findLinkedTo(project, ProjectItem.PROJECT)) {
        ProjectItem.duplicate(item, item.get(ProjectItem.LABEL), newProject, offset, repository);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static void deleteAll(org.globsframework.model.Key key, GlobRepository repository) {
    repository.startChangeSet();
    try {
      Glob project = repository.find(key);
      if (project != null) {
        SeriesGroup.deleteAll(repository.findLinkTarget(project, Project.SERIES_GROUP), repository);
        for (Glob item : repository.findLinkedTo(project, ProjectItem.PROJECT)) {
          ProjectItem.deleteAll(item, repository);
        }
        repository.delete(ProjectStat.TYPE, linkedTo(project, ProjectStat.PROJECT));
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 5;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 5) {
        deserializeDataV5(fieldSetter, data);
      }
      else if (version == 4) {
        deserializeDataV4(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Project.NAME));
      output.writeInteger(fieldValues.get(Project.SERIES_GROUP));
      output.writeBoolean(fieldValues.get(Project.ACTIVE));
      output.writeInteger(fieldValues.get(Project.PICTURE));
      return serializedByteArrayOutput.toByteArray();
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES_GROUP, input.readInteger());
      fieldSetter.set(Project.ACTIVE, input.readBoolean());
      fieldSetter.set(Project.PICTURE, input.readInteger());
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES_GROUP, input.readInteger());
      fieldSetter.set(Project.ACTIVE, input.readBoolean());
      fieldSetter.set(Project.PICTURE, input.readInteger());
      input.readInteger(); // Project.DEFAULT_ACCOUNT
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES_GROUP, input.readInteger());
      fieldSetter.set(Project.ACTIVE, input.readBoolean());
      fieldSetter.set(Project.PICTURE, input.readInteger());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES, input.readInteger());
      fieldSetter.set(Project.ACTIVE, input.readBoolean());
      fieldSetter.set(Project.PICTURE, input.readInteger());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Project.NAME, input.readUtf8String());
      fieldSetter.set(Project.SERIES, input.readInteger());
      input.readDouble(); // Project.TOTAL_AMOUNT not used anymore;
    }
  }
}
