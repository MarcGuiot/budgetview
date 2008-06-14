package org.globsframework.gui.views.impl;

import org.globsframework.gui.views.LabelCustomizer;
import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Ref;

public class GlobLabelCustomizerFactory {

  private GlobLabelCustomizerFactory() {
    // static class
  }

  public static LabelCustomizer create(Field field,
                                       final GlobStringifier stringifier,
                                       final GlobRepository repository) {

    final Ref<LabelCustomizer> result = new Ref<LabelCustomizer>();
    field.safeVisit(new FieldVisitor() {
      public void visitInteger(IntegerField field) throws Exception {
        result.set(createNumberCellRenderer(stringifier, repository));
      }

      public void visitLong(LongField field) throws Exception {
        result.set(createNumberCellRenderer(stringifier, repository));
      }

      public void visitDouble(DoubleField field) throws Exception {
        result.set(createNumberCellRenderer(stringifier, repository));
      }

      public void visitString(StringField field) throws Exception {
        result.set(stringifier(stringifier, repository));
      }

      public void visitDate(DateField field) throws Exception {
        result.set(stringifier(stringifier, repository));
      }

      public void visitBoolean(BooleanField field) throws Exception {
        result.set(stringifier(stringifier, repository));
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        result.set(stringifier(stringifier, repository));
      }

      public void visitBlob(BlobField field) throws Exception {
        result.set(stringifier(stringifier, repository));
      }

      public void visitLink(LinkField field) throws Exception {
        visitInteger(field);
      }
    });
    return result.get();
  }

  private static LabelCustomizer createNumberCellRenderer(GlobStringifier stringifier, GlobRepository repository) {
    return chain(stringifier(stringifier, repository), alignRight());
  }
}
