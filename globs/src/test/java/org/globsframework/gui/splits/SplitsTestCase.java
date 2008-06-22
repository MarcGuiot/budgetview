package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontService;
import org.globsframework.gui.splits.utils.DummyIconLocator;
import org.globsframework.gui.splits.utils.DummyTextLocator;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.styles.StyleService;
import org.uispec4j.UISpecTestCase;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.StringReader;
import java.io.Reader;

public abstract class SplitsTestCase extends UISpecTestCase {
  protected SplitsBuilder builder;
  protected ColorService colorService = new ColorService();
  protected FontService fontService = new FontService();
  protected DummyIconLocator iconLocator = new DummyIconLocator();
  protected DummyTextLocator textLocator = new DummyTextLocator();
  protected StyleService styleService = new StyleService();

  protected void setUp() throws Exception {
    super.setUp();
    builder = new SplitsBuilder(colorService, iconLocator, textLocator, fontService);
  }

  protected Component parse(String xml) throws Exception {
    validateDocument(toStream(xml));
    return builder.parse(toStream(xml));
  }

  protected Component parseWithoutSchemaValidation(String xml) throws Exception {
    return builder.parse(toStream(xml));
  }

  protected StringReader toStream(String xml) {
    return new StringReader("<splits>" + xml + "</splits>");
  }

  protected void validateDocument(Reader reader) throws Exception {
    String schemaLang = "http://www.w3.org/2001/XMLSchema";
    SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
    Schema schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream("/splits.xsd")));
    Validator validator = schema.newValidator();
    validator.validate(new StreamSource(reader));
  }

  protected void checkParsingError(String xml, String error) throws Exception {
    try {
      parse(xml);
      fail();
    }
    catch (SplitsException e) {
      assertEquals(error, e.getMessage());
    }
  }

}
