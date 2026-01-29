# Group10-ProgRepair

This project tried to simulate how classic **GenProg** algorithm worked to fix Java programs.

The assumptions of this tool are:
- A single file Java source code
- A single file Java test suite for the target class
- The source code of target class and the test suite stored in the same directory
- The test suite file name is the target class + "Test.java"

  For example: if the target class is **IntCalculator** (in **IntCalculator.java**) 
  then the test suite have to be **IntCalculatorTest.java**

The project is built with **Maven**, targets **Java 21**, and produces a **fat (shaded) JAR** for easy distribution.

---

## 🧠 GenProg Simulation Overview

This project implements the core components of the classic GenProg algorithm, including several extensions and modifications:

### 1. Static Fault Localization
Implemented techniques:
- **Ochiai**
- **Tarantula**

---

### 2. Mutation Operators
Mutations are applied at the **statement level** of the program’s Abstract Syntax Tree (AST).

Like classic **GenProg**, mutation operations are chosen randomly between:
- **Insert** a statement
- **Delete** a statement
- **Swap** two statements

We also experimenting with mutation by adding a new mutation operation (which is disabled by default):
- **Binary Expression Modification**  
  Example: replacing `+` with `-`, `>` with `<`

---

### 3. Crossover Operator
The crossover operator works at the **raw source code (text) level**:

- A crossover point is selected
- Two offspring are generated:
    - Offspring 1 = first part of Parent A + second part of Parent B
    - Offspring 2 = first part of Parent B + second part of Parent A

---

### 4. Fitness Function
A **Weighted Fitness Function** evaluates how good a candidate patch is.

- Rewards patches that fix tests failed by the original program
- Uses different weights for:
    - **Positive tests** (originally passing)
    - **Negative tests** (originally failing)

---

### 5. Selection Strategy
The project uses **Binary Tournament Selection**:

- Two random individuals are selected
- The one with the better fitness value is chosen

---

### 6. Search Algorithm (Modified GenProg)
This project implements a modified version of the classic GenProg search algorithm:

- **Only compilable programs** are allowed into the next generation
- In contrast, the original **GenProg** may keep non-compilable programs

This design choice aim to maximize a generation's effective search space, as in original **GenProg** the non-compilable
program got the worst fitness, which will eventually get eliminated in later generations.

---

## 📁 Main Components Location

- **Fault Localization**
    - `src/main/java/org/group10/suspiciouscalculator/OchiaiSuspiciousCalculator.java`
    - `src/main/java/org/group10/suspiciouscalculator/TarantulaSuspiciousCalculator.java`

- **Mutation**
    - `src/main/java/org/group10/mutator/ClassicGenProgMutator.java`

- **Crossover**
    - `src/main/java/org/group10/crossover/RawProgramCrossover.java`

- **Fitness Function**
    - `src/main/java/org/group10/fitness/WeightedFitnessFunction.java`

- **Selection**
    - `src/main/java/org/group10/selection/ProgramBinaryTournamentSelection.java`

- **Search Algorithm**
    - `src/main/java/org/group10/searchalgorithm/ClassicGenProgAlgorithm.java`

- **Program Representation**
    - `src/main/java/org/group10/program/Program.java`

- **Test Suite Representation**
    - `src/main/java/org/group10/testsuite/TestSuite.java`

---

## 🛠️ Requirements

- **Java JDK 21** or newer
- **Maven 3.9+**

Verify your setup:

```bash
$ java --version
$ mvn --version
```

