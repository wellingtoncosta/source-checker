package sourcechecker;

import com.google.auto.service.AutoService;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.util.Trees;
import sourcechecker.annotation.SourceCheckerConfiguration;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class SourceCheckerProcessor extends AbstractProcessor {

    private Messager messager;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.messager = processingEnvironment.getMessager();
        this.trees = Trees.instance(processingEnv);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(!roundEnv.processingOver()) { // check only non-generated sources
            Element configurations = getConfigurations(roundEnv);
            if(configurations != null) {
                roundEnv.getRootElements()
                        .stream()
                        .filter(element -> element.getAnnotation(SourceCheckerConfiguration.class) == null)
                        .forEach(element -> validateElements(configurations, element));
            }
        }

        return false;
    }

    private Element getConfigurations(RoundEnvironment roundEnvironment) {
        List<Element> elements = roundEnvironment.getRootElements()
                .stream()
                .filter(element -> element.getAnnotation(SourceCheckerConfiguration.class) != null)
                .collect(Collectors.toList());

        if(elements.size() == 0) {
            logError(
                    null,
                    "Configuration not found. Please use the @%s annotation.",
                    SourceCheckerConfiguration.class.getSimpleName()
            );

            return null;
        }

        return elements.get(0);
    }

    private void validateElements(Element configurations, Element element) {
        checkClassSize(configurations, element);
        checkMethodSize(configurations, element);
        checkWhileTrue(configurations, element);
    }

    private void checkClassSize(Element configurations, Element element) {
        long maxLinesOfCodePerClass = configurations.getAnnotation(SourceCheckerConfiguration.class).maxLinesOfCodePerClass();
        long classSize = fieldsCount(element) + methodsCount(element);

        if(classSize > maxLinesOfCodePerClass) {
            logError(
                    element,
                    "The class %s is too large. The limit of lines of code per class is %d.",
                    element.getSimpleName(),
                    maxLinesOfCodePerClass
            );
        }
    }

    private long fieldsCount(Element element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .count();
    }

    private long methodsCount(Element element) {
        return getMethods(element)
                .stream()
                .mapToLong(value -> trees.getTree(value).getBody().getStatements().size())
                .sum();
    }

    private void checkMethodSize(Element configurations, Element element) {
        long maxLinesOfCodePerMethod = configurations.getAnnotation(SourceCheckerConfiguration.class).maxLinesOfCodePerMethod();

        getMethods(element).forEach(method -> {
            MethodTree methodTree = trees.getTree(method);
            BlockTree blockTree = methodTree.getBody();

            if(blockTree.getStatements().size() > maxLinesOfCodePerMethod) {
                logError(
                        method,
                        "The method %s of the class %s is too large. The limit of lines of code per method is %d.",
                        methodTree.getName(),
                        element.getSimpleName(),
                        maxLinesOfCodePerMethod
                );
            }
        });
    }

    private void checkWhileTrue(Element configurations, Element element) {
        boolean avoidWhileTrue = configurations.getAnnotation(SourceCheckerConfiguration.class).avoidWhileTrue();

        if(avoidWhileTrue) {
            getMethods(element).forEach(method -> {
                MethodTree methodTree = trees.getTree(method);
                BlockTree blockTree = methodTree.getBody();

                List<StatementTree> whileTrueStatements = blockTree.getStatements()
                        .stream()
                        .filter(statement -> statement.toString().contains("while"))
                        .filter(statement -> statement.toString().contains("true"))
                        .collect(Collectors.toList());

                if(whileTrueStatements.size() > 0) {
                    logError(method, "The while(true) statement is not allowed.");
                }
            });
        }
    }

    private List<ExecutableElement> getMethods(Element element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind().equals(ElementKind.METHOD))
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());
    }

    private void logError(Element element, String message, Object... args) {
        logError(element, String.format(message, args));
    }

    private void logError(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

}