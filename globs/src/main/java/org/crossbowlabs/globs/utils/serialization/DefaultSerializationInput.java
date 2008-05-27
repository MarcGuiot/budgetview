package org.crossbowlabs.globs.utils.serialization;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.delta.*;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DefaultSerializationInput implements SerializedInput {
  private InputStream inputStream;

  DefaultSerializationInput(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public Glob readGlob(GlobModel model) {
    GlobType globType = model.getType(readString());
    GlobBuilder builder = GlobBuilder.init(globType);
    InputStreamFieldVisitor fieldVisitorInput = new InputStreamFieldVisitor(builder);
    for (Field field : globType.getFields()) {
      field.safeVisit(fieldVisitorInput);
    }
    return builder.get();
  }

  public ChangeSet readChangeSet(GlobModel model) {
    MutableChangeSet changeSet = new DefaultChangeSet();
    int count = readInteger();
    for (int i = 0; i < count; i++) {
      GlobType type = model.getType(readString());
      Key key = KeyBuilder.createFromValues(type, readValues(type));
      int state = readByte();
      FieldValues values = readValues(type);
      switch (state) {
        case 1:
          changeSet.processCreation(key, values);
          break;
        case 2:
          changeSet.processUpdate(key, values);
          break;
        case 3:
          changeSet.processDeletion(key, values);
          break;
        default:
          throw new UnexpectedApplicationState("Invalid state '" + state + "' undefined for: " + key);
      }
    }
    return changeSet;
  }

  public DeltaGlob readDeltaGlob(GlobModel model) {
    GlobType type = model.getType(readString());
    Key key = KeyBuilder.createFromValues(type, readValues(type));
    DefaultDeltaGlob defaultGlob = new DefaultDeltaGlob(key);

    DeltaState deltaState;
    int state = readByte();
    switch (state) {
      case 1:
        deltaState = DeltaState.CREATED;
        break;
      case 2:
        deltaState = DeltaState.UPDATED;
        break;
      case 3:
        deltaState = DeltaState.DELETED;
        break;
      default:
        throw new UnexpectedApplicationState("Invalid state '" + state + "' undefined for: " + key);
    }
    defaultGlob.setState(deltaState);
    defaultGlob.setValues(readValues(type));

    return defaultGlob;
  }

  private FieldValues readValues(GlobType type) {
    FieldValuesBuilder builder = FieldValuesBuilder.init();
    FieldReader fieldReader = new FieldReader(this, builder);
    int fieldCount = readNotNullInt();
    while (fieldCount != 0) {
      int fieldIndex = readNotNullInt();
      Field field = type.getField(fieldIndex);
      field.safeVisit(fieldReader);
      fieldCount--;
    }
    return builder.get();
  }

  public int[] readIntArray() {
    int length = readNotNullInt();
    int array[] = new int[length];
    for (int i = 0; i < array.length; i++) {
      array[i] = readNotNullInt();
    }
    return array;
  }

  public long[] readLongArray() {
    int length = readNotNullInt();
    long array[] = new long[length];
    for (int i = 0; i < array.length; i++) {
      array[i] = readNotNullLong();
    }
    return array;
  }

  static class FieldReader implements FieldVisitor {
    private DefaultSerializationInput input;
    private FieldValuesBuilder builder;

    public FieldReader(DefaultSerializationInput input, FieldValuesBuilder builder) {
      this.input = input;
      this.builder = builder;
    }

    public void visitInteger(IntegerField field) throws Exception {
      builder.set(field, input.readInteger());
    }

    public void visitDouble(DoubleField field) throws Exception {
      builder.set(field, input.readDouble());
    }

    public void visitString(StringField field) throws Exception {
      builder.set(field, input.readString());
    }

    public void visitDate(DateField field) throws Exception {
      builder.set(field, input.readDate());
    }

    public void visitBoolean(BooleanField field) throws Exception {
      builder.set(field, input.readBoolean());
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      builder.set(field, input.readDate());
    }

    public void visitBlob(BlobField field) throws Exception {
      builder.set(field, input.readBytes());
    }

    public void visitLong(LongField field) throws Exception {
      builder.set(field, input.readLong());
    }

    public void visitLink(LinkField field) throws Exception {
      builder.set(field, input.readInteger());
    }
  }

  public Date readDate() {
    long time = readNotNullLong();
    if (time == -1) {
      return null;
    }
    return new Date(time);
  }

  public Integer readInteger() {
    if (isNull()) {
      return null;
    }
    return readNotNullInt();
  }

  public int readNotNullInt() {
    try {
      return ((inputStream.read() & 0xFF)) +
             ((inputStream.read() & 0xFF) << 8) +
             ((inputStream.read() & 0xFF) << 16) +
             ((inputStream.read() & 0xFF) << 24);
    }
    catch (IOException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  private boolean isNull() {
    try {
      return inputStream.read() != 0;
    }
    catch (IOException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Double readDouble() {
    Long l = readLong();
    if (l == null) {
      return null;
    }
    return Double.longBitsToDouble(l);
  }

  public double readNotNullDouble() {
    return Double.longBitsToDouble(readNotNullLong());
  }

  public String readString() {
    byte[] bytes = readBytes();
    if (bytes == null) {
      return null;
    }
    return new String(bytes);
  }

  public Boolean readBoolean() {
    try {
      int i = inputStream.read();
      return i == 0 ? Boolean.FALSE : i == 1 ? Boolean.TRUE : null;
    }
    catch (IOException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Long readLong() {
    if (isNull()) {
      return null;
    }
    return readNotNullLong();
  }

  public long readNotNullLong() {
    try {
      return ((inputStream.read() & 0xFFL)) +
             ((inputStream.read() & 0xFFL) << 8) +
             ((inputStream.read() & 0xFFL) << 16) +
             ((inputStream.read() & 0xFFL) << 24) +
             ((inputStream.read() & 0xFFL) << 32) +
             ((inputStream.read() & 0xFFL) << 40) +
             ((inputStream.read() & 0xFFL) << 48) +
             ((inputStream.read() & 0xFFL) << 56);
    }
    catch (IOException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public byte readByte() {
    try {
      return (byte) (inputStream.read());
    }
    catch (IOException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public byte[] readBytes() {
    try {
      int length = readNotNullInt();
      if (length == -1) {
        return null;
      }
      int readed = 0;
      byte[] bytes = new byte[length];
      while (readed != length) {
        int readSize = inputStream.read(bytes, readed, length - readed);
        if (readSize == -1) {
          throw new InvalidData("Missing data in buffer expected " + length + " but was " + readed);
        }
        readed += readSize;
      }
      return bytes;
    }
    catch (IOException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  private class InputStreamFieldVisitor implements FieldVisitor {
    private GlobBuilder builder;

    public InputStreamFieldVisitor(GlobBuilder builder) {
      this.builder = builder;
    }

    public void visitInteger(IntegerField field) throws Exception {
      builder.set(field, readInteger());
    }

    public void visitLink(LinkField field) throws Exception {
      builder.set(field, readInteger());
    }

    public void visitDouble(DoubleField field) throws Exception {
      builder.set(field, readDouble());
    }

    public void visitString(StringField field) throws Exception {
      builder.set(field, readString());
    }

    public void visitDate(DateField field) throws Exception {
      builder.set(field, readDate());
    }

    public void visitBoolean(BooleanField field) throws Exception {
      builder.set(field, readBoolean());
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      builder.set(field, readDate());
    }

    public void visitBlob(BlobField field) throws Exception {
      builder.set(field, readBytes());
    }

    public void visitLong(LongField field) throws Exception {
      builder.set(field, readLong());
    }

    public Glob getValue() {
      return builder.get();
    }

  }
}
