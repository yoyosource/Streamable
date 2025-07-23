package de.yoyosource.streamable2;

import de.yoyosource.TestOther;
import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class PipelineStep {
    final List<Object> capturedArguments = new ArrayList<>();
    final List<ClassDesc> capturedArgumentTypes = new ArrayList<>();
    protected final MethodModel methodModel;
    protected final MethodTypeDesc methodTypeDesc;

    protected PipelineStep(TestOther.StreamableLambda step) {
        try {
            Method method = step.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(step);

            InputStream inputStream = TestOther.class.getResourceAsStream("/" + lambda.getImplClass() + ".class");
            ClassModel model = ClassFile.of().parse(inputStream.readAllBytes());
            methodModel = model.elementStream().filter(MethodModel.class::isInstance)
                    .map(MethodModel.class::cast)
                    .filter(mm -> mm.methodName().equalsString(lambda.getImplMethodName()))
                    .findFirst()
                    .orElse(null);

            ClassDesc[] classDescs = methodModel.methodTypeSymbol().parameterArray();
            for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
                capturedArguments.add(lambda.getCapturedArg(i));
                capturedArgumentTypes.add(classDescs[i]);
            }

            this.methodTypeDesc = methodModel.methodTypeSymbol();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    public static class GenerateRunData {
        public final ClassDesc clazz;
        public final CodeBuilder codeBuilder;
        public int localIndex;
        public final Label end;
        Runnable continuation;

        public void _continue() {
            continuation.run();
        }

        public void generateSlotData(PipelineStep pipelineStep, boolean dupIfPossible) {
            if (dupIfPossible) {
                if (pipelineStep.capturedArgumentTypes.isEmpty()) {
                    codeBuilder.dup();
                } else {
                    codeBuilder.astore(localIndex);
                }
            } else {
                codeBuilder.astore(localIndex);
            }
            for (ClassDesc classDesc : pipelineStep.capturedArgumentTypes) {
                TypeKind typeKind = TypeKind.from(classDesc);
                codeBuilder.loadLocal(typeKind, localIndex);
                localIndex += typeKind.slotSize();
            }
            if (!dupIfPossible || !pipelineStep.capturedArgumentTypes.isEmpty()) {
                codeBuilder.aload(localIndex);
            }
        }
    }

    public abstract void generateRun(GenerateRunData runData);
}
