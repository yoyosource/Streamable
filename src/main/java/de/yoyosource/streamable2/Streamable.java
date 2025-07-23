package de.yoyosource.streamable2;

import de.yoyosource.TestOther;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.*;

import static java.lang.constant.ConstantDescs.*;

public class Streamable<T> {

    private List<PipelineStep> steps = new ArrayList<>();

    private Streamable add(PipelineStep step) {
        steps.add(step);
        return this;
    }

    public void run() {
        try {
            ClassDesc name = ClassDesc.of("", UUID.randomUUID().toString().replace("-", ""));

            byte[] bytes = ClassFile.of().build(name, classBuilder -> {
                classBuilder.withFlags(AccessFlag.PUBLIC)
                        .withVersion(67, 0);

                ClassDesc[] parameterTypes = steps.stream().flatMap(pipelineStep -> pipelineStep.capturedArgumentTypes.stream())
                        .toArray(ClassDesc[]::new);
                classBuilder.withMethod("run", MethodTypeDesc.of(CD_void, parameterTypes), AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                    methodBuilder.withCode(codeBuilder -> {
                        codeBuilder.aconst_null();
                        Label end = codeBuilder.newLabel();

                        Iterator<PipelineStep> stepIterator = steps.iterator();
                        PipelineStep.GenerateRunData data = new PipelineStep.GenerateRunData(name, codeBuilder, 0, end, null);
                        Runnable runnable = () -> {
                            while (stepIterator.hasNext()) {
                                stepIterator.next().generateRun(data);
                            }
                        };
                        data.continuation = runnable;
                        runnable.run();
                        codeBuilder.labelBinding(end);
                        codeBuilder.return_();
                    });
                });

                for (PipelineStep step : steps) {
                    MethodModel methodModel = step.methodModel;
                    classBuilder.withMethod(methodModel.methodName().stringValue(), step.methodTypeDesc, AccessFlag.PRIVATE.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                        methodBuilder.transformCode(methodModel.code().get(), CodeTransform.ACCEPT_ALL);
                    });
                }
            });

            File file = new File("C:/Dev/Projekte/Git - Schule/Streamable/TestClass.class");
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public <R> Streamable<R> map(TestOther.StreamableFunction<T, R> mapper) {
        return add(new PipelineStep(mapper) {
            @Override
            public void generateRun(GenerateRunData runData) {
                runData.generateSlotData(this, false);
                runData.codeBuilder.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc);
                if (!methodTypeDesc.returnType().equals(methodTypeDesc.parameterType(0))) {
                    runData.codeBuilder.checkcast(methodTypeDesc.returnType());
                }
            }
        });
    }

    public Streamable<T> filter(TestOther.StreamablePredicate<T> predicate) {
        return add(new PipelineStep(predicate) {
            @Override
            public void generateRun(GenerateRunData runData) {
                runData.generateSlotData(this, true);
                runData.codeBuilder.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc)
                        .ifeq(runData.end);
            }
        });
    }

    public Streamable<T> peek(TestOther.StreamableConsumer<T> consumer) {
        return add(new PipelineStep(consumer) {
            @Override
            public void generateRun(GenerateRunData runData) {
                runData.generateSlotData(this, true);
                runData.codeBuilder.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc);
            }
        });
    }

    public <R> Streamable<R> flatMap(TestOther.StreamableFunction<T, Iterable<R>> mapper) {
        return add(new PipelineStep(mapper) {
            @Override
            public void generateRun(GenerateRunData runData) {
                runData.generateSlotData(this, false);
                runData.codeBuilder.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc);
                Label startLoop = runData.codeBuilder.newLabel();
                Label endLoop = runData.codeBuilder.newLabel();
                runData.codeBuilder.invokeinterface(ClassDesc.of("java.lang", "Iterable"), "iterator", MethodTypeDesc.of(ClassDesc.of("java.util", "Iterator")))
                        .labelBinding(startLoop)
                        .dup()
                        .invokeinterface(ClassDesc.of("java.util", "Iterator"), "hasNext", MethodTypeDesc.of(CD_boolean))
                        .ifeq(endLoop)
                        .dup()
                        .invokeinterface(ClassDesc.of("java.util", "Iterator"), "next", MethodTypeDesc.of(CD_Object));
                runData._continue();
                runData.codeBuilder.pop()
                        .goto_(startLoop)
                        .labelBinding(endLoop);
            }
        });
    }
}