After that, you can compile the project with:
```bash
$ mvn clean package -DskipTests
```
Verify installations:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar --version
progrep_group10 1.0
```

### Optional (but recommened)
Using docker for installations:
```bash
$ docker build -t group10-progrep .
$ docker run -it --rm group10-progrep bash
```
And then the normal:
```bash
$ mvn clean package -DskipTests
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar --version
progrep_group10 1.0
```

---

## 🚀 Features

### Test
Run the tests of the program to verify the environment

Command:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar test
Required options: '--dir=<dirPath>', '--class=<classname>'
Options:
  -c, --class=<classname> : Name of the class to be test
  -d, --dir=<dirPath>     : The directory path lead to the classes
```
Example:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar test -d benchmark/IntCalculator_buggy/ -c IntCalculator
==========================
TEST SUMMARY: IntCalculator
==========================
Executing test IntCalculatorTest@testAdd: ❌
Executing test IntCalculatorTest@testSubtract: ❌
Executing test IntCalculatorTest@testMultiply: ✅
Executing test IntCalculatorTest@testDivide: ✅
Executing test IntCalculatorTest@testDivideByZero: ✅
5 tests executed
3 tests successful
2 tests failed
==========================
```

### Benchmark
Run the tools on the pre-defined benchmark multiple time

List of pre-defined benchmark targets:

| Index | Target  Directory                   | Target Class       | Bug Type                                 |
|-------|-------------------------------------|--------------------|------------------------------------------|
| 0     | benchmark/IntCalculator_buggy/      | IntCalculator      | Add and Subtract operations are reversed | 
| 1     | benchmark/Stack_buggy/              | Stack              | Missing empty (null) check               |
| 2     | benchmark/VIPCustomer_buggy/        | VIPCustomer        | Super.pay(amount) is called twice        |
| 3     | benchmark/Counter_buggy/            | Counter            | Variable shadow the class field          |
| 4     | benchmark/Shop_buggy/               | Shop               | Missing empty (null) check               |
| 5     | benchmark/BinaryExprExamples_buggy/ | BinaryExprExamples | Use the wrong binary expressions         |

Command:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar benchmark
Required option: '--benchmark=<benchmarkTarget>'
Options:
  -b, --benchmark=<benchmarkTarget>: index of the benchmark target to run on

      -fl, --fault_localization=<faultLocalization>: type of fault localization technique to be used
                                                     Currently support: ochiai and tarantula
                                                     Default value: ochiai

  -g, --generation=<maxGeneration>: maximum generation for the search algorithm
                                    Default value: 200

  -m, --mutation=<mutationWeight>: probability a mutation to happen
                                   Default value: 0.06
      --multiclass_mutation      : boolean flag to allow getting fixes from different class
                                   Default value: false
      --mutate_binaryexprs       : boolean flag to allow for binary expression modify mutation
                                   operations to happen
                                   Default value: false
      --neg_weight=<negativeWeight>: fitness function weight associate with negative tests 
                                     Default value: 10
      --pos_weight=<positiveWeight>: fitness function weight associate with positive tests
                                     Default value: 1
                                     
      -out, --output_dir=<outputDir>: output directory for the patches the tool have found
                                      if null then the patches will not get print out to files
                                      Default value: null

  -p, --population=<populationSize>: maximum population size of the search
                                     Default value: 40


  -r, --runs=<runs>: number of runs the tool will do to benchmark target
                     Default value: 10
```
Example:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar benchmark -b 0
```

### Repair
Use the tool on a given (buggy) class.
Command:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar repair
Required options: '--dir=<dirPath>', '--class=<classname>'
Options:
  -c, --class=<classname> : Name of the class to be test
  -d, --dir=<dirPath>     : The directory path lead to the classes
      -fl, --fault_localization=<faultLocalization>: type of fault localization technique to be used
                                                     Currently support: ochiai and tarantula
                                                     Default value: ochiai

  -g, --generation=<maxGeneration>: maximum generation for the search algorithm
                                    Default value: 200

  -m, --mutation=<mutationWeight>: probability a mutation to happen
                                   Default value: 0.06
      --multiclass_mutation      : boolean flag to allow getting fixes from different class
                                   Default value: false
      --mutate_binaryexprs       : boolean flag to allow for binary expression modify mutation
                                   operations to happen
                                   Default value: false
      --neg_weight=<negativeWeight>: fitness function weight associate with negative tests 
                                     Default value: 10
      --pos_weight=<positiveWeight>: fitness function weight associate with positive tests
                                     Default value: 1
                                     
      -out, --output_dir=<outputDir>: output directory for the patches the tool have found
                                      if null then the patches will not get print out to files
                                      Default value: null

  -p, --population=<populationSize>: maximum population size of the search
                                     Default value: 40
```

Example:
```bash
$ java -jar target/ProgRepair-1.0-SNAPSHOT.jar repair -d benchmark/IntCalculator_buggy/ -c IntCalculator
```
