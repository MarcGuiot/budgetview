package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.UniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class ProjectItem {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Project.class)
  public static LinkField PROJECT;

  @Target(ProjectItemType.class)
  @DefaultInteger(0)
  public static LinkField ITEM_TYPE;

  @NamingField
  @DefaultString("")
  public static StringField LABEL;

  @Target(Month.class)
  public static IntegerField FIRST_MONTH;

  @DefaultBoolean(true)
  public static BooleanField USE_SAME_AMOUNTS;

  @DefaultDouble(0.00)
  @DoublePrecision(4)
  public static DoubleField PLANNED_AMOUNT;

  @Required
  @DefaultInteger(1)
  public static IntegerField MONTH_COUNT;

  @DefaultBoolean(true)
  public static BooleanField ACTIVE;

  @Target(Series.class)
  public static LinkField SERIES;

  @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  @Target(Picture.class)
  public static LinkField PICTURE;

  public static StringField URL;

  public static StringField DESCRIPTION;

  public static UniqueIndex SERIES_INDEX;
  public static UniqueIndex SUB_SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(ProjectItem.class, "projectItem");
    loader.defineUniqueIndex(SERIES_INDEX, SERIES);
    loader.defineUniqueIndex(SUB_SERIES_INDEX, SUB_SERIES);
  }

  public static Double getTotalPlannedAmount(FieldValues projectItem, GlobRepository repository) {
    if (projectItem == null) {
      return null;
    }

    if (projectItem.isTrue(USE_SAME_AMOUNTS)) {
      Double planned = projectItem.get(ProjectItem.PLANNED_AMOUNT);
      Integer monthCount = projectItem.get(ProjectItem.MONTH_COUNT);
      if (planned == null || monthCount == null) {
        return 0.00;
      }
      return planned * monthCount;
    }

    return repository.findByIndex(ProjectItemAmount.PROJECT_ITEM_INDEX, ProjectItemAmount.PROJECT_ITEM, projectItem.get(ID))
      .getGlobs()
      .getSum(ProjectItemAmount.PLANNED_AMOUNT);
  }

  public static Double getAmount(Glob item, int monthId, GlobRepository repository) {
    if (item.isTrue(USE_SAME_AMOUNTS)) {
      return item.get(PLANNED_AMOUNT, 0.00);
    }
    Glob monthAmount = repository.find(org.globsframework.model.Key.create(ProjectItemAmount.PROJECT_ITEM, item.get(ID),
                                                                           ProjectItemAmount.MONTH, monthId));
    return monthAmount != null ? monthAmount.get(ProjectItemAmount.PLANNED_AMOUNT) : 0.00;
  }

  public static ClosedMonthRange getMonthRange(FieldValues values) {
    Integer firstMonth = values.get(ProjectItem.FIRST_MONTH);
    Integer monthCount = values.get(ProjectItem.MONTH_COUNT);
    if ((firstMonth == null) || (monthCount == null)) {
      return null;
    }
    return new ClosedMonthRange(firstMonth, getLastMonth(values));
  }

  public static int getLastMonth(FieldValues itemValues) {
    return Month.offset(itemValues.get(FIRST_MONTH), itemValues.get(MONTH_COUNT) - 1);
  }

  public static boolean usesSeries(FieldValues itemValues) {
    return !usesSubSeries(itemValues);
  }

  public static boolean usesSubSeries(FieldValues itemValues) {
    return !Utils.equal(ProjectItemType.TRANSFER.getId(), itemValues.get(ITEM_TYPE));
  }

  public static Glob findProjectItem(Glob series, GlobRepository repository) {
    if (series == null) {
      return null;
    }
    GlobList items = repository.getAll(ProjectItem.TYPE, GlobMatchers.linkedTo(series, ProjectItem.SERIES));
    if (items.isEmpty()) {
      return null;
    }
    if (items.size() > 1) {
      throw new UnexpectedApplicationState("More than 1 project for series " + series + " : " + items);
    }
    return items.getFirst();
  }

  public static class Serializer implements PicsouGlobSerializer {
    public int getWriteVersion() {
      return 3;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(ProjectItem.PROJECT));
      output.writeInteger(fieldValues.get(ProjectItem.ITEM_TYPE));
      output.writeUtf8String(fieldValues.get(ProjectItem.LABEL));
      output.writeInteger(fieldValues.get(ProjectItem.FIRST_MONTH));
      output.writeBoolean(fieldValues.get(ProjectItem.USE_SAME_AMOUNTS));
      output.writeDouble(fieldValues.get(ProjectItem.PLANNED_AMOUNT));
      output.writeInteger(fieldValues.get(ProjectItem.MONTH_COUNT));
      output.writeBoolean(fieldValues.get(ProjectItem.ACTIVE));
      output.writeInteger(fieldValues.get(ProjectItem.SERIES));
      output.writeInteger(fieldValues.get(ProjectItem.SUB_SERIES));
      output.writeInteger(fieldValues.get(ProjectItem.PICTURE));
      output.writeUtf8String(fieldValues.get(ProjectItem.URL));
      output.writeUtf8String(fieldValues.get(ProjectItem.DESCRIPTION));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.ITEM_TYPE, input.readInteger());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.FIRST_MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.USE_SAME_AMOUNTS, input.readBoolean());
      fieldSetter.set(ProjectItem.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.MONTH_COUNT, input.readInteger());
      fieldSetter.set(ProjectItem.ACTIVE, input.readBoolean());
      fieldSetter.set(ProjectItem.SERIES, input.readInteger());
      fieldSetter.set(ProjectItem.SUB_SERIES, input.readInteger());
      fieldSetter.set(ProjectItem.PICTURE, input.readInteger());
      fieldSetter.set(ProjectItem.URL, input.readUtf8String());
      fieldSetter.set(ProjectItem.DESCRIPTION, input.readUtf8String());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.ITEM_TYPE, ProjectItemType.EXPENSE.getId());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.FIRST_MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.USE_SAME_AMOUNTS, true);
      fieldSetter.set(ProjectItem.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.MONTH_COUNT, 1);
      fieldSetter.set(ProjectItem.ACTIVE, true);
      fieldSetter.set(ProjectItem.SERIES, null);
      fieldSetter.set(ProjectItem.SUB_SERIES, input.readInteger());
      fieldSetter.set(ProjectItem.PICTURE, null);
      fieldSetter.set(ProjectItem.URL, null);
      fieldSetter.set(ProjectItem.DESCRIPTION, null);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.ITEM_TYPE, ProjectItemType.EXPENSE.getId());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.FIRST_MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.USE_SAME_AMOUNTS, true);
      fieldSetter.set(ProjectItem.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.MONTH_COUNT, 1);
      fieldSetter.set(ProjectItem.ACTIVE, true);
      fieldSetter.set(ProjectItem.SERIES, null);
      fieldSetter.set(ProjectItem.SUB_SERIES, null);
      fieldSetter.set(ProjectItem.PICTURE, null);
      fieldSetter.set(ProjectItem.URL, null);
      fieldSetter.set(ProjectItem.DESCRIPTION, null);
    }
  }
}
