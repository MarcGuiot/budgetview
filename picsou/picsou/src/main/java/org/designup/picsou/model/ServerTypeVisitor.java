package org.designup.picsou.model;

import org.globsframework.model.FieldValues;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public interface ServerTypeVisitor {

  void visitTransaction(Key key, FieldValues values) throws Exception;

  void visitBank(Key key, FieldValues values) throws Exception;

  void visitAccount(Key key, FieldValues values) throws Exception;

  void visitTransactionToCategory(Key key, FieldValues values) throws Exception;

  void visitLabelToCategory(Key key, FieldValues values) throws Exception;

  void visitImport(Key key, FieldValues values) throws Exception;

  void visitCategory(Key key, FieldValues values) throws Exception;

  void visitOther(Key key, FieldValues values) throws Exception;

  public static class Visitor {
    public static void safeVisit(Key key, FieldValues values, ServerTypeVisitor visitor) {
      try {
        if (key.getGlobType() == Transaction.TYPE) {
          visitor.visitTransaction(key, values);
        }
        else if (key.getGlobType() == Bank.TYPE) {
          visitor.visitBank(key, values);
        }
        else if (key.getGlobType() == Account.TYPE) {
          visitor.visitAccount(key, values);
        }
        else if (key.getGlobType() == TransactionToCategory.TYPE) {
          visitor.visitTransactionToCategory(key, values);
        }
        else if (key.getGlobType() == LabelToCategory.TYPE) {
          visitor.visitLabelToCategory(key, values);
        }
        else if (key.getGlobType() == TransactionImport.TYPE) {
          visitor.visitImport(key, values);
        }
        else if (key.getGlobType() == Category.TYPE) {
          visitor.visitCategory(key, values);
        }
        else {
          visitor.visitOther(key, values);
        }
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }
}
