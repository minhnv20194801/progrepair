package org.group10.testsuite;

import org.group10.utils.FolderCleaner;
import org.group10.utils.instrument.CoverageInstrumenter;
import org.group10.utils.instrument.CoverageTracker;
import org.group10.utils.instrument.InstrumentingClassLoader;
import org.group10.program.Program;
import org.group10.utils.DummyOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class TestSuite {
    private final List<String> codes = new ArrayList<>();
    private int numberOfTests;

    public TestSuite(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                codes.add(line);
                if (line.contains("@Test")) {
                    numberOfTests++;
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: Can't find test suite");
            System.exit(1);
        }
    }

    public void executeTests(Program targetProgram) throws Exception {
        executeTests(targetProgram, false);
    }

    public void executeTests(Program targetProgram, boolean isShowLog) throws Exception {
        if (targetProgram.isNotCompilable()) {
            if (isShowLog) {
                System.out.println("Program is not compilable");
            }
            // if program is not compilable then silently return
            return;
        }

        long id = ProcessHandle.current().pid();
        Path outputDir = Files.createTempDirectory(targetProgram.getClassName()+id+"compiled_");

        Path targetProgramFile = outputDir.resolve(targetProgram.getClassName() + ".java");
        Files.write(targetProgramFile, targetProgram.getCodes());
        Path testFile = outputDir.resolve(targetProgram.getClassName() + "Test.java");
        Files.write(testFile, codes);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            FolderCleaner.cleanTmpDir(targetProgram.getClassName());
            throw new IllegalStateException("No Java compiler available (JRE instead of JDK)");
        }

        // dummyOutputStream so the compiler error or warning does not go out
        OutputStream dummyOutputStream = isShowLog? null : new DummyOutputStream();

        int result = compiler.run(
                null,
                dummyOutputStream,
                dummyOutputStream,
                "-g",
                "-d", outputDir.toString(),
                targetProgramFile.toString(),
                testFile.toString()
        );

        if (result != 0) {
            if (isShowLog) {
                System.err.println("Test Suite compile failure");
            }
            FolderCleaner.cleanTmpDir(targetProgram.getClassName());
            return;
        }

        InstrumentingClassLoader loader = new InstrumentingClassLoader();

        List<String> testClasses = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(outputDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        try {
                            String className = outputDir.relativize(p)
                                    .toString()
                                    .replace(FileSystems.getDefault().getSeparator(), ".")
                                    .replaceAll("\\.class$", "");

                            byte[] classBytes = Files.readAllBytes(p);
                            if (className.endsWith("Test")) {
                                testClasses.add(className);
                                loader.addClass(className, classBytes);
                            } else {
                                byte[] instrumented = CoverageInstrumenter.instrument(classBytes);
                                loader.addClass(className, instrumented);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to load class from " + p, e);
                        }
                    });
        }

        try (LauncherSession session = LauncherFactory.openSession()) {
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            Launcher launcher = session.getLauncher();
            launcher.registerTestExecutionListeners(listener);
            List<String> positiveTests = new ArrayList<>();
            List<String> negativeTests = new ArrayList<>();

            if (isShowLog) {
                System.out.println("==========================");
                System.out.println("TEST SUMMARY: " + targetProgram.getClassName());
                System.out.println("==========================");
            }

            for (String testClassName: testClasses) {
                Class<?> testClass = loader.loadClass(testClassName);
                for (Method m : testClass.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Test.class)) {
                        CoverageTracker.reset();
                        if (isShowLog) {
                            System.out.print("Executing test " + testClass.getSimpleName() + "@" + m.getName() + ": ");
                        }
                        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                                .selectors(selectMethod(testClass, m.getName()))
                                .build();
                        launcher.execute(request);

                        boolean isSucceed = (listener.getSummary().getTestsSucceededCount() == 1);
                        List<Integer> hitLines = CoverageTracker.getExecutedLines();
                        if (isSucceed) {
                            positiveTests.add(testClass.getSimpleName() + "@" + m.getName());

                            for (int i = 0; i < targetProgram.getCodes().size(); i++) {
                                if (hitLines.contains(i)) {
                                    Map<Integer, Integer> eps = targetProgram.getEps();
                                    eps.put(i, eps.getOrDefault(i, 0) + 1);
                                } else {
                                    Map<Integer, Integer> nps = targetProgram.getNps();
                                    nps.put(i, nps.getOrDefault(i, 0) + 1);
                                }
                            }

                            if (isShowLog) {
                                System.out.println("✅");
                            }
                        } else {
                            negativeTests.add(testClass.getSimpleName() + "@" + m.getName());
                            for (int i = 0; i < targetProgram.getCodes().size(); i++) {
                                if (hitLines.contains(i)) {
                                    Map<Integer, Integer> efs = targetProgram.getEfs();
                                    efs.put(i, efs.getOrDefault(i, 0) + 1);
                                } else {
                                    Map<Integer, Integer> nfs = targetProgram.getNfs();
                                    nfs.put(i, nfs.getOrDefault(i, 0) + 1);
                                }
                            }

                            if (isShowLog) {
                                System.out.println("❌");
                            }
                        }
                    }
                }
            }

            targetProgram.setPositiveTests(positiveTests);
            targetProgram.setNegativeTests(negativeTests);

            if (isShowLog) {
                System.out.println((positiveTests.size() + negativeTests.size()) + " tests executed");
                System.out.println(positiveTests.size() + " tests successful");
                System.out.println(negativeTests.size() + " tests failed");
                System.out.println("==========================");
            }
        }
        FolderCleaner.cleanTmpDir(targetProgram.getClassName());
    }
}