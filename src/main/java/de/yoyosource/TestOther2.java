package de.yoyosource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;

public class TestOther2 {

    public static void test(TestOther.StreamableFunction streamableFunction) {
        try {
            Method method = streamableFunction.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(streamableFunction);

            InputStream inputStream = TestOther.class.getResourceAsStream("/" + lambda.getImplClass() + ".class");
            ClassModel model = ClassFile.of().parse(inputStream.readAllBytes());
            MethodModel methodModel = model.elementStream().filter(MethodModel.class::isInstance)
                    .map(MethodModel.class::cast)
                    .filter(mm -> mm.methodName().equalsString(lambda.getImplMethodName()))
                    .findFirst()
                    .orElse(null);

            byte[] bytes = ClassFile.of().build(ClassDesc.of("de.yoyosource", "TestClass"), classBuilder -> {
                classBuilder.withFlags(AccessFlag.PUBLIC)
                        .withVersion(67, 0)
                        .withMethod(classBuilder.constantPool().utf8Entry("step$1"), methodModel.methodType(), AccessFlag.PRIVATE.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                            methodBuilder.transformCode(methodModel.code().get(), CodeTransform.ACCEPT_ALL);
                        });
            });

            File file = new File("/Users/jojo/IdeaProjects/Streamable/TestClass.class");
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.close();

            // Class<?> clazz = Class.forName(lambda.getImplClass().replace('/', '.'), false, Thread.currentThread().getContextClassLoader());
            // Method m = clazz.getDeclaredMethod(lambda.getImplMethodName());
            // System.out.println(": " + m);
            // System.out.println(lambda.getImplClass());
            // System.out.println(lambda.getImplMethodName());
            // System.out.println(lambda.getCapturedArgCount());
        } catch (Throwable e) {
            e.printStackTrace();
            // Ignore
        }

//
        // String s = run.getClass().getTypeName();
        // System.out.println(s);
        // int lastIndex = s.lastIndexOf("/");
        // if (lastIndex > -1) s = s.substring(0, s.lastIndexOf('/'));
        // s = s.replace('.', '/');
//
        // System.out.println(s);
        // InputStream inputStream = TestOther.class.getResourceAsStream("/" + s + ".class");
        // System.out.println(inputStream);
    }
}
