package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class ProjectItem {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Project.class)
  public static LinkField PROJECT;

  @Target(Account.class)
  public static LinkField ACCOUNT;

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

  /**
   * @deprecated *
   */
  @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  @Target(Picture.class)
  public static LinkField PICTURE;

  public static StringField URL;

  public static StringField DESCRIPTION;

  public static IntegerField SEQUENCE_NUMBER;

  public static NotUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(ProjectItem.class, "projectItem");
    loader.defineNonUniqueIndex(SERIES_INDEX, SERIES);
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
    Glob monthAmount = ProjectItemAmount.findUnique(item.get(ID), monthId, repository);
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
    Integer monthCount = itemValues.get(MONTH_COUNT);
    int offset = monthCount != null & monthCount > 1 ? monthCount - 1 : 0;
    return Month.offset(itemValues.get(FIRST_MONTH), offset);
  }

  public static boolean usesSavingsSeries(FieldValues itemValues) {
    return !usesExtrasSeries(itemValues);
  }

  public static boolean usesExtrasSeries(FieldValues itemValues) {
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

  public static GlobList getItemsForSeries(Integer seriesId, GlobRepository repository) {
    return repository.findByIndex(ProjectItem.SERIES_INDEX, seriesId);
  }

  public static Integer getNextSequenceNumber(Integer projectId, GlobRepository repository) {
    SortedSet<Integer> numbers = repository.getAll(TYPE, fieldEquals(PROJECT, projectId))
      .getSortedSet(SEQUENCE_NUMBER);
    if (numbers.isEmpty()) {
      return 0;
    }
    return numbers.last() + 1;
  }

  public static class Serializer implements PicsouGlobSerializer {
    public int getWriteVersion() {
      return 5;
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
      output.writeInteger(fieldValues.get(ProjectItem.SEQUENCE_NUMBER));
      output.writeInteger(fieldValues.get(ProjectItem.ACCOUNT));
      return serializedByteArrayOutput.toByteArray();
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

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
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
      fieldSetter.set(ProjectItem.SEQUENCE_NUMBER, input.readInteger());
      fieldSetter.set(ProjectItem.ACCOUNT, input.readInteger());
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
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
      fieldSetter.set(ProjectItem.SEQUENCE_NUMBER, input.readInteger());
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
      fieldSetter.set(ProjectItem.SEQUENCE_NUMBER, 0);
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
      fieldSetter.set(ProjectItem.SEQUENCE_NUMBER, 0);
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
      fieldSetter.set(ProjectItem.SEQUENCE_NUMBER, 0);
    }
  }
}
