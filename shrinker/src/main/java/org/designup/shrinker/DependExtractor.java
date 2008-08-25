package org.designup.shrinker;

import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Pair;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class DependExtractor {
  private Set<String> neededClassToJar = new HashSet<String>();
  private LinkedList<String> classToParse = new LinkedList<String>();
  private LinkedList<String> filesToAdd = new LinkedList<String>();
  private Set<String> classToIgnore = new HashSet<String>();
  private MultiMap<String, String> methodToIgnore = new MultiMap<String, String>();
  private String currentClass;
  private ClassRetreiver classRetreiver;
  private LinkedList<Pair<String, Boolean>> pathAndIsRecursive = new LinkedList<Pair<String, Boolean>>();

  public DependExtractor() {
  }

  interface ClassRetreiver {
    InputStream getCode(String className);

    void add(String dependClassName, String className);

    void addPathContent(String path, Boolean isRecursive);
  }

  public void add(ClassRetreiver classRetreiver) {
    this.classRetreiver = classRetreiver;
  }

  public void addStartPoint(String className) {
    String undotedClassName = undotte(className);
    classToParse.add(undotedClassName);
    neededClassToJar.add(undotedClassName);
  }

  public void addRootPackage(String path, boolean isRecursive) {
    pathAndIsRecursive.add(new Pair<String, Boolean>(path, isRecursive));
  }

  public void addMethodToIgnore(String methodeClassName, String methodName) {
    methodToIgnore.put(undotte(methodeClassName), methodName);
  }

  public void addClassToIgnore(String name) {
    classToIgnore.add(undotte(name));
  }

  public static String undotte(String name) {
    return name.replace('.', '/');
  }

  public void extract() throws IOException {
    extractClass();
    while (!pathAndIsRecursive.isEmpty()) {
      Pair<String, Boolean> pathAndPattern = pathAndIsRecursive.removeFirst();
      classRetreiver.addPathContent(pathAndPattern.getFirst(), pathAndPattern.getSecond());
    }
    for (String s : filesToAdd) {
      System.out.println(s);
    }
  }

  private void extractClass() throws IOException {
    for (String aClassToParse : classToParse) {
      classRetreiver.add(null, aClassToParse);
    }
    ClassVisitorExtractor extractor = new ClassVisitorExtractor();
    while (!classToParse.isEmpty()) {
      currentClass = classToParse.removeFirst();
      ClassReader reader = findClassReader(currentClass);
      if (reader != null) {
        reader.accept(extractor, ClassReader.SKIP_DEBUG);
      }
    }
  }

  private ClassReader findClassReader(String className) throws IOException {
    InputStream code = classRetreiver.getCode(className);
    if (code == null) {
      return null;
    }
    return new ClassReader(code);
  }

  void add(String className) {
    className = undotte(className);
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
    classRetreiver.add(currentClass, className);
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

  static class EmptyVisitor implements ClassVisitor, FieldVisitor,
                                       MethodVisitor, AnnotationVisitor {

    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotationDefault() {
      return this;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return this;
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      return this;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitCode() {
    }

    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
    }

    public void visitInsn(int opcode) {
    }

    public void visitIntInsn(int opcode, int operand) {
    }

    public void visitVarInsn(int opcode, int var) {
    }

    public void visitTypeInsn(int opcode, String desc) {
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    }

    public void visitJumpInsn(int opcode, Label label) {
    }

    public void visitLabel(Label label) {
    }

    public void visitLdcInsn(Object cst) {
    }

    public void visitIincInsn(int var, int increment) {
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
    }

    public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
    }

    public void visitLineNumber(int line, Label start) {
    }

    public void visitMaxs(int maxStack, int maxLocals) {
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      return this;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      return this;
    }

    public void visit(String name, Object value) {
    }

    public void visitEnum(String name, String desc, String value) {
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
      return this;
    }

    public AnnotationVisitor visitArray(String name) {
      return this;
    }

    public void visitEnd() {
    }
  }
}
