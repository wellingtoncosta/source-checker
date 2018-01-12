# source-checker

This project is a simple example of static source code analysis at compile-time using Java Annotation Processing.

Example:

__Step 1 - Define the code rules by the__ ```@SourceCheckerConfiguration``` __annotation__

```java
@SourceCheckerConfiguration(
        maxLinesOfCodePerClass = 100,
        maxLinesOfCodePerMethod = 6,
        avoidWhileTrue = true
)
public class Configuration { }
```

__Step 2 - Compile the project__

The code rules will be applied to the sources during the project compilation.

__If any rule is violated, a compilation error will be shown in console output explaining the violations and where they occurred, and the project will be not compiled. Consequently, the__ ```.class``` __files will be not generated.__

*Note: If you use this tool on your project, you must to define the code rules by the* ```@SourceCheckerConfiguration``` *annotation, otherwise an error will be shown in console output.*