package org.designup.picsou.gui.importer.csv;

import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.utils.Strings;

public class CsvType {

  public static GlobType CSV_TYPE;

  @Key
  public static IntegerField ID;

  public static StringField DATE_OPERATION;

  public static StringField DATE_BANK;

  public static StringField LABEL;

  public static StringField NOTE;

  public static StringField AMOUNT;

  public static StringField CREDIT;

  public static StringField DEBIT;

  public static StringField ENVELOPE;

  public static StringField SUB_ENVELOPE;

  public static StringField NOT_IMPORTED;

  static {
    GlobTypeLoader.init(CsvType.class);
  }

  interface Translate {
    Translate NULL = new Translate() {
      public String getUpdate(Object previous, String newValue) {
        return newValue;
      }
    };

    Object getUpdate(Object previous, String newValue);
  }

  public static class Name {
    public final StringField field;
    public final String name;
    public final Field importTransactionField;
    public final Translate translate;


    public Name(StringField field, String name, Field importTransactionField, Translate translate) {
      this.field = field;
      this.name = name;
      this.importTransactionField = importTransactionField;
      this.translate = translate;
    }
  }

  public static Name[] getValues() {
    return new Name[]{getName(NOT_IMPORTED, null, null),
                      getName(DATE_OPERATION, ImportedTransaction.BANK_DATE, Translate.NULL),
                      getName(DATE_BANK, ImportedTransaction.DATE, Translate.NULL),
                      getName(LABEL, ImportedTransaction.QIF_M, new CancatTranslate()),
                      getName(NOTE, ImportedTransaction.NOTE, new CancatTranslate()),
                      getName(AMOUNT, ImportedTransaction.AMOUNT, new Translate() {
                        public Object getUpdate(Object previous, String newValue) {
                          return Amounts.extractAmount(newValue);
                        }
                      }),
                      getName(DEBIT, ImportedTransaction.AMOUNT, new Translate() {
                        public Object getUpdate(Object previous, String newValue) {
                          if (Strings.isNullOrEmpty(newValue)){
                            return previous;
                          }
                          return -(Math.abs(Amounts.extractAmount(newValue)));
                        }
                      }),
                      getName(CREDIT, ImportedTransaction.AMOUNT, new Translate() {
                        public Object getUpdate(Object previous, String newValue) {
                          if (Strings.isNullOrEmpty(newValue)){
                            return previous;
                          }
                          return (Math.abs(Amounts.extractAmount(newValue)));
                        }
                      }),
                      getName(ENVELOPE, null, null),
                      getName(SUB_ENVELOPE, null, null)};
  }

  private static Name getName(final StringField field, Field importTransactionField, Translate translate) {
    String name = Lang.get("importer.csv.field.name." + field.getName());
    return new Name(field, name, importTransactionField, translate);
  }

  private static class CancatTranslate implements Translate {
    public String getUpdate(Object previous, String newValue) {
      return previous == null ? newValue : previous + " " + newValue;
    }
  }
}
