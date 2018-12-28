package stjava.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import stjava.annotation.StructurallyTyped;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class StructuralTypeProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<TypeMirror, List<? extends Element>> interfaces = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    private List<? extends Element> allMembers(TypeElement typeElement) {
        return elementUtils.getAllMembers(typeElement).stream()
                .filter(e -> ((Element) e).getKind() == ElementKind.METHOD)
                .filter(e -> !"java.lang.Object".equals(((Symbol.MethodSymbol)e).owner.getQualifiedName().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getRootElements()) {
            if (element.getKind() == ElementKind.INTERFACE) {
                TypeElement typeElement = (TypeElement) element;
                interfaces.put(typeElement.asType(), allMembers(typeElement));
            }
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(StructurallyTyped.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            messager.printMessage(Diagnostic.Kind.NOTE, typeElement.getQualifiedName());
            try {
                generateCode(typeElement);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        return true;
    }

    private void generateCode(TypeElement typeElement) throws IOException {

        String name = "STIo" + typeElement.getSimpleName().toString();

        TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        Map<String, ? extends Element> enclosedElements = allMembers(typeElement).stream()
                .collect(Collectors.toMap(e -> e.getSimpleName().toString(), e -> e));

        for (Map.Entry<TypeMirror, List<? extends Element>> entry : interfaces.entrySet()) {
            boolean equals = true;
            TypeMirror ifaceType = entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "Processing " + ifaceType.toString());
            List<? extends Element> ifaceTypeElements = entry.getValue();
            for (Element element : ifaceTypeElements) {
                if (!enclosedElements.containsKey(element.getSimpleName().toString())) {
                    equals = false;
                    break;
                }
                Element enclosedElement = enclosedElements.get(element.getSimpleName().toString());
                Symbol.MethodSymbol enclosedMethodSymbol = (Symbol.MethodSymbol) enclosedElement;
                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
                if (!typeUtils.isSameType(enclosedMethodSymbol.getReturnType(), methodSymbol.getReturnType())) {
                    equals = false;
                    break;
                }
                if (enclosedMethodSymbol.params().length() != methodSymbol.params().length()) {
                    equals = false;
                    break;
                }
                for (int i = 0; i < methodSymbol.params().length(); i++) {
                    if (!typeUtils.isSameType(methodSymbol.params().get(i).asType(), enclosedMethodSymbol.params().get(i).asType())) {
                        equals = false;
                        break;
                    }
                }
            }
            if (equals && !ifaceTypeElements.isEmpty()) { // do not allow empty (marker) interface to sneak here
                typeSpecBuilder.addSuperinterface(TypeName.get(ifaceType));
            }
        }

        TypeSpec typeSpec = typeSpecBuilder.build();

        JavaFile.builder(((Symbol.ClassSymbol) typeElement).packge().fullname.toString(), typeSpec)
                .build()
                .writeTo(filer);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(StructurallyTyped.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}