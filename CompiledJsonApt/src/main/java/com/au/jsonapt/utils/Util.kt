package com.au.jsonapt.utils

import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

fun writeJavaClassFile(processingEnv: ProcessingEnvironment, code:String, classFullName:String) {
    processingEnv.filer?.let {
        try {
            // 创建一个JavaFileObject来表示要生成的文件
            val sourceFile: JavaFileObject = it.createSourceFile(classFullName, null)
            sourceFile.openWriter().use { writer ->
                // 写入Java（或Kotlin）代码
                writer.write(code)
                writer.flush()
            }
        } catch (e: IOException) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate $classFullName =>" + e.message)
        }
    }
}