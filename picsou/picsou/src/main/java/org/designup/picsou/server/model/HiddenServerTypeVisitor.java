package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public interface HiddenServerTypeVisitor {

  void visitHiddenTransaction() throws Exception;

  void visitHiddenBank() throws Exception;

  void visitHiddenAccount() throws Exception;

  void visitHiddenTransactionToCategory() throws Exception;

  void visitHiddenLabelToCategory() throws Exception;

  void visitHiddenImport() throws Exception;

  void visitHiddenCategory() throws Exception;

  void visitOther() throws Exception;

  public static class Visitor {
    public static void safeVisit(GlobType globType, HiddenServerTypeVisitor visitorHidden) {
      try {
        if (globType == HiddenTransaction.TYPE) {
          visitorHidden.visitHiddenTransaction();
        }
        else if (globType == HiddenBank.TYPE) {
          visitorHidden.visitHiddenBank();
        }
        else if (globType == HiddenAccount.TYPE) {
          visitorHidden.visitHiddenAccount();
        }
        else if (globType == HiddenTransactionToCategory.TYPE) {
          visitorHidden.visitHiddenTransactionToCategory();
        }
        else if (globType == HiddenLabelToCategory.TYPE) {
          visitorHidden.visitHiddenLabelToCategory();
        }
        else if (globType == HiddenImport.TYPE) {
          visitorHidden.visitHiddenImport();
        }
        else if (globType == HiddenCategory.TYPE) {
          visitorHidden.visitHiddenCategory();
        }
        else {
          visitorHidden.visitOther();
        }
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

}
