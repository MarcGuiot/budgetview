package org.designup.shrinker;

import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.collections.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class DependExtractor {
  private Set<String> neededClassToJar = new HashSet<String>();
  private LinkedList<String> classToParse = new LinkedList<String>();
  private Set<String> classToIgnore = new HashSet<String>();
  private MultiMap<String, String> methodToIgnore = new MultiMap<String, String>();
  private String currentClass;
  private ClassRetreiver classRetriever;
  private LinkedList<Pair<String, Boolean>> pathAndIsRecursive = new LinkedList<Pair<String, Boolean>>();
  private boolean withDebug;

  public DependExtractor(boolean withDebug) {
    this.withDebug = withDebug;
  }

  interface ClassRetreiver {
    InputStream getCode(String className);

    void add(String dependClassName, String className);

    interface Added{
      boolean add(String className);
    }

    void addPathContent(String path, Boolean isRecursive, Added added);
  }

  public void add(ClassRetreiver classRetreiver) {
    this.classRetriever = classRetreiver;
  }

  public void addStartPoint(String className) {
    String undotedClassName = undot(className);
    classToParse.add(undotedClassName);
    neededClassToJar.add(undotedClassName);
  }

  public void addRootPackage(String path, boolean isRecursive) {
    pathAndIsRecursive.add(new Pair<String, Boolean>(path, isRecursive));
  }

  public void addMethodToIgnore(String methodeClassName, String methodName) {
    methodToIgnore.put(undot(methodeClassName), methodName);
  }

  public void addClassToIgnore(String name) {
    classToIgnore.add(undot(name));
  }

  public static String undot(String name) {
    return name.replace('.', '/');
  }

  public void extract() throws IOException {
    while (!pathAndIsRecursive.isEmpty()) {
      Pair<String, Boolean> pathAndPattern = pathAndIsRecursive.removeFirst();
      classRetriever.addPathContent(pathAndPattern.getFirst(), pathAndPattern.getSecond(), new ClassRetreiver.Added() {
        public boolean add(String className) {
          if (classToIgnore.contains(className)) {
            return false;
          }
          classToParse.add(className);
          return true;
        }
      });
    }
    extractClass();
  }

  private void extractClass() throws IOException {
    for (String aClassToParse : classToParse) {
      classRetriever.add(null, aClassToParse);
    }
    ClassVisitorExtractor extractor = new ClassVisitorExtractor();
    while (!classToParse.isEmpty()) {
      currentClass = classToParse.removeFirst();
      ClassReader reader = findClassReader(currentClass);
      if (reader != null) {
        try {
          reader.accept(extractor, withDebug ? 0 : ClassReader.SKIP_DEBUG);
        }
        catch (Exception e) {
          throw new IOException("Error for: " + currentClass, e);
        }
      }
    }
  }

  private ClassReader findClassReader(String className) throws IOException {
    InputStream code = classRetriever.getCode(className);
    if (code == null) {
      return null;
    }
    try {
      return new ClassReader(code);
    }
    catch (Exception e) {
      throw new IOException("Error reading class: " + className, e);
    }
  }

  void add(String className) {
    className = undot(className);
    if (className.startsWith("[L")) {   //ignore array of enum??
      return;
    }
    if (classToIgnore.contains(className)) {
      return;
    }
    if (this.neededClassToJar.contains(className)) {
      return;
    }
    neededClassToJar.add(className);
    classToParse.add(className);
    classRetriever.add(currentClass, className);
  }

  private void add(Type argumentType) {
    if (argumentType.getSort() == Type.OBJECT) {
      add(argumentType.getClassName());
    }
    if (argumentType.getSort() == Type.ARRAY) {
      add(argumentType.getElementType());
    }
  }

  private class ClassVisitorExtractor extends EmptyVisitor {
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      if (superName != null) {
        add(superName);
      }
      for (String anInterface : interfaces) {
        add(anInterface);
      }
    }

    public void visitOuterClass(String owner, String name, String desc) {
      add(owner);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      add(name);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      if (methodToIgnore.get(currentClass).contains(name)) {
        return new EmptyVisitor();
      }
      Type[] argumentTypes = Type.getArgumentTypes(desc);
      for (Type argumentType : argumentTypes) {
        add(argumentType);
      }
      if (exceptions != null) {
        for (String exception : exceptions) {
          add(Type.getObjectType(exception));
        }
      }
      add(Type.getReturnType(desc));
      return this;
    }

    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
      add(owner);
    }

    public void visitTypeInsn(final int opcode, final String desc) {
      if (opcode == Opcodes.NEW) {
        add(desc);
      }
    }

    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
      if (opcode == Opcodes.PUTFIELD
          || opcode == Opcodes.PUTSTATIC
          || opcode == Opcodes.GETFIELD
          || opcode == Opcodes.GETSTATIC) {
        add(owner);
      }
    }

    public void visitLdcInsn(final Object cst) {
      if (cst instanceof Type) {
        add(((Type)cst).getClassName());
      }
    }
  }

}
