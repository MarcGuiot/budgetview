package org.crossbowlabs.globs.remote;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.sqlstreams.SelectBuilder;
import org.crossbowlabs.globs.sqlstreams.SelectQuery;
import org.crossbowlabs.globs.streams.accessors.*;
import org.crossbowlabs.globs.utils.Ref;

class SerializedSelectBuilder implements SelectBuilder {
  private ClientDataAccess.Proxy proxy;

  public SerializedSelectBuilder(ClientDataAccess.Proxy proxy) {
    this.proxy = proxy;
  }

  public SelectQuery getQuery() {
    return new SerializedQuery(proxy, this);
  }

  public SelectBuilder select(Field field) {
    return null;
  }

  public SelectBuilder selectAll() {
    return null;
  }

  public SelectBuilder select(IntegerField field, Ref<IntegerAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(LongField field, Ref<LongAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(BooleanField field, Ref<BooleanAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(StringField field, Ref<StringAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(DateField field, Ref<DateAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(DoubleField field, Ref<DoubleAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(TimeStampField field, Ref<TimestampAccessor> accessor) {
    return null;
  }

  public SelectBuilder select(BlobField field, Ref<BlobAccessor> accessor) {
    return null;
  }

  public IntegerAccessor retrieve(IntegerField field) {
    return null;
  }

  public LongAccessor retrieve(LongField field) {
    return null;
  }

  public StringAccessor retrieve(StringField field) {
    return null;
  }

  public DateAccessor retrieve(DateField field) {
    return null;
  }

  public BooleanAccessor retrieve(BooleanField field) {
    return null;
  }

  public DoubleAccessor retrieve(DoubleField field) {
    return null;
  }

  public BlobAccessor retrieve(BlobField field) {
    return null;
  }

  public Accessor retrieveUnTyped(Field field) {
    return null;
  }
}
