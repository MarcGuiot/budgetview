package org.crossbowlabs.globs.model.indexing;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.index.*;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.indexing.builders.NotUniqueLeafFieldIndexBuilder;
import org.crossbowlabs.globs.model.indexing.builders.MultiFieldIndexBuilder;
import org.crossbowlabs.globs.model.indexing.builders.UniqueLeafFieldIndexBuilder;
import org.crossbowlabs.globs.model.indexing.indices.*;

import java.util.HashMap;
import java.util.Map;

public class IndexManager {
   private Map<Field, IndexTables> fieldToIndex = new HashMap<Field, IndexTables>();
   private Map<GlobType, IndexTables> globTypeToIndex = new HashMap<GlobType, IndexTables>();
   private Map<Index, IndexedTable> indexAssociationTableMap = new HashMap<Index, IndexedTable>();
   private Map<MultiFieldIndex, GlobRepository.MultiFieldIndexed> multiFieldIndexTableMap = new HashMap<MultiFieldIndex, GlobRepository.MultiFieldIndexed>();

   private IndexSource indexSource;

   public IndexManager(IndexSource indexSource) {
     this.indexSource = indexSource;
   }

   public GlobRepository.MultiFieldIndexed getAssociatedTable(MultiFieldIndex multiFieldIndex) {
     GlobRepository.MultiFieldIndexed multiFieldIndexed = multiFieldIndexTableMap.get(multiFieldIndex);
     if (multiFieldIndexed == null) {
       UpdateMultiIndexVisitor updateMultiIndexVisitor = new UpdateMultiIndexVisitor();
       multiFieldIndex.visit(updateMultiIndexVisitor);
       multiFieldIndexed = updateMultiIndexVisitor.getIndexed();
       multiFieldIndexTableMap.put(multiFieldIndex, multiFieldIndexed);
     }
     return multiFieldIndexed;
   }

   public IndexTables getAssociatedTable(Field field) {
     return fieldToIndex.get(field);
   }

   public IndexTables getAssociatedTable(GlobType type) {
     return globTypeToIndex.get(type);
   }

   public IndexedTable getAssociatedTable(Index index) {
     IndexedTable indexedTable = indexAssociationTableMap.get(index);
     if (indexedTable == null) {
       UpdateIndexVisitor updateIndexVisitor = new UpdateIndexVisitor();
       index.visitIndex(updateIndexVisitor);
       indexedTable = updateIndexVisitor.getTable();
     }
     return indexedTable;
   }


   private void updateFieldToIndexTables(Field field, IndexedTable indexedTable) {
     IndexTables tables = fieldToIndex.get(field);
     if (tables == null) {
       tables = new OneIndexTable(indexedTable);
     }
     else {
       tables = tables.add(indexedTable);
     }
     fieldToIndex.put(field, tables);
   }

   private void updateGlobTypeToIndexTables(GlobType type, IndexedTable indexedTable) {
     IndexTables globTypeTables = globTypeToIndex.get(type);
     if (globTypeTables == null) {
       globTypeTables = new OneIndexTable(indexedTable);
     }
     else {
       globTypeTables = globTypeTables.add(indexedTable);
     }
     globTypeToIndex.put(type, globTypeTables);
   }

   private class UpdateIndexVisitor implements IndexVisitor {
     private IndexedTable indexedTable;

     public void visiteUniqueIndex(UniqueIndex index) {
       indexedTable = new DefaultUniqueIndex(index.getField());
       addIndex(index);
     }

     public void visiteNotUniqueIndex(NotUniqueIndex index) {
       indexedTable = new DefaultNotUniqueIndex(index.getField());
       addIndex(index);
     }

     private void addIndex(Index index) {
       Field field = index.getField();
       updateFieldToIndexTables(field, indexedTable);
       updateGlobTypeToIndexTables(field.getGlobType(), indexedTable);
       for (Glob glob : indexSource.getGlobs(field.getGlobType())) {
         indexedTable.add(glob);
       }
       indexAssociationTableMap.put(index, indexedTable);
     }

     public IndexedTable getTable() {
       return indexedTable;
     }
   }

   class UpdateMultiIndexVisitor implements MultiFieldIndexVisitor {
     private MultiFieldIndexBuilder multiFieldIndexed;
     private MiddleLevelIndex rootIndex;

     public void visitNotUnique(MultiFieldNotUniqueIndex index) {
       Field[] fields = index.getFields();
       multiFieldIndexed =
               new DefaultMultiMiddleFieldIndexBuilder(fields[0]);
       rootIndex = new MiddleLevelIndex(multiFieldIndexed);
       MultiFieldIndexBuilder sub = multiFieldIndexed;
       for (int i = 1; i < fields.length; i++) {
         Field field = fields[i];
         if (i != fields.length - 1) {
           DefaultMultiMiddleFieldIndexBuilder tmp = new DefaultMultiMiddleFieldIndexBuilder(field);
           sub.setChild(tmp);
           sub = tmp;
         }
         else {
           MultiFieldIndexBuilder tmp = new NotUniqueLeafFieldIndexBuilder(field);
           sub.setChild(tmp);
           sub = tmp;
         }
         updateFieldToIndexTables(field, rootIndex);
       }
       updateGlobTypeToIndexTables(multiFieldIndexed.getField().getGlobType(), rootIndex);
     }

     public void visitUnique(MultiFieldUniqueIndex index) {
       Field[] fields = index.getFields();
       multiFieldIndexed =
               new DefaultMultiMiddleFieldIndexBuilder(fields[0]);
       rootIndex = new MiddleLevelIndex(multiFieldIndexed);
       updateFieldToIndexTables(fields[0], rootIndex);
       MultiFieldIndexBuilder sub = multiFieldIndexed;
       for (int i = 1; i < fields.length; i++) {
         Field field = fields[i];
         if (i != fields.length - 1) {
           DefaultMultiMiddleFieldIndexBuilder tmp = new DefaultMultiMiddleFieldIndexBuilder(field);
           sub.setChild(tmp);
           sub = tmp;
         }
         else {
           MultiFieldIndexBuilder tmp = new UniqueLeafFieldIndexBuilder(field);
           sub.setChild(tmp);
           sub = tmp;
         }
         updateFieldToIndexTables(field, rootIndex);
       }
       updateGlobTypeToIndexTables(multiFieldIndexed.getField().getGlobType(), rootIndex);
     }

     public GlobRepository.MultiFieldIndexed getIndexed() {
       Field field = multiFieldIndexed.getField();
       GlobType globType = field.getGlobType();
       for (Glob glob : indexSource.getGlobs(globType)) {
         rootIndex.add(glob);
       }
       return rootIndex;
     }

     private class DefaultMultiMiddleFieldIndexBuilder implements MultiFieldIndexBuilder {
       private Field field;
       private MultiFieldIndexBuilder childIndexBuilder;

       public DefaultMultiMiddleFieldIndexBuilder(Field field) {
         this.field = field;
       }

       public UpdatableMultiFieldIndex create() {
         return new MiddleLevelIndex(this);
       }

       public MultiFieldIndexBuilder getSubBuilder() {
         return childIndexBuilder;
       }

       public void setChild(MultiFieldIndexBuilder indexBuilder) {
         this.childIndexBuilder = indexBuilder;
       }

       public Field getField() {
         return field;
       }
     }
   }

 }
