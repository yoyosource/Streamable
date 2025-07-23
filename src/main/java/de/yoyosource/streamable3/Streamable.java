package de.yoyosource.streamable3;

import de.yoyosource.TestOther;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_void;

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

                Map<Object, ClassDesc> types = new IdentityHashMap<>();
                Map<Object, Integer> slots = new IdentityHashMap<>();

                int slotCounter = 0;
                for (PipelineStep step : steps) {
                    for (int i = 0; i < step.capturedArguments.size(); i++) {
                        Object arg = step.capturedArguments.get(i);
                        ClassDesc argClass = step.capturedArgumentTypes.get(i);

                        if (types.containsKey(arg)) continue;

                        types.put(arg, argClass);
                        slots.put(arg, slotCounter);
                        slotCounter += TypeKind.from(argClass).slotSize();
                    }
                }

                ClassDesc[] parameterTypes = slots.entrySet().stream().sorted(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .map(types::get)
                        .toArray(ClassDesc[]::new);

                int finalSlotCounter = slotCounter;
                classBuilder.withMethod("run", MethodTypeDesc.of(CD_void, parameterTypes), AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                    methodBuilder.withCode(codeBuilder -> {
                        codeBuilder.aconst_null().astore(finalSlotCounter);
                        Label end = codeBuilder.newLabel();

                        Iterator<PipelineStep> stepIterator = steps.iterator();
                        PipelineStep.Generator data = new PipelineStep.Generator(name, codeBuilder, generator -> {
                            if (stepIterator.hasNext()) {
                                stepIterator.next().generate(generator);
                            }
                        }, slots);
                        data.next(finalSlotCounter, end);
                        codeBuilder.labelBinding(end);
                        codeBuilder.return_();
                    });
                });

                for (PipelineStep step : steps) {
                    MethodModel methodModel = step.methodModel;
                    if (methodModel == null) continue;
                    classBuilder.withMethod(methodModel.methodName().stringValue(), step.methodTypeDesc, AccessFlag.PRIVATE.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                        methodBuilder.transformCode(methodModel.code().get(), CodeTransform.ACCEPT_ALL);
                    });
                }
            });

            File file = new File("TestClass.class").getAbsoluteFile();
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
            public void generate(Generator runData) {
                runData.loadLocals(this);
                runData.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc);
                if (!methodTypeDesc.returnType().equals(methodTypeDesc.parameterType(0))) {
                    runData.checkcast(methodTypeDesc.returnType());
                }
                runData.astore(runData.getLocalIndex());
                runData.next();
            }
        });
    }

    public Streamable<T> filter(TestOther.StreamablePredicate<T> predicate) {
        return add(new PipelineStep(predicate) {
            @Override
            public void generate(Generator runData) {
                runData.loadLocals(this);
                runData.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc)
                        .ifeq(runData.getEndLabel());
                runData.next();
            }
        });
    }

    public Streamable<T> peek(TestOther.StreamableConsumer<T> consumer) {
        return add(new PipelineStep(consumer) {
            @Override
            public void generate(Generator runData) {
                runData.loadLocals(this);
                runData.invoke(this);
                // runData.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc);
                runData.next();
            }
        });
    }

    public <R> Streamable<R> flatMap(TestOther.StreamableFunction<T, Iterable<R>> mapper) {
        return add(new PipelineStep(mapper) {
            @Override
            public void generate(Generator runData) {
                runData.loadLocals(this);
                runData.invokestatic(runData.clazz, methodModel.methodName().stringValue(), methodTypeDesc);
                Label startLoop = runData.newLabel();
                Label endLoop = runData.newLabel();
                runData.invokeinterface(ClassDesc.of("java.lang", "Iterable"), "iterator", MethodTypeDesc.of(ClassDesc.of("java.util", "Iterator")))
                        .astore(runData.getLocalIndex() + 1)
                        .labelBinding(startLoop)
                        .aload(runData.getLocalIndex() + 1)
                        .invokeinterface(ClassDesc.of("java.util", "Iterator"), "hasNext", MethodTypeDesc.of(CD_boolean))
                        .ifeq(endLoop)
                        .invokeinterface(ClassDesc.of("java.util", "Iterator"), "next", MethodTypeDesc.of(CD_Object))
                        .astore(runData.getLocalIndex() + 2);
                runData.next(runData.getLocalIndex() + 2, startLoop);
                runData.pop().goto_(startLoop)
                        .labelBinding(endLoop);
            }
        });
    }

    public static void main(String[] args) {
        int i = 0;

        new Streamable<Integer>()
                .peek(input -> System.out.println(Arrays.toString(args) + " " + input))
                .peek(System.out::println)
                .map(input -> input * 2)
                // .filter(input -> input > 2)
                .peek(Streamable::consumer)
                //.peek(input -> System.out.println(Arrays.toString(args) + " " + input))
                //.peek(input -> System.out.println(Arrays.toString(args) + " " + input))
                //.peek(input -> System.out.println(i))
                //.peek(System.out::println)
                //.filter(input -> input > 0)
                //.map(input -> input * 2)
                //.flatMap(input -> List.of(input, input + 1, input + 2, input + 3, input + 4))
                //.peek(System.out::println)
                .run();
    }

    private static void consumer(int value) {
        System.out.println(value);
    }
}
