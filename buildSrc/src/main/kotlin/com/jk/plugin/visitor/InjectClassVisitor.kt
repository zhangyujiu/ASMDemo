package com.jk.plugin.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor

class InjectClassVisitor (api: Int, classWriter: ClassWriter) : ClassVisitor(api, classWriter) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        return InjectMethodVisitor(api, visitMethod, access, name, descriptor)
    }
}