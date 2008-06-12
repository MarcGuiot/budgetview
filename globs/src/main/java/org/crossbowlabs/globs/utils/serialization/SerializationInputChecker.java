package org.crossbowlabs.globs.utils.serialization;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.delta.DeltaGlob;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.util.Date;

public class SerializationInputChecker implements SerializedInput {
  private SerializedInput serializedInput;

  public SerializationInputChecker(SerializedInput serializedInput) {
    this.serializedInput = serializedInput;
  }

  public Glob readGlob(GlobModel model) {
    String value = serializedInput.readString();
    if ("Glob".equals(value)) {
      return serializedInput.readGlob(model);
    }
    else {
      throw new UnexpectedApplicationState("Glob expected but got " + value);
    }
  }

  public ChangeSet readChangeSet(GlobModel model) {
    String value = serializedInput.readString();
    if ("ChangeSet".equals(value)) {
      return serializedInput.readChangeSet(model);
    }
    else {
      throw new UnexpectedApplicationState("ChangeSet expected but got " + value);
    }
  }

  public DeltaGlob readDeltaGlob(GlobModel model) {
    String value = serializedInput.readString();
    if ("DeltaGlob".equals(value)) {
      return serializedInput.readDeltaGlob(model);
    }
    else {
      throw new UnexpectedApplicationState("DeltaGlob expected but got " + value);
    }
  }

  public int[] readIntArray() {
    String value = serializedInput.readString();
    if ("int array".equals(value)) {
      return serializedInput.readIntArray();
    }
    else {
      throw new UnexpectedApplicationState("int array expected but got " + value);
    }
  }

  public long[] readLongArray() {
    String value = serializedInput.readString();
    if ("long array".equals(value)) {
      return serializedInput.readLongArray();
    }
    else {
      throw new UnexpectedApplicationState("long array expected but got " + value);
    }

  }

  public Date readDate() {
    String value = serializedInput.readString();
    if ("Date".equals(value)) {
      return serializedInput.readDate();
    }
    else {
      throw new UnexpectedApplicationState("Date expected but got " + value);
    }
  }

  public Integer readInteger() {
    String value = serializedInput.readString();
    if ("Integer".equals(value)) {
      return serializedInput.readInteger();
    }
    else {
      throw new UnexpectedApplicationState("Integer expected but got " + value);
    }
  }

  public int readNotNullInt() {
    String value = serializedInput.readString();
    if ("int".equals(value)) {
      return serializedInput.readNotNullInt();
    }
    else {
      throw new UnexpectedApplicationState("int expected but got " + value);
    }
  }

  public Double readDouble() {
    String value = serializedInput.readString();
    if ("Double".equals(value)) {
      return serializedInput.readDouble();
    }
    else {
      throw new UnexpectedApplicationState("Double expected but got " + value);
    }
  }

  public double readNotNullDouble() {
    String value = serializedInput.readString();
    if ("double".equals(value)) {
      return serializedInput.readNotNullDouble();
    }
    else {
      throw new UnexpectedApplicationState("Double expected but got " + value);
    }
  }

  public String readString() {
    String value = serializedInput.readString();
    if ("String".equals(value)) {
      return serializedInput.readString();
    }
    else {
      throw new UnexpectedApplicationState("String expected but got " + value);
    }
  }

  public Boolean readBoolean() {
    String value = serializedInput.readString();
    if ("Boolean".equals(value)) {
      return serializedInput.readBoolean();
    }
    else {
      throw new UnexpectedApplicationState("Boolean expected but got " + value);
    }
  }

  public Long readLong() {
    String value = serializedInput.readString();
    if ("Long".equals(value)) {
      return serializedInput.readLong();
    }
    else {
      throw new UnexpectedApplicationState("Long expected but got " + value);
    }
  }

  public long readNotNullLong() {
    String value = serializedInput.readString();
    if ("long".equals(value)) {
      return serializedInput.readNotNullLong();
    }
    else {
      throw new UnexpectedApplicationState("long expected but got " + value);
    }
  }

  public byte readByte() {
    String value = serializedInput.readString();
    if ("byte".equals(value)) {
      return serializedInput.readByte();
    }
    else {
      throw new UnexpectedApplicationState("byte expected but got " + value);
    }
  }

  public byte[] readBytes() {
    String value = serializedInput.readString();
    if ("Bytes".equals(value)) {
      return serializedInput.readBytes();
    }
    else {
      throw new UnexpectedApplicationState("Bytes expected but got " + value);
    }
  }
}
