package org.globsframework.utils.serialization;

import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.delta.DeltaGlob;

import java.util.Date;

class SerializedOutputChecker implements SerializedOutput {
  private final DefaultSerializationOutput serializationOutput;

  public SerializedOutputChecker(DefaultSerializationOutput serializationOutput) {
    this.serializationOutput = serializationOutput;
  }

  public void writeGlob(Glob glob) {
    serializationOutput.writeString("Glob");
    serializationOutput.writeGlob(glob);
  }

  public void writeChangeSet(ChangeSet changeSet) {
    serializationOutput.writeString("ChangeSet");
    serializationOutput.writeChangeSet(changeSet);
  }

  public void writeDeltaGlob(DeltaGlob deltaGlob) {
    serializationOutput.writeString("DeltaGlob");
    serializationOutput.writeDeltaGlob(deltaGlob);
  }

  public void write(int[] array) {
    serializationOutput.writeString("int array");
    serializationOutput.write(array);
  }

  public void write(long[] array) {
    serializationOutput.writeString("long array");
    serializationOutput.write(array);
  }

  public void writeDate(Date date) {
    serializationOutput.writeString("Date");
    serializationOutput.writeDate(date);
  }

  public void write(int value) {
    serializationOutput.writeString("int");
    serializationOutput.write(value);
  }

  public void writeInteger(Integer value) {
    serializationOutput.writeString("Integer");
    serializationOutput.writeInteger(value);
  }

  public void write(double value) {
    serializationOutput.writeString("double");
    serializationOutput.write(value);
  }

  public void writeDouble(Double value) {
    serializationOutput.writeString("Double");
    serializationOutput.writeDouble(value);
  }

  public void writeString(String value) {
    serializationOutput.writeString("String");
    serializationOutput.writeString(value);
  }

  public void write(boolean value) {
    serializationOutput.writeString("Boolean");
    serializationOutput.write(value);
  }

  public void writeBoolean(Boolean value) {
    serializationOutput.writeString("Boolean");
    serializationOutput.writeBoolean(value);
  }

  public void write(long value) {
    serializationOutput.writeString("long");
    serializationOutput.write(value);
  }

  public void writeLong(Long value) {
    serializationOutput.writeString("Long");
    serializationOutput.writeLong(value);
  }

  public void writeByte(int value) {
    serializationOutput.writeString("byte");
    serializationOutput.writeByte(value);
  }

  public void writeByte(byte value) {
    serializationOutput.writeString("byte");
    serializationOutput.writeByte(value);
  }

  public void writeBytes(byte[] value) {
    serializationOutput.writeString("Bytes");
    serializationOutput.writeBytes(value);
  }
}
