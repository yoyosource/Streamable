package de.yoyosource.streamable3;

import de.yoyosource.TestOther;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class PipelineStep {

    final List<Object> capturedArguments = new ArrayList<>();
    final List<ClassDesc> capturedArgumentTypes = new ArrayList<>();
    protected SerializedLambda lambda;
    protected MethodModel methodModel;
    protected MethodTypeDesc methodTypeDesc;

    protected PipelineStep(TestOther.StreamableLambda step) {
        try {
            Method method = step.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            lambda = (SerializedLambda) method.invoke(step);

            InputStream inputStream = TestOther.class.getResourceAsStream("/" + lambda.getImplClass() + ".class");
            ClassModel model = ClassFile.of().parse(inputStream.readAllBytes());
            methodModel = model.elementStream().filter(MethodModel.class::isInstance)
                    .map(MethodModel.class::cast)
                    .filter(mm -> mm.methodName().equalsString(lambda.getImplMethodName()))
                    .findFirst()
                    .orElse(null);

            ClassDesc[] classDescs = methodModel.methodTypeSymbol().parameterArray();
            for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
                if (i >= classDescs.length) {
                    capturedArguments.add(lambda.getCapturedArg(i));
                    capturedArgumentTypes.add(lambda.getCapturedArg(i).getClass().describeConstable().orElseThrow());
                } else {
                    capturedArguments.add(lambda.getCapturedArg(i));
                    capturedArgumentTypes.add(classDescs[i]);
                }
            }

            this.methodTypeDesc = methodModel.methodTypeSymbol();

            if (!lambda.getImplMethodName().contains("$")) {
                methodModel = null;
                methodTypeDesc = null;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    public static class Generator {

        public final ClassDesc clazz;

        @Delegate
        private final CodeBuilder codeBuilder;
        private final List<Integer> localIndices = new ArrayList<>();
        private final List<Label> endLabels = new ArrayList<>();
        public final Consumer<Generator> next;
        private final Map<Object, Integer> capturedArgumentSlots;

        public Label getEndLabel() {
            return endLabels.getLast();
        }

        public int getLocalIndex() {
            return localIndices.getLast();
        }

        public void next() {
            next.accept(this);
        }

        public void next(int localIndex, Label end) {
            localIndices.add(localIndex);
            endLabels.add(end);
            next.accept(this);
            endLabels.removeLast();
            localIndices.removeLast();
        }

        public void loadLocals(PipelineStep pipelineStep) {
            for (Object object : pipelineStep.capturedArguments) {
                aload(capturedArgumentSlots.get(object));
            }
            aload(getLocalIndex());
        }

        public void invoke(PipelineStep pipelineStep) {
            if (pipelineStep.lambda.getImplMethodName().contains("$")) {
                invokestatic(clazz, pipelineStep.methodModel.methodName().stringValue(), pipelineStep.methodTypeDesc);
            } else {
                loadLocals(pipelineStep);
                switch (pipelineStep.lambda.getImplMethodKind()) {
                    case MethodHandleInfo.REF_invokeStatic:
                        invokestatic(ClassDesc.ofInternalName(pipelineStep.lambda.getImplClass()), pipelineStep.lambda.getImplMethodName(), MethodTypeDesc.ofDescriptor(pipelineStep.lambda.getImplMethodSignature()));
                        break;
                    case MethodHandleInfo.REF_invokeInterface:
                        invokeinterface(ClassDesc.ofInternalName(pipelineStep.lambda.getImplClass()), pipelineStep.lambda.getImplMethodName(), MethodTypeDesc.ofDescriptor(pipelineStep.lambda.getImplMethodSignature()));
                        break;
                    case MethodHandleInfo.REF_invokeSpecial:
                        invokespecial(ClassDesc.ofInternalName(pipelineStep.lambda.getImplClass()), pipelineStep.lambda.getImplMethodName(), MethodTypeDesc.ofDescriptor(pipelineStep.lambda.getImplMethodSignature()));
                        break;
                    case MethodHandleInfo.REF_invokeVirtual:
                        invokevirtual(ClassDesc.ofInternalName(pipelineStep.lambda.getImplClass()), pipelineStep.lambda.getImplMethodName(), MethodTypeDesc.ofDescriptor(pipelineStep.lambda.getImplMethodSignature()));
                        break;
                    default:
                        throw new IllegalArgumentException("invalid impl method kind: " + pipelineStep.lambda.getImplMethodKind());
                }
            }
        }
    }

    public abstract void generate(Generator generator);
}
