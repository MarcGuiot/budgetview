package org.crossbowlabs.globs.streams.xml;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.MutableGlob;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.streams.accessors.*;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.xml.XmlSingleGlobParser;
import org.crossbowlabs.saxstack.parser.DefaultXmlNode;
import org.crossbowlabs.saxstack.parser.ExceptionHolder;
import org.crossbowlabs.saxstack.parser.SaxStackParser;
import org.crossbowlabs.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

import java.io.StringReader;
import java.util.*;

public class XmlGlobStreamReader {
  private GlobModel globModel;
  private String xml;
  private XmlGlobStream xmlMoStream;

  public static GlobStream parse(Directory directory, String xml) {
    return new XmlGlobStreamReader(directory, "<root>" + xml + "</root>").parse();
  }

  private XmlGlobStreamReader(Directory directory, String xml) {
    this.xml = xml;
    globModel = directory.get(GlobModel.class);
  }

  private GlobStream parse() {
    xmlMoStream = new XmlGlobStream();
    SaxStackParser.parse(new SAXParser(), new RootProxyNode(), new StringReader(xml));
    return xmlMoStream;
  }

  private void add(Glob mo) {
    xmlMoStream.add(mo);
  }

  class RootProxyNode extends DefaultXmlNode implements XmlSingleGlobParser.GlobAdder {

    public RootProxyNode() {
    }

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) throws ExceptionHolder {
      try {
        if (childName.equals("root")) {
          return this;
        }
        else {
          XmlSingleGlobParser.parse(childName, xmlAttrs, globModel, this);
          return this;
        }
      }
      catch (Exception e) {
        throw new ExceptionHolder(e);
      }
    }

    public void add(MutableGlob mo) {
      XmlGlobStreamReader.this.add(mo);
    }
  }

  private static class XmlGlobStream implements GlobStream {
    private GlobType globType;
    private GlobList globs = new GlobList();
    private List<Field> fields = new ArrayList<Field>();
    private Iterator<Glob> iterator;
    private Glob current;
    private Map<String, Accessor> accessors = new HashMap<String, Accessor>();

    public XmlGlobStream() {
    }

    public boolean next() {
      if (iterator == null) {
        iterator = globs.iterator();
      }
      if (iterator.hasNext()) {
        current = iterator.next();
        return true;
      }
      return false;
    }

    public Collection<Field> getFields() {
      return fields;
    }

    public GlobType getObjectType() {
      return globType;
    }

    public Accessor getAccessor(Field field) {
      return accessors.get(field.getName());
    }

    public void add(Glob glob) {
      if (globs.isEmpty()) {
        globType = glob.getType();
        for (Field field : glob.getType().getFields()) {
          AccessorDataTypeVisitor dataTypeVisitor = new AccessorDataTypeVisitor(this);
          field.safeVisit(dataTypeVisitor);
          accessors.put(field.getName(), dataTypeVisitor.getAccessor());
          fields.add(globType.getField(field.getName()));
        }
      }
      globs.add(glob);
    }

    private static class AccessorDataTypeVisitor implements FieldVisitor {
      private XmlGlobStream stream;
      private Accessor accessor;

      public AccessorDataTypeVisitor(XmlGlobStream stream) {
        this.stream = stream;
      }

      public void visitDate(DateField field) throws Exception {
        accessor = new XmlDateAccessor(stream, field);
      }

      public void visitBoolean(BooleanField field) throws Exception {
        accessor = new XmlBooleanAccessor(stream, field);
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        accessor = new XmlTimestampAccessor(stream, field);
      }

      public void visitBlob(BlobField field) throws Exception {
        accessor = new XmlBlobAccessor(stream, field);
      }

      public void visitString(StringField field) throws Exception {
        accessor = new XmlStringAccessor(stream, field);
      }

      public void visitDouble(DoubleField field) throws Exception {
        accessor = new XmlDoubleAccessor(stream, field);
      }

      public void visitInteger(IntegerField field) throws Exception {
        accessor = new XmlIntegerAccessor(stream, field);
      }

      public void visitLong(LongField field) throws Exception {
        accessor = new XmlLongAccessor(stream, field);
      }

      public void visitLink(LinkField field) throws Exception {
        visitInteger(field);
      }

      public Accessor getAccessor() {
        return accessor;
      }

      private static class XmlDateAccessor implements DateAccessor {
        private XmlGlobStream xmlMoStream;
        private DateField field;

        public XmlDateAccessor(XmlGlobStream xmlMoStream, DateField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Date getDate() {
          return xmlMoStream.current.get(field);
        }

        public Object getObjectValue() {
          return getDate();
        }
      }

      private static class XmlTimestampAccessor implements DateAccessor {
        private XmlGlobStream xmlMoStream;
        private TimeStampField field;

        public XmlTimestampAccessor(XmlGlobStream xmlMoStream, TimeStampField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Date getDate() {
          return xmlMoStream.current.get(field);
        }

        public Object getObjectValue() {
          return getDate();
        }
      }

      private static class XmlBlobAccessor implements BlobAccessor {
        private XmlGlobStream xmlMoStream;
        private BlobField field;

        public XmlBlobAccessor(XmlGlobStream xmlMoStream, BlobField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Object getObjectValue() {
          return getValue();
        }

        public byte[] getValue() {
          return xmlMoStream.current.get(field);
        }
      }

      private static class XmlStringAccessor implements StringAccessor {
        private XmlGlobStream xmlMoStream;
        private StringField field;

        public XmlStringAccessor(XmlGlobStream xmlMoStream, StringField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public String getString() {
          return xmlMoStream.current.get(field);
        }

        public Object getObjectValue() {
          return getString();
        }
      }

      private static class XmlBooleanAccessor implements BooleanAccessor {
        private XmlGlobStream xmlMoStream;
        private BooleanField field;

        public XmlBooleanAccessor(XmlGlobStream xmlMoStream, BooleanField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Boolean getBoolean() {
          return xmlMoStream.current.get(field);
        }

        public boolean getValue() {
          return getBoolean();
        }

        public Object getObjectValue() {
          return getBoolean();
        }
      }

      private static class XmlDoubleAccessor implements DoubleAccessor {
        private XmlGlobStream xmlMoStream;
        private DoubleField field;

        public XmlDoubleAccessor(XmlGlobStream xmlMoStream, DoubleField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Double getDouble() {
          return xmlMoStream.current.get(field);
        }

        public double getValue() {
          return getDouble();
        }

        public Object getObjectValue() {
          return getDouble();
        }
      }

      private class XmlIntegerAccessor implements IntegerAccessor {
        private XmlGlobStream xmlMoStream;
        private IntegerField field;

        public XmlIntegerAccessor(XmlGlobStream xmlMoStream, IntegerField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Integer getInteger() {
          return xmlMoStream.current.get(field);
        }

        public int getValue() {
          return getInteger();
        }

        public Object getObjectValue() {
          return getInteger();
        }
      }

      private class XmlLongAccessor implements LongAccessor {
        private XmlGlobStream xmlMoStream;
        private LongField field;

        public XmlLongAccessor(XmlGlobStream xmlMoStream, LongField field) {
          this.xmlMoStream = xmlMoStream;
          this.field = field;
        }

        public Long getLong() {
          return xmlMoStream.current.get(field);
        }

        public long getValue() {
          return getLong();
        }

        public Object getObjectValue() {
          return getLong();
        }
      }
    }

  }

}
