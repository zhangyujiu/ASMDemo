package com.jk.plugin.visitor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method

class InjectMethodVisitor(api: Int, methodVisitor: MethodVisitor, access: Int, name: String?, private val descriptor: String?) :
    AdviceAdapter(api, methodVisitor, access, name, descriptor) {

    var flag = false
    var startTime = -1
    override fun onMethodEnter() {
        super.onMethodEnter()
        println("name:$name    descriptor:$descriptor")
        if (!flag) return
        //    INVOKESTATIC java/lang/System.currentTimeMillis ()J
        //    LSTORE 1
        invokeStatic(Type.getType("Ljava/lang/System;"), Method("currentTimeMillis", "()J"))
        startTime = newLocal(Type.LONG_TYPE)
        storeLocal(startTime)
    }

    var endTime = -1
    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (!flag) return
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
        visitLdcInsn("method '$name' total duration\uff1a")

        //    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
        invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"))
        //    LLOAD 3
        loadLocal(endTime)
        //    LLOAD 1
        loadLocal(startTime)
        //    LSUB
        math(SUB, Type.LONG_TYPE)
        //    INVOKEVIRTUAL java/lang/StringBuilder.append (J)Ljava/lang/StringBuilder;
        invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), Method("append", "(J)Ljava/lang/StringBuilder;"))
        //    INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
        invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), Method("toString", "()Ljava/lang/String;"))
        //    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
        invokeVirtual(Type.getType("Ljava/io/PrintStream;"), Method("println", "(Ljava/lang/String;)V"))
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        flag = descriptor?.contains("Lcom/jk/asmdemo/AsmInject;") == true
        return super.visitAnnotation(descriptor, visible)
    }
}