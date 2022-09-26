package me.dragonite;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class) // 프로세서로 등록
public class MagicMojaProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(Magic.class.getName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Magic Annotation이 붙은 element 검색
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Magic.class);
        for(Element element: elements){
            Name simpleName = element.getSimpleName();
            // element가 INTERFACE인 경우
            if(element.getKind() != ElementKind.INTERFACE){
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR,
                                "Magic annotation cant not be used on "+ simpleName);
            } else {
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.NOTE,
                                "Processing "+ simpleName);
            }

            // 새로운 소스코드를 생성
            TypeElement typeElement = (TypeElement) element;
            ClassName className = ClassName.get(typeElement);
            // 메소드
            MethodSpec pullOut = MethodSpec.methodBuilder("pullOut")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return $S", "Rabbit!")
                    .build();
            // 클래스
            TypeSpec magicMoja = TypeSpec.classBuilder("MagicMoja")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(className)
                    .addMethod(pullOut)
                    .build();

            Filer filer = processingEnv.getFiler();
            // Java File 생성
            try {
                JavaFile.builder(className.packageName(), magicMoja)
                        .build()
                        .writeTo(filer); // Filer에 바로 쓸수있음
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR : "+e);
            }
        }
        return true;
    }
}
