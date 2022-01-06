package com.jk.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.jk.plugin.visitor.InjectClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AsmTransform : Transform() {
    override fun getName(): String = "JKTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean = false

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation?.apply {
            outputProvider.deleteAll()

            inputs.forEach {
                val directoryInputs = it.directoryInputs
                directoryInputs.forEach { input ->
                    val dirName = input.name
                    val src = input.file
                    println("目录：${src.absolutePath}")
                    val md5Name = DigestUtils.md5Hex(src.absolutePath)
                    val dest = outputProvider.getContentLocation(dirName + md5Name, input.contentTypes, input.scopes, Format.DIRECTORY)
                    processInject(src, dest)
                }
            }
           inputs.forEach { input ->
                input.jarInputs.forEach { jarInput ->
                    if (jarInput.file.exists()) {
                        val srcFile = jarInput.file

                        // 必须给jar重新命名，否则会冲突
                        var jarName = jarInput.name
                        val md5 = DigestUtils.md5Hex(jarInput.file.absolutePath)
                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length - 4)
                        }
                        val dest = outputProvider?.getContentLocation(md5 + jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        FileUtils.copyFile(srcFile, dest)
                    }
                }
            }
        }
    }

    private fun processInject(src: File, dest: File) {
        val dir = src.absolutePath
        val allFiles = FileUtils.getAllFiles(src)
        allFiles.forEach { file ->
            println(file.absoluteFile)
            val fis = FileInputStream(file)
            val byteArray = if (shouldModify(file.absolutePath)) {
                val classReader = ClassReader(fis)
                val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
                classReader.accept(InjectClassVisitor(Opcodes.ASM7, classWriter), ClassReader.EXPAND_FRAMES)
                classWriter.toByteArray()
            } else {
                fis.readAllBytes()
            }

            val absolutePath = file.absolutePath
            val fullClassPath = absolutePath.replace(dir, "")
            val outFile = File(dest, fullClassPath)
            FileUtils.mkdirs(outFile.parentFile)
            val fos = FileOutputStream(outFile)
            fos.write(byteArray)
            fos.close()
            fis.close()
        }
    }

    private fun shouldModify(filePath: String): Boolean {
        return filePath.endsWith(".class")
                && !filePath.contains("R.class")
                && !filePath.contains("$")
                && !filePath.contains("R$")
                && !filePath.contains("BuildConfig.class")
    }
}