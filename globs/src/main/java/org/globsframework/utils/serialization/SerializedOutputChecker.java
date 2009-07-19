package org.globsframework.utils.serialization;

import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;

import java.util.Date;

class SerializedOutputChecker implements SerializedOutput {
  private final DefaultSerializationOutput serializationOutput;

  public SerializedOutputChecker(DefaultSerializationOutput serializationOutput) {
    this.serializationOutput = serializationOutput;
  }

  public void writeGlob(Glob glob) {
    serializationOutput.writeJavaString("Glob");
    serializationOutput.writeGlob(glob);
  }

  public void writeChangeSet(ChangeSet changeSet) {
    serializationOutput.writeJavaString("ChangeSet");
    serializationOutput.writeChangeSet(changeSet);
  }

  public void write(int[] array) {
    serializationOutput.writeJavaString("int array");
    serializationOutput.write(array);
  }

  public void write(long[] array) {
    serializationOutput.writeJavaString("long array");
    serializationOutput.write(array);
  }

  public void writeDate(Date date) {
    serializationOutput.writeJavaString("Date");
    serializationOutput.writeDate(date);
  }

  public void write(int value) {
    serializationOutput.writeJavaString("int");
    serializationOutput.write(value);
  }

  public void writeInteger(Integer value) {
    serializationOutput.writeJavaString("Integer");
    serializationOutput.writeInteger(value);
  }

  public void write(double value) {
    serializationOutput.writeJavaString("double");
    serializationOutput.write(value);
  }

  public void writeDouble(Double value) {
    serializationOutput.writeJavaString("Double");
    serializationOutput.writeDouble(value);
  }

  public void writeJavaString(String value) {
    serializationOutput.writeJavaString("String");
    serializationOutput.writeJavaString(value);
  }

  public void writeUtf8String(String value) {
    serializationOutput.writeJavaString("StringUtf8");
    serializationOutput.writeUtf8String(value);
  }

  public void write(boolean value) {
    serializationOutput.writeJavaString("Boolean");
    serializationOutput.write(value);
  }

  public void writeBoolean(Boolean value) {
    serializationOutput.writeJavaString("Boolean");
    serializationOutput.writeBoolean(value);
  }

  public void write(long value) {
    serializationOutput.writeJavaString("long");
    serializationOutput.write(value);
  }

  public void writeLong(Long value) {
    serializationOutput.writeJavaString("Long");
    serializationOutput.writeLong(value);
  }

  public void writeByte(int value) {
    serializationOutput.writeJavaString("byte");
    serializationOutput.writeByte(value);
  }

  public void writeByte(byte value) {
    serializationOutput.writeJavaString("byte");
    serializationOutput.writeByte(value);
  }

  public void writeBytes(byte[] value) {
    serializationOutput.writeJavaString("Bytes");
    serializationOutput.writeBytes(value);
  }
}
