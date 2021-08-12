package com.google.common.truth.extension.generator.internal;

import com.google.common.flogger.FluentLogger;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Method;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.extension.generator.internal.Utils.writeToDisk;

@RequiredArgsConstructor
public class OverallEntryPoint {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final List<JavaClassSource> children = new ArrayList<>();

  @Getter
  private final String packageName;

  /**
   * Having collected together all the access points, creates one large class filled with access points to all of them.
   * <p>
   * The overall access will throw an error if any middle classes don't correctly extend their parent.
   */
  public void createOverallAccessPoints() {
    JavaClassSource overallAccess = Roaster.create(JavaClassSource.class);
    overallAccess.setName("ManagedTruth");
    overallAccess.getJavaDoc()
            .setText("Single point of access for all managed Subjects.");
    overallAccess.setPublic()
            .setPackage(packageName);

    // brute force
    for (JavaClassSource child : children) {
      List<MethodSource<JavaClassSource>> methods = child.getMethods();
      for (Method m : methods) {
        if (!m.isConstructor())
          overallAccess.addMethod(m);
      }
      // this seems like overkill, but at least in the child style case, there's very few imports - even
      // none extra at all (aside from wild card vs specific methods).
      List<Import> imports = child.getImports();
      for (Import i : imports) {
        // roaster just throws up a NPE when this happens
        Set<String> simpleNames = overallAccess.getImports().stream().map(Import::getSimpleName).filter(x -> !x.equals("*")).collect(Collectors.toSet());
        if (simpleNames.contains(i.getSimpleName())) {
          logger.atWarning().log("Collision of imports - cannot add duplicated import %s", i);
        } else {
          overallAccess.addImport(i);
        }
      }
    }

    writeToDisk(overallAccess);
  }

  public void add(ThreeSystem ts) {
    this.children.add(ts.child);
  }
}
