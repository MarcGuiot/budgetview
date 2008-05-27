package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.streams.accessors.*;
import org.crossbowlabs.globs.utils.Ref;

// attention sur le distinct : les valeurs de la clef sont automatiquement ajoutees  ==> faire un distinct a part

public interface SelectBuilder {

  SelectQuery getQuery();

  SelectBuilder select(Field field);

  SelectBuilder selectAll();

  SelectBuilder select(IntegerField field, Ref<IntegerAccessor> accessor);

  SelectBuilder select(LongField field, Ref<LongAccessor> accessor);

  SelectBuilder select(BooleanField field, Ref<BooleanAccessor> accessor);

  SelectBuilder select(StringField field, Ref<StringAccessor> accessor);

  SelectBuilder select(DateField field, Ref<DateAccessor> accessor);

  SelectBuilder select(DoubleField field, Ref<DoubleAccessor> accessor);

  SelectBuilder select(TimeStampField field, Ref<TimestampAccessor> accessor);

  SelectBuilder select(BlobField field, Ref<BlobAccessor> accessor);

  IntegerAccessor retrieve(IntegerField field);

  LongAccessor retrieve(LongField field);

  StringAccessor retrieve(StringField field);

  DateAccessor retrieve(DateField field);

  BooleanAccessor retrieve(BooleanField field);

  DoubleAccessor retrieve(DoubleField field);

  BlobAccessor retrieve(BlobField field);

  Accessor retrieveUnTyped(Field field);
}
