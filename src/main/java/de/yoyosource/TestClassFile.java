package de.yoyosource;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.util.Iterator;

import static java.lang.constant.ConstantDescs.*;

public class TestClassFile {

    @SuppressWarnings("preview")
    public static void main(String[] args) throws Throwable {
        // ClassModel classModel = ClassFile.of().parse(Path.of("/Users/jojo/IdeaProjects/Streamable/build/classes/java/main/de/yoyosource/streamable/Streamable.class"));
        // classModel.elements().forEach(System.out::println);

        // PrintStream, Iterator (dup)
        // PrintStream, Iterator, Iterator (hasNext)
        // PrintStream, Iterator, boolean (jump)
        // PrintStream, Iterator (dup2)
        // PrintStream, Iterator, PrintStream, Iterator (next)
        // PrintStream, Iterator, PrintStream, Object
        // PrintStream, Iterator

        // Iterator, boolean, PrintStream, Object

        byte[] bytes = ClassFile.of().build(ClassDesc.of("de.yoyosource", "TestClass"), classBuilder -> {
            classBuilder.withFlags(AccessFlag.PUBLIC)
                    .withVersion(67, 0)
                    .withMethod("run", MethodTypeDesc.of(CD_void, ClassDesc.of("java.lang", "Iterable")), AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(), methodBuilder -> {
                        methodBuilder.withCode(codeBuilder -> {
                            Label loopStart = codeBuilder.newLabel();
                            Label loopEnd = codeBuilder.newLabel();
                            codeBuilder.getstatic(ClassDesc.of("java.lang", "System"), "out", ClassDesc.of("java.io", "PrintStream"))
                                    .dup()
                                    .ldc(codeBuilder.constantPool().stringEntry("Started"))
                                    .invokevirtual(ClassDesc.of("java.io", "PrintStream"), "println", MethodTypeDesc.of(CD_void, ClassDesc.of("java.lang", "Object")))
                                    .aload(0)
                                    .invokeinterface(ClassDesc.of("java.lang", "Iterable"), "iterator", MethodTypeDesc.of(ClassDesc.of("java.util", "Iterator")))
                                    .labelBinding(loopStart)
                                    .dup()
                                    .invokeinterface(ClassDesc.of("java.util", "Iterator"), "hasNext", MethodTypeDesc.of(CD_boolean))
                                    .ifeq(loopEnd)
                                    .dup()
                                    .invokeinterface(ClassDesc.of("java.util", "Iterator"), "next", MethodTypeDesc.of(CD_Object))
                                    // .invokevirtual(ClassDesc.of("java.io", "PrintStream"), "println", MethodTypeDesc.of(CD_void, ClassDesc.of("java.lang", "Object")))
                                    .pop()
                                    .goto_(loopStart)
                                    .labelBinding(loopEnd)
                                    .pop()
                                    .ldc(codeBuilder.constantPool().stringEntry("Finished"))
                                    .invokevirtual(ClassDesc.of("java.io", "PrintStream"), "println", MethodTypeDesc.of(CD_void, ClassDesc.of("java.lang", "Object")))
                                    .return_();
                        });
                    });
        });

        File file = new File("/Users/jojo/IdeaProjects/Streamable/TestClass.class");
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        fileOutputStream.close();

        SimpleClassLoader simpleClassLoader = new SimpleClassLoader();
        Class<?> clazz = simpleClassLoader.loadClass(bytes);
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            MethodHandle methodHandle = MethodHandles.lookup().unreflect(declaredMethod);
            methodHandle.invoke((Iterable<Long>) () -> new Iterator<>() {
                long count = 0;

                @Override
                public boolean hasNext() {
                    return count < 1_000_000_000L;
                }

                @Override
                public Long next() {
                    return count++;
                }
            });
        }
    }

    private static class SimpleClassLoader extends ClassLoader {
        public Class<?> loadClass(byte[] bytes) {
            return defineClass(bytes, 0, bytes.length);
        }
    }
}
