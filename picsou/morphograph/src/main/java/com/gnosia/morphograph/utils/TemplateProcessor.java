package com.gnosia.morphograph.utils;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class TemplateProcessor {

  private Configuration configuration = new Configuration();
  private String fileName;
  private Map parameters = new HashMap();

  public static TemplateProcessor init(String fileName) {
    return new TemplateProcessor(fileName);
  }

  private TemplateProcessor(String fileName) {
    configuration.setClassForTemplateLoading(getClass(), "/layout");
    configuration.setObjectWrapper(new DefaultObjectWrapper());
    configuration.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
    this.fileName = fileName;
  }

  public TemplateProcessor add(String key, Object obj) {
    parameters.put(key, obj);
    return this;
  }

  public String run() {
    try {
      Template template = configuration.getTemplate(fileName);
      StringWriter out = new StringWriter();
      template.process(parameters, out);
      out.flush();
      return out.toString();
    }
    catch (Exception e) {
      throw new ResourceAccessFailed(e);
    }
  }
}
