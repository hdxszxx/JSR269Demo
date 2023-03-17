package org.example.processor.jsr269.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import org.example.processor.jsr269.annotations.ToBeTested;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @version 1.0
 * @date 2023/3/16 16:35
 */
@SupportedAnnotationTypes("org.example.processor.jsr269.annotations.ToBeTested")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ToBeTestedProcessor extends AbstractProcessor {

    private void note(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ToBeTested.class);
        if (elements == null || elements.size() == 0) {
            return true;
        }
        ProcessingEnvironment unwrappedprocessingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        final Context context = ((JavacProcessingEnvironment) unwrappedprocessingEnv).getContext();
        final JavacElements elementUtils = (JavacElements) processingEnv.getElementUtils();
        final TreeMaker treeMaker = TreeMaker.instance(context);
        for (Element element : elements) {
            ToBeTested annotation = element.getAnnotation(ToBeTested.class);
            String owner = annotation.owner();
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            treeMaker.pos = jcMethodDecl.pos;
            //修改AST的逻辑
            jcMethodDecl.body = treeMaker.Block(0, List.of(
                    treeMaker.Exec(
                            treeMaker.Apply(
                                    List.<JCTree.JCExpression>nil(),
                                    treeMaker.Select(
                                            treeMaker.Select(
                                                    treeMaker.Ident(
                                                            elementUtils.getName("System")
                                                    ),
                                                    elementUtils.getName("out")
                                            ),
                                            elementUtils.getName("println")
                                    ),
                                    List.<JCTree.JCExpression>of(
                                            treeMaker.Literal("Method " + element.getSimpleName() + " invoke by " + owner + ".")
                                    )
                            )
                    ),
                    jcMethodDecl.body));
        }
        return true;
    }


    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
        }
        return unwrapped != null ? unwrapped : wrapper;
    }
}
