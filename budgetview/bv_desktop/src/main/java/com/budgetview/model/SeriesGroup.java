package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SeriesGroup {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @DefaultBoolean(true)
  public static BooleanField EXPANDED;

  static {
    TypeLoader.init(SeriesGroup.class, "seriesGroup");
  }

  public static BudgetArea getBudgetArea(Glob group) {
    return BudgetArea.get(group.get(BUDGET_AREA));
  }

  public static boolean isForProject(Glob group, GlobRepository repository) {
    return group != null && repository.contains(Project.TYPE, fieldEquals(Project.SERIES_GROUP, group.get(SeriesGroup.ID)));
  }

  public static GlobMatcher userCreatedGroups() {
    return new GlobMatcher() {
      public boolean matches(Glob group, GlobRepository repository) {
        return !isForProject(group, repository);
      }
    };
  }

  public static void deleteAll(Glob group, GlobRepository repository) {
    if (group == null) {
      return;
    }
    repository.startChangeSet();
    try {
      for (Glob series : repository.findLinkedTo(group, Series.GROUP)) {
        repository.update(series.getKey(), Series.GROUP, null);
      }
      repository.delete(group);
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeInteger(values.get(BUDGET_AREA));
      outputStream.writeBoolean(values.get(EXPANDED));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(BUDGET_AREA, input.readInteger());
      fieldSetter.set(EXPANDED, input.readBoolean());
    }
  }
}
