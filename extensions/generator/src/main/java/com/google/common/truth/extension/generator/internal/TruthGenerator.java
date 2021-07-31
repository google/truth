package com.google.common.truth.extension.generator.internal;

import com.google.common.collect.Sets;
import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.TruthGeneratorAPI;
import com.google.common.truth.extension.generator.internal.model.SourceClassSets;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Antony Stubbs
 */
public class TruthGenerator implements TruthGeneratorAPI {

    @Override
    public void generate(String... modelPackages) {
        Utils.requireNotEmpty(modelPackages);

        OverallEntryPoint overallEntryPoint = new OverallEntryPoint();
        Set<ThreeSystem> subjectsSystems = generateSkeletonsFromPackages(modelPackages, overallEntryPoint);

        //
        addTests(subjectsSystems);

        // just take the first for now
        String[] packageNameForOverall = modelPackages;
        overallEntryPoint.createOverallAccessPoints(packageNameForOverall[0]);
    }

    private Set<ThreeSystem> generateSkeletonsFromPackages(final String[] modelPackages, OverallEntryPoint overallEntryPoint) {
        Set<Class<?>> allTypes = collectSourceClasses(modelPackages);
        return generateSkeletons(allTypes, Optional.empty(), overallEntryPoint);
    }

    private Set<ThreeSystem> generateSkeletons(Set<Class<?>> classes, Optional<String> targetPackageName,
                                               OverallEntryPoint overallEntryPoint) {
        SkeletonGenerator skeletonGenerator = new SkeletonGenerator();

        Set<ThreeSystem> subjectsSystems = new HashSet<>();
        for (Class<?> c : classes) {
            Optional<ThreeSystem> threeSystem = skeletonGenerator.threeLayerSystem(c, targetPackageName);
            if (threeSystem.isPresent()) {
                ThreeSystem ts = threeSystem.get();
                subjectsSystems.add(ts);
                overallEntryPoint.add(ts);
            }
        }
        return subjectsSystems;
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
    public void generate(SourceClassSets ss) {
        Set<String[]> packages = ss.getSimplePackageOfClasses().stream().map(
                this::getPackageStrings
        ).collect(Collectors.toSet());

        OverallEntryPoint overallEntryPoint = new OverallEntryPoint();

        // skeletons generation is independent and should be able to be done in parallel
        Set<ThreeSystem> skeletons = packages.parallelStream().flatMap(
                x -> generateSkeletonsFromPackages(x, overallEntryPoint).stream()
        ).collect(Collectors.toSet());

        Set<SourceClassSets.PackageAndClasses> packageAndClasses = ss.getPackageAndClasses();
        Set<ThreeSystem> setStream = packageAndClasses.stream().flatMap(
                x -> {
                    Set<Class<?>> collect = Arrays.stream(x.getClasses()).collect(Collectors.toSet());
                    return generateSkeletons(collect, Optional.of(x.getTargetPackageName()), overallEntryPoint).stream();
                }
        ).collect(Collectors.toSet());

        //
        addTests(Sets.union(skeletons, setStream));

        // create overall entry point
        overallEntryPoint.createOverallAccessPoints(ss.getPackageForOverall());
    }

    @Override
    public void generate(final List<Class> classes) {
        Utils.requireNotEmpty(classes);
        generate(new SourceClassSets(classes.get(0).getPackageName()));
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
