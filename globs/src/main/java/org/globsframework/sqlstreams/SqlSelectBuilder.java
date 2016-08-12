package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.streams.accessors.*;
import org.globsframework.utils.Ref;

// attention sur le distinct : les valeurs de la clef sont automatiquement ajoutees  ==> faire un distinct a part

public interface SqlSelectBuilder {

  SqlSelectBuilder select(Field field);

  SqlSelectBuilder selectAll();

  SqlSelectBuilder select(IntegerField field, Ref<IntegerAccessor> accessor);

  SqlSelectBuilder select(LongField field, Ref<LongAccessor> accessor);

  SqlSelectBuilder select(BooleanField field, Ref<BooleanAccessor> accessor);

  SqlSelectBuilder select(StringField field, Ref<StringAccessor> accessor);

  SqlSelectBuilder select(DateField field, Ref<DateAccessor> accessor);

  SqlSelectBuilder select(DoubleField field, Ref<DoubleAccessor> accessor);

  SqlSelectBuilder select(TimeStampField field, Ref<TimestampAccessor> accessor);

  SqlSelectBuilder select(BlobField field, Ref<BlobAccessor> accessor);

  IntegerAccessor retrieve(IntegerField field);

  LongAccessor retrieve(LongField field);

  StringAccessor retrieve(StringField field);

  DateAccessor retrieve(DateField field);

  BooleanAccessor retrieve(BooleanField field);

  DoubleAccessor retrieve(DoubleField field);

  BlobAccessor retrieve(BlobField field);

  Accessor retrieveUnTyped(Field field);

  SqlSelect getQuery();

  SqlSelect getNotAutoCloseQuery();

  GlobList getList();

  Glob getUnique();
}
