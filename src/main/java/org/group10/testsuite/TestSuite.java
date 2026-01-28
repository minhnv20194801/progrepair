package org.group10.testsuite;

import org.group10.program.Program;
import org.group10.utils.DummyOutputStream;
import org.group10.utils.FolderCleaner;
import org.group10.utils.instrument.CoverageInstrumenter;
import org.group10.utils.instrument.CoverageTracker;
import org.group10.utils.instrument.InstrumentingClassLoader;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

/**
 * Represents a test suite for a specific Java class. <br>
 *
 * The class containing the source code of the test suite, and methods
 * to execute the tests on a target program <br>
 *
 * It supports both silent execution and execution with logs.
 */
public class TestSuite {
    private final List<String> codes = new ArrayList<>();
    private List<String> testClasses = new ArrayList<>();

    /**
     * Constructs a {@link TestSuite} from a Java test file. <br>
     *
     * Reads the test file line by line, counts the number of tests (annotated with {@link Test}),
     * and stores the source code for later compilation and execution.
     *
     * @param path the path to the Java test file
     */
    public TestSuite(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                codes.add(line);
            }
        } catch (IOException e) {
            System.err.println("ERROR: Can't find test suite");
            System.exit(1);
        }
    }

    /**
     * Executes all tests of this test suite on a given {@link Program}.
     * <p>
     * This method compiles both the target program and the test suite, instruments
     * the classes to track code coverage, and executes each test individually.
     * It updates the target program's statistics:
     * <ul>
     *     <li>{@link Program#getEfs()} and {@link Program#getNfs()} for failed tests</li>
     *     <li>{@link Program#getEps()} and {@link Program#getNps()} for successful tests</li>
     * </ul>
     *
     * @param targetProgram the program on which tests will be executed
     * @param withLog     a boolean flag to print compilation and test execution logs
     * @throws Exception if any I/O, compilation, or reflection error occurs
     */
    public void executeTests(Program targetProgram, boolean withLog) throws Exception {
        try {
            targetProgram.tryCompile(withLog);
        } catch (Exception e) {
            if (withLog) {
                System.out.println("Program is not compilable");
                System.err.println(e.getMessage());
            }
            // if program is not compilable then silently return
            return;
        }

        long id = ProcessHandle.current().pid();
        Path outputDir = Files.createTempDirectory(targetProgram.getClassName() + id + "compiled_");
        compile(outputDir, targetProgram, withLog);

        ClassLoader loader = loadClasses(outputDir);

        try (LauncherSession session = LauncherFactory.openSession()) {
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            Launcher launcher = session.getLauncher();
            launcher.registerTestExecutionListeners(listener);
//            List<String> positiveTests = new ArrayList<>();
//            List<String> negativeTests = new ArrayList<>();
            int successCount = 0;
            int failedCount = 0;

            if (withLog) {
                System.out.println("==========================");
                System.out.println("TEST SUMMARY: " + targetProgram.getClassName());
                System.out.println("==========================");
            }

            for (String testClassName : testClasses) {
                Class<?> testClass = loader.loadClass(testClassName);
                List<Method> testMethods = Arrays.stream(testClass.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(Test.class))
                        .toList();

                for (Method m : testMethods) {
                    CoverageTracker.reset();
                    String testName = testClass.getSimpleName() + "@" + m.getName();
                    if (withLog) {
                        System.out.print("Executing test " + testName + ": ");
                    }
                    LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                            .selectors(selectMethod(testClass, m.getName()))
                            .build();
                    launcher.execute(request);

                    boolean isSucceed = (listener.getSummary().getTestsSucceededCount() == 1);

                    if (isSucceed) {
                        recordSuccessResult(targetProgram, testName);
                        successCount++;
                        if (withLog) {
                            System.out.println("✅");
                        }
                    } else {
                        recordFailedResult(targetProgram, testName);
                        failedCount++;
                        if (withLog) {
                            System.out.println("❌");
                        }
                    }
                }
            }

            if (withLog) {
                System.out.println((successCount + failedCount) + " tests executed");
                System.out.println(successCount + " tests successful");
                System.out.println(failedCount + " tests failed");
                System.out.println("==========================");
            }
        }
        FolderCleaner.cleanTmpDir(targetProgram.getClassName());
    }

    private void compile(Path outputDir, Program targetProgram, boolean withLog) throws Exception {
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
        OutputStream dummyOutputStream = withLog ? null : new DummyOutputStream();

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
            if (withLog) {
                System.err.println("Test Suite compile failure");
            }
            FolderCleaner.cleanTmpDir(targetProgram.getClassName());
        }
    }

    private ClassLoader loadClasses(Path classDir) throws Exception {
        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        testClasses.clear();

        try (Stream<Path> paths = Files.walk(classDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        try {
                            String className = classDir.relativize(p)
                                    .toString()
                                    .replace(FileSystems.getDefault().getSeparator(), ".")
                                    .replaceAll("\\.class$", "");

                            byte[] classBytes = Files.readAllBytes(p);
                            if (className.endsWith("Test")) {
                                testClasses.add(className);
                                loader.addClass(className, classBytes);
                            } else {
                                // If the class is not a test, then instrument the class
                                byte[] instrumented = CoverageInstrumenter.instrument(classBytes);
                                loader.addClass(className, instrumented);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to load class from " + p, e);
                        }
                    });
        }

        return loader;
    }

    private void recordSuccessResult(Program targetProgram, String testName) {
        List<Integer> hitLines = CoverageTracker.getExecutedLines();

        for (int i = 0; i < targetProgram.getCodes().size(); i++) {
            if (hitLines.contains(i)) {
                Map<Integer, Integer> eps = targetProgram.getEps();
                eps.put(i, eps.getOrDefault(i, 0) + 1);
            } else {
                Map<Integer, Integer> nps = targetProgram.getNps();
                nps.put(i, nps.getOrDefault(i, 0) + 1);
            }
        }

        targetProgram.getPositiveTests().add(testName);
    }

    private void recordFailedResult(Program targetProgram, String testName) {
        List<Integer> hitLines = CoverageTracker.getExecutedLines();

        for (int i = 0; i < targetProgram.getCodes().size(); i++) {
            if (hitLines.contains(i)) {
                Map<Integer, Integer> efs = targetProgram.getEfs();
                efs.put(i, efs.getOrDefault(i, 0) + 1);
            } else {
                Map<Integer, Integer> nfs = targetProgram.getNfs();
                nfs.put(i, nfs.getOrDefault(i, 0) + 1);
            }
        }

        targetProgram.getNegativeTests().add(testName);
    }

    /**
     * Writes the test suite source code to a specified directory.
     *
     * @param outputDir the directory to write the test file to
     * @param className the base name for the test file (e.g., "MyClass" -> "MyClassTest.java")
     * @throws Exception if an I/O error occurs while writing the file
     */
    public void toFile(String outputDir, String className) throws Exception {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        Path javaFile = dir.resolve(className + "Test.java");
        Files.write(javaFile, codes);
    }
}