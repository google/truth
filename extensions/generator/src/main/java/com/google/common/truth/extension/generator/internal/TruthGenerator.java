package com.google.common.truth.extension.generator.internal;

import com.google.common.flogger.FluentLogger;
import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.SourceClassSets;
import com.google.common.truth.extension.generator.SourceClassSets.PackageAndClasses;
import com.google.common.truth.extension.generator.TruthGeneratorAPI;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import lombok.Getter;
import lombok.Setter;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Antony Stubbs
 */
public class TruthGenerator implements TruthGeneratorAPI {

  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private boolean recursive = true;

  @Setter
  @Getter
  private Optional<String> entryPoint = Optional.empty();

  @Override
  public void generate(String... modelPackages) {
    Utils.requireNotEmpty(modelPackages);

    // just take the first for now
    // todo createEntryPointForPackages(modelPackages)
    String[] packageNameForOverall = modelPackages;
    OverallEntryPoint overallEntryPoint = new OverallEntryPoint(packageNameForOverall[0]);
    Set<ThreeSystem> subjectsSystems = generateSkeletonsFromPackages(modelPackages, overallEntryPoint);

    //
    addTests(subjectsSystems);
    overallEntryPoint.createOverallAccessPoints();
  }

  private Set<ThreeSystem> generateSkeletonsFromPackages(final String[] modelPackages, OverallEntryPoint overallEntryPoint) {
    Set<Class<?>> allTypes = collectSourceClasses(modelPackages);
    return generateSkeletons(allTypes, Optional.empty(), overallEntryPoint);
  }

  private Set<ThreeSystem> generateSkeletons(Set<Class<?>> classes, Optional<String> targetPackageName,
                                             OverallEntryPoint overallEntryPoint) {
    int sizeBeforeFilter = classes.size();
    classes = filterSubjects(classes, sizeBeforeFilter);

    Set<ThreeSystem> subjectsSystems = new HashSet<>();
    for (Class<?> clazz : classes) {
      SkeletonGenerator skeletonGenerator = new SkeletonGenerator(targetPackageName, overallEntryPoint);
      Optional<ThreeSystem> threeSystem = skeletonGenerator.threeLayerSystem(clazz);
      if (threeSystem.isPresent()) {
        ThreeSystem ts = threeSystem.get();
        subjectsSystems.add(ts);
        overallEntryPoint.add(ts);
      }
    }
    return subjectsSystems;
  }

  private Set<Class<?>> filterSubjects(Set<Class<?>> classes, int sizeBeforeFilter) {
    // filter existing subjects from inbound set
    classes = classes.stream().filter(x -> !Subject.class.isAssignableFrom(x)).collect(Collectors.toSet());
    log.at(Level.FINE).log("Removed %s Subjects from inbound", classes.size() - sizeBeforeFilter);
    return classes;
  }

  private Set<Class<?>> collectSourceClasses(final String[] modelPackages) {
    // for all classes in package
    SubTypesScanner subTypesScanner = new SubTypesScanner(false);

    Reflections reflections = new Reflections(modelPackages, subTypesScanner);
    reflections.expandSuperTypes(); // get things that extend something that extend object

    // https://github.com/ronmamo/reflections/issues/126
    Set<Class<? extends Enum>> subTypesOfEnums = reflections.getSubTypesOf(Enum.class);

    Set<Class<?>> allTypes = reflections.getSubTypesOf(Object.class)
            // remove Subject classes from previous runs
            .stream().filter(x -> !Subject.class.isAssignableFrom(x))
            .collect(Collectors.toSet());
    allTypes.addAll(subTypesOfEnums);
    return allTypes;
  }

  private void addTests(final Set<ThreeSystem> allTypes) {
    SubjectMethodGenerator tg = new SubjectMethodGenerator(allTypes);
    tg.addTests(allTypes);
  }

  @Override
  public void generateFromPackagesOf(Class<?>... classes) {
    generate(getPackageStrings(classes));
  }

  @Override
  public void combinedSystem(final SourceClassSets ss) {

  }

  private String[] getPackageStrings(final Class<?>[] classes) {
    return Arrays.stream(classes).map(x -> x.getPackage().getName()).collect(Collectors.toList()).toArray(new String[0]);
  }

