package de.yoyosource;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.lang.constant.ConstantDescs.*;

public class PipelineBuilder {

    private static final class PipelineClassLoader extends ClassLoader {
        private static final PipelineClassLoader INSTANCE = new PipelineClassLoader();

        public Class<?> loadClass(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    private String id = UUID.randomUUID().toString().replaceAll("-", "")
            .replace('0', 'g')
            .replace('1', 'h')
            .replace('2', 'i')
            .replace('3', 'j')
            .replace('4', 'k')
            .replace('5', 'l')
            .replace('6', 'm')
            .replace('7', 'n')
            .replace('8', 'o')
            .replace('9', 'p');

    private int localIndex = 0;

    @RequiredArgsConstructor
    private class ArgumentData {
        private final Object argument;
        private final Integer index;
        private final ClassDesc type;
    }

    @RequiredArgsConstructor
    private class MethodData {
        private final MethodModel methodModel;
        private List<ArgumentData> arguments = new ArrayList<>();
    }

    private List<ArgumentData> arguments = new ArrayList<>();
    private List<MethodData> steps = new ArrayList<>();

    public PipelineBuilder() {
    }

    public <I, O> PipelineBuilder map(TestOther.StreamableFunction<I, O> step) {
        return add(step);
    }

    public <I> PipelineBuilder filter(TestOther.StreamablePredicate<I> step) {
        return add(step);
    }

    public PipelineBuilder peek(TestOther.StreamableRunnable step) {
        return add(step);
    }

    public PipelineBuilder add(TestOther.StreamableLambda step) {
        try {
            Method method = step.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(step);

            InputStream inputStream = TestOther.class.getResourceAsStream("/" + lambda.getImplClass() + ".class");
            ClassModel model = ClassFile.of().parse(inputStream.readAllBytes());
            MethodModel methodModel = model.elementStream().filter(MethodModel.class::isInstance)
                    .map(MethodModel.class::cast)
                    .filter(mm -> mm.methodName().equalsString(lambda.getImplMethodName()))
                    .findFirst()
                    .orElse(null);

            ClassDesc[] classDescs = methodModel.methodTypeSymbol().parameterArray();
            MethodData methodData = new MethodData(methodModel);
            for (int i = 0; i < lambda.getCapturedArgCount(); i++) {

                ArgumentData argumentData = null;
                Object data = lambda.getCapturedArg(i);
                for (ArgumentData argument : arguments) {
                    if (argument.argument == data) {
                        argumentData = argument;
                        break;
                    }
                }
                if (argumentData == null) {
                    argumentData = new ArgumentData(data, localIndex, classDescs[i]);
                    arguments.add(argumentData);
                }

                methodData.arguments.add(argumentData);
                localIndex += TypeKind.from(argumentData.type).slotSize();
            }
            steps.add(methodData);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return this;
    }

    public Pipeline build() throws IOException, IllegalAccessException {
        System.out.println("Building pipeline...");

        byte[] bytes = ClassFile.of().build(ClassDesc.of("", id), classBuilder -> {
            classBuilder.withFlags(AccessFlag.PUBLIC)
                    .withVersion(67, 0);

            ClassDesc[] types = arguments.stream().map(argumentData -> argumentData.type).toArray(ClassDesc[]::new);
            classBuilder.withMethod("run", MethodTypeDesc.of(CD_void, types), AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                methodBuilder.withCode(codeBuilder -> {
                    for (int i = 0; i < steps.size(); i++) {
                        MethodData methodData = steps.get(i);
                        MethodModel methodModel = methodData.methodModel;
                        methodData.arguments.forEach(argumentData -> {
                            codeBuilder.loadLocal(TypeKind.from(argumentData.type), argumentData.index);
                        });
                        codeBuilder.new_(CD_Integer)
                                .dup()
                                .iconst_0()
                                .invokespecial(CD_Integer, "<init>", MethodTypeDesc.of(CD_void, CD_int))
                                // .checkcast(CD_Integer)
                                .invokestatic(ClassDesc.of("", id), "step$" + i, methodModel.methodTypeSymbol());
                    }
                    codeBuilder.return_();
                });
            });

            for (int i = 0; i < steps.size(); i++) {
                MethodModel step = steps.get(i).methodModel;
                classBuilder.withMethod(classBuilder.constantPool().utf8Entry("step$" + i), step.methodType(), AccessFlag.PRIVATE.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                    methodBuilder.transformCode(step.code().get(), CodeTransform.ACCEPT_ALL);
                });
            }
        });

        File file = new File("/Users/jojo/IdeaProjects/Streamable/TestClass.class");
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        fileOutputStream.close();

        Class<?> clazz = PipelineClassLoader.INSTANCE.loadClass(id, bytes);
        Method method = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals("run"))
                .findFirst()
                .orElseThrow();
        MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
        return new Pipeline(methodHandle, arguments.stream().map(argumentData -> argumentData.argument).toArray());
    }

    public static class Pipeline {

        private final MethodHandle methodHandle;
        private final Object[] arguments;

        private Pipeline(MethodHandle methodHandle, Object[] arguments) {
            this.methodHandle = methodHandle;
            this.arguments = arguments;
        }

        public void run() throws Throwable {
            System.out.println("Running pipeline...");
            methodHandle.invokeWithArguments(arguments);
        }
    }
}
