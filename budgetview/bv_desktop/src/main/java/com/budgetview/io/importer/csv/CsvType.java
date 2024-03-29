package com.budgetview.io.importer.csv;

import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public class CsvType {

  public static GlobType CSV_TYPE;

  @Key
  public static IntegerField ID;

  @NoObfuscation
  public static StringField USER_DATE;

  @NoObfuscation
  public static StringField BANK_DATE;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static StringField NOTE;

  @NoObfuscation
  public static StringField AMOUNT;

  @NoObfuscation
  public static StringField CREDIT;

  @NoObfuscation
  public static StringField DEBIT;

  @NoObfuscation
  public static StringField ENVELOPE;

  @NoObfuscation
  public static StringField SUB_ENVELOPE;

  @NoObfuscation
  public static StringField NOT_IMPORTED;

  public static Mapper[] getMappers(CsvReader.TextType type) {
    List<Mapper> mappers = new ArrayList<Mapper>();
    for (Mapper mapper : MAPPERS) {
      if (type == null || mapper.getTextType() == null || mapper.getTextType() == type){
        mappers.add(mapper);
      }
    }
    return mappers.toArray(new Mapper[mappers.size()]);
  }

  static {
    TypeLoader.init(CsvType.class, "csvType");
  }

  public interface CsvConverter {

    Object convert(Object previous, String newValue);

    CsvReader.TextType getType();
  }

  public static CsvConverter DATE_CONVERTER = new CsvConverter() {
    public String convert(Object previous, String newValue) {
      return newValue;
    }

    public CsvReader.TextType getType() {
      return CsvReader.TextType.DATE;
    }
  };

  private static Mapper[] MAPPERS = new Mapper[]{
    getMapper(NOT_IMPORTED, null, null),
    getMapper(USER_DATE, ImportedTransaction.DATE, DATE_CONVERTER,
              "user date", "date d'operation"),
    getMapper(BANK_DATE, ImportedTransaction.BANK_DATE, DATE_CONVERTER,
              "bank date", "date banque", "date de banque", "date valeur","date de valeur"),
    getMapper(LABEL, ImportedTransaction.QIF_M, new ConcatCsvConverter(),
              "label", "libelle"),
    getMapper(NOTE, ImportedTransaction.NOTE, new ConcatCsvConverter(),
              "note"),
    getMapper(AMOUNT, ImportedTransaction.AMOUNT, new CsvConverter() {
      public Object convert(Object previous, String newValue) {
        return Amounts.extractAmount(newValue);
      }

      public CsvReader.TextType getType() {
        return CsvReader.TextType.NUMBER;
      }

    }, "amount", "montant"),
    getMapper(DEBIT, ImportedTransaction.AMOUNT, new CsvConverter() {
      public Object convert(Object previous, String newValue) {
        if (Strings.isNullOrEmpty(newValue)) {
          return previous;
        }
        return -(Math.abs(Amounts.extractAmount(newValue)));
      }
      public CsvReader.TextType getType() {
        return CsvReader.TextType.NUMBER;
      }

    }, "debit"),
    getMapper(CREDIT, ImportedTransaction.AMOUNT, new CsvConverter() {
      public Object convert(Object previous, String newValue) {
        if (Strings.isNullOrEmpty(newValue)) {
          return previous;
        }
        return (Math.abs(Amounts.extractAmount(newValue)));
      }
      public CsvReader.TextType getType() {
        return CsvReader.TextType.NUMBER;
      }

    }, "credit"),
    getMapper(ENVELOPE, null, new ConcatCsvConverter(),
              "envelope", "enveloppe", "category", "categorie"),
    getMapper(SUB_ENVELOPE, null, new ConcatCsvConverter(),
              "sub envelope", "sub category", "sous enveloppe", "sous categorie")
  };

  private static Mapper getMapper(final StringField field, Field importedTransactionField, CsvConverter translate, String... defaultFieldNames) {
    String name = Lang.get("import.csv.field.mapper." + field.getName());
    return new Mapper(field, name, importedTransactionField, translate, defaultFieldNames);
  }

  public static class Mapper {
    public final StringField field;
    public final String name;
    public final Field importedTransactionField;
    public final CsvConverter converter;
    public final String[] defaultFieldNames;

    public Mapper(StringField field, String name, Field importedTransactionField, CsvConverter converter, String[] defaultFieldNames) {
      this.field = field;
      this.name = name;
      this.importedTransactionField = importedTransactionField;
      this.converter = converter;
      this.defaultFieldNames = defaultFieldNames;
    }

    public CsvReader.TextType getTextType() {
      if (converter == null){
        return null;
      }
      return converter.getType();
    }

    public String toString() {
      return name;
    }
  }
  
  public static Mapper get(StringField field) {
    for (Mapper mapper : MAPPERS) {
      if (mapper.field.equals(field)) {
        return mapper;
      }
    }
    return null;
  }

  public static Mapper get(String name) {
    for (Mapper mapper : MAPPERS) {
      if (mapper.field.getName().equals(name)) {
        return mapper;
      }
    }
    return null;
  }

  public static Mapper[] getMappers() {
    return MAPPERS;
  }

  private static class ConcatCsvConverter implements CsvConverter {
    public String convert(Object previous, String newValue) {
      return previous == null ? newValue : previous + " " + newValue;
    }

    public CsvReader.TextType getType() {
      return CsvReader.TextType.TEXT;
    }

  }
}