  @Override
  public Map<Class<?>, ThreeSystem> generate(SourceClassSets ss) {
    RecursiveChecker rc = new RecursiveChecker();
    if (recursive) {
      rc.addReferencedIncluded(ss);
    } else {
      Set<Class<?>> missing = rc.findReferencedNotIncluded(ss);
      if (!missing.isEmpty()) {
        log.at(Level.WARNING)
                .log("Some referenced classes in the tree are not in the list of Subjects to be generated. " +
                        "Consider using automatic recursive generation, or add the missing classes. " +
                        "Otherwise your experience will be limited in places." +
                        "Missing classes %s", missing);
      }
    }

    Set<String[]> packages = ss.getSimplePackageOfClasses().stream().map(
            this::getPackageStrings
    ).collect(Collectors.toSet());

    OverallEntryPoint overallEntryPoint = new OverallEntryPoint(ss.getPackageForOverall());

    // skeletons generation is independent and should be able to be done in parallel
    Set<ThreeSystem> skeletons = packages.parallelStream().flatMap(
            x -> generateSkeletonsFromPackages(x, overallEntryPoint).stream()
    ).collect(Collectors.toSet());

    // custom package destination
    Set<PackageAndClasses> packageAndClasses = ss.getPackageAndClasses();
    Set<ThreeSystem> setStream = packageAndClasses.stream().flatMap(
            x -> {
              Set<Class<?>> collect = Arrays.stream(x.getClasses()).collect(Collectors.toSet());
              return generateSkeletons(collect, Optional.of(x.getTargetPackageName()), overallEntryPoint).stream();
            }
    ).collect(Collectors.toSet());

    // straight up classes
    Set<ThreeSystem> simpleClasses = generateSkeletons(ss.getSimpleClasses(), Optional.empty(), overallEntryPoint);

    // legacy classes
    Set<ThreeSystem> legacyClasses = generateSkeletons(ss.getLegacyBeans(), Optional.empty(), overallEntryPoint);
    legacyClasses.forEach(x -> x.setLegacyMode(true));

    // legacy classes with custom package destination
    Set<PackageAndClasses> legacyPackageAndClasses = ss.getLegacyPackageAndClasses();
    Set<ThreeSystem> legacyPackageSet = legacyPackageAndClasses.stream().flatMap(
            x -> {
              Set<Class<?>> collect = Arrays.stream(x.getClasses()).collect(Collectors.toSet());
              return generateSkeletons(collect, Optional.of(x.getTargetPackageName()), overallEntryPoint).stream();
            }
    ).collect(Collectors.toSet());
    legacyPackageSet.forEach(x -> x.setLegacyMode(true));


    // add tests
    Set<ThreeSystem> union = new HashSet<>();
    union.addAll(skeletons);
    union.addAll(setStream);
    union.addAll(simpleClasses);
    union.addAll(legacyClasses);
    union.addAll(legacyPackageSet);

    //
    addTests(union);

    // create overall entry point
    overallEntryPoint.createOverallAccessPoints();

    return union.stream().collect(Collectors.toMap(ThreeSystem::getClassUnderTest, x -> x));
  }

  @Override
  public Map<Class<?>, ThreeSystem> generate(Set<Class<?>> classes) {
    Utils.requireNotEmpty(classes);
    String entrypointPackage = (this.entryPoint.isPresent())
            ? entryPoint.get()
            : createEntrypointPackage(classes);
    SourceClassSets ss = new SourceClassSets(entrypointPackage);
    ss.generateFrom(classes);
    return generate(ss);
  }

  /**
   * todo change this to do this by finding the highest common package of all outputs
   */
  private String createEntrypointPackage(final Set<Class<?>> classes) {
    return classes.stream().findFirst().get().getPackageName();
  }

  @Override
  public Map<Class<?>, ThreeSystem> generate(Class<?>... classes) {
    return generate(Arrays.stream(classes).collect(Collectors.toSet()));
  }

  @Override
  public String maintain(final Class source, final Class userAndGeneratedMix) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public <T> String combinedSystem(final Class<T> source) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public void combinedSystem(final String... modelPackages) {
    throw new IllegalStateException("Not implemented yet");
  }
}
