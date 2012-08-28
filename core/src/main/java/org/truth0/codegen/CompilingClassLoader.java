package org.truth0.codegen;

import static java.util.Collections.singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

// NICKED FROM JSILVER -- BUT DAVID BEAUMONT WROTE MOST OF THAT ONE TOO !
/**
 * This is a Java ClassLoader that will attempt to load a class from a string of source code.
 *
 * <h3>Example</h3>
 *
 * <pre>
 * String className = "com.foo.MyClass";
 * String classSource =
 *   "package com.foo;\n" +
 *   "public class MyClass implements Runnable {\n" +
 *   "  @Override public void run() {\n" +
 *   "   log(\"Hello world\");\n" +
 *   "  }\n" +
 *   "}";
 *
 * // Load class from source.
 * ClassLoader classLoader = new CompilingClassLoader(
 *     parentClassLoader, className, classSource);
 * Class myClass = classLoader.loadClass(className);
 *
 * // Use it.
 * Runnable instance = (Runnable)myClass.newInstance();
 * instance.run();
 * </pre>
 *
 * Only one chunk of source can be compiled per instance of CompilingClassLoader. If you need to
 * compile more, create multiple CompilingClassLoader instances.
 *
 * Uses Java 1.6's in built compiler API.
 *
 * If the class cannot be compiled, loadClass() will throw a ClassNotFoundException and log the
 * compile errors to System.err. If you don't want the messages logged, or want to explicitly handle
 * the messages you can provide your own {@link javax.tools.DiagnosticListener} through
 * {#setDiagnosticListener()}.
 *
 * @see java.lang.ClassLoader
 * @see javax.tools.JavaCompiler
 */
public class CompilingClassLoader extends ClassLoader {

  /**
   * Thrown when code cannot be compiled.
   */
  public static class CompilerException extends Exception {

    private static final long serialVersionUID = -2936958840023603270L;

    public CompilerException(String message) {
      super(message);
    }
  }

  private final Map<String, ByteArrayOutputStream> byteCodeForClasses =
      new HashMap<String, ByteArrayOutputStream>();

  private static final URI EMPTY_URI;

  static {
    try {
      // Needed to keep SimpleFileObject constructor happy.
      EMPTY_URI = new URI("");
    } catch (URISyntaxException e) {
      throw new Error(e);
    }
  }

  /**
   * @param parent Parent classloader to resolve dependencies from.
   * @param className Name of class to compile. eg. "com.foo.MyClass".
   * @param sourceCode Java source for class. e.g. "package com.foo; class MyClass { ... }".
   * @param diagnosticListener Notified of compiler errors (may be null).
   */
  public CompilingClassLoader(ClassLoader parent, String className, String sourceCode,
      DiagnosticListener<JavaFileObject> diagnosticListener) throws CompilerException {
    super(parent);
    if (!compileSourceCodeToByteCode(className, sourceCode, diagnosticListener)) {
      throw new CompilerException("Could not compile " + className);
    }
  }

  /**
   * Override ClassLoader's class resolving method. Don't call this directly, instead use
   * {@link ClassLoader#loadClass(String)}.
   */
  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    ByteArrayOutputStream byteCode = byteCodeForClasses.get(name);
    if (byteCode == null) {
      throw new ClassNotFoundException(name);
    }
    return defineClass(name, byteCode.toByteArray(), 0, byteCode.size());
  }

  /**
   * @return Whether compilation was successful.
   */
  private boolean compileSourceCodeToByteCode(String className, String sourceCode,
      DiagnosticListener<JavaFileObject> diagnosticListener) {
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

    // Set up the in-memory filesystem.
    InMemoryFileManager fileManager =
        new InMemoryFileManager(javaCompiler.getStandardFileManager(null, null, null));
    JavaFileObject javaFile = new InMemoryJavaFile(className, sourceCode);

    // Javac option: remove these when the javac zip impl is fixed
    // (http://b/issue?id=1822932)
    System.setProperty("useJavaUtilZip", "true"); // setting value to any non-null string
    List<String> options = new LinkedList<String>();
    // this is ignored by javac currently but useJavaUtilZip should be
    // a valid javac XD option, which is another bug
    options.add("-XDuseJavaUtilZip");

    // Now compile!
    JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(null, // Null: log any
                                                                              // unhandled errors to
                                                                              // stderr.
        fileManager, diagnosticListener, options, null, singleton(javaFile));
    return compilationTask.call();
  }

  /**
   * Provides an in-memory representation of JavaFileManager abstraction, so we do not need to write
   * any files to disk.
   *
   * When files are written to, rather than putting the bytes on disk, they are appended to buffers
   * in byteCodeForClasses.
   *
   * @see javax.tools.JavaFileManager
   */
  private class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    public InMemoryFileManager(JavaFileManager fileManager) {
      super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, final String className,
        JavaFileObject.Kind kind, FileObject sibling) throws IOException {
      return new SimpleJavaFileObject(EMPTY_URI, kind) {
        @Override
        public OutputStream openOutputStream() throws IOException {
          ByteArrayOutputStream outputStream = byteCodeForClasses.get(className);
          if (outputStream != null) {
            throw new IllegalStateException("Cannot write more than once");
          }
          // Reasonable size for a simple .class.
          outputStream = new ByteArrayOutputStream(256);
          byteCodeForClasses.put(className, outputStream);
          return outputStream;
        }
      };
    }
  }

  private static class InMemoryJavaFile extends SimpleJavaFileObject {

    private final String sourceCode;

    public InMemoryJavaFile(String className, String sourceCode) {
      super(makeUri(className), Kind.SOURCE);
      this.sourceCode = sourceCode;
    }

    private static URI makeUri(String className) {
      try {
        return new URI(className.replaceAll("\\.", "/") + Kind.SOURCE.extension);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e); // Not sure what could cause this.
      }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      return sourceCode;
    }
  }
}
