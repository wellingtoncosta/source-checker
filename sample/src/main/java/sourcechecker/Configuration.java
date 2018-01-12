package sourcechecker;

import sourcechecker.annotation.SourceCheckerConfiguration;

@SourceCheckerConfiguration(
        maxLinesOfCodePerClass = 100,
        maxLinesOfCodePerMethod = 6,
        avoidWhileTrue = true
)
public class Configuration { }