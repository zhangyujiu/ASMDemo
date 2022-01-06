package com.jk.asmdemo

import org.junit.Test

import org.junit.Assert.*
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method
import java.io.FileInputStream
import java.io.FileOutputStream

class AsmUnitTest {
    @Test
    fun inject() {
        val fis = FileInputStream("F:\\project\\ASMDemo\\app\\src\\test\\java\\com\\jk\\asmdemo\\Demo.class")

        val classReader = ClassReader(fis)

        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        classReader.accept(MyClassVisitor(Opcodes.ASM7, classWriter), ClassReader.EXPAND_FRAMES)

        val byteArray = classWriter.toByteArray()
        val fos = FileOutputStream("F:\\project\\ASMDemo\\app\\src\\test\\java\\com\\jk\\asmdemo\\Demo.class")
        fos.write(byteArray)

        fos.close()
        fis.close()
    }

    class MyClassVisitor(api: Int, classWriter: ClassWriter) : ClassVisitor(api, classWriter) {

        override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            return MyMethodVisitor(api, visitMethod, access, name, descriptor)
        }
    }

    class MyMethodVisitor(api: Int, methodVisitor: MethodVisitor, access: Int, name: String?, descriptor: String?) :
        AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        var startTime = -1
        override fun onMethodEnter() {
            super.onMethodEnter()
            if (name != "main") return
//            INVOKESTATIC java/lang/System.currentTimeMillis ()J
//            LSTORE 1
            invokeStatic(Type.getType("Ljava/lang/System;"), Method("currentTimeMillis", "()J"))
            startTime = newLocal(Type.LONG_TYPE)
            storeLocal(startTime)
        }

        var endTime = -1
        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            if (name != "main") return
            invokeStatic(Type.getType("Ljava/lang/System;"), Method("currentTimeMillis", "()J"))
            endTime = newLocal(Type.LONG_TYPE)
            storeLocal(endTime)

            //    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
            getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"))
            //    NEW java/lang/StringBuilder
            newInstance(Type.getType("Ljava/lang/StringBuilder;"))
            //    DUP
            dup()
            //    INVOKESPECIAL java/lang/StringBuilder.<init> ()V
            invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"), Method("<init>", "()V"))
            //    LDC "total time\uff1a"
            visitLdcInsn("total time\uff1a")

            //    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"))
            //    LLOAD 3
            loadLocal(startTime)
            //    LLOAD 1
            loadLocal(endTime)
            //    LSUB
            math(SUB, Type.LONG_TYPE)
            //    INVOKEVIRTUAL java/lang/StringBuilder.append (J)Ljava/lang/StringBuilder;
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), Method("append", "(J)Ljava/lang/StringBuilder;"))
            //    INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), Method("toString", "()Ljava/lang/String;"))
            //    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
            invokeVirtual(Type.getType("Ljava/io/PrintStream;"), Method("println", "(Ljava/lang/String;)V"))
        }

    }
}