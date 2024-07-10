package com.au.jsonapt

import com.au.jsonannotations.CompiledJsonBean
import com.au.jsonannotations.CompiledJsonField
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

class JsonProcessor : AbstractProcessor() {
    companion object {
        var processingEnv: ProcessingEnvironment? = null
        fun log(s:String) {
            processingEnv?.messager?.printMessage(Diagnostic.Kind.WARNING, "JsonProcessor: " + s)
        }
    }

    override fun init(pe: ProcessingEnvironment?) {
        super.init(pe)
        this.processingEnv = pe
        log("init....")
    }

    /**
     * 所支持的注解合集
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            CompiledJsonBean::class.java.canonicalName,
            CompiledJsonField::class.java.canonicalName)
    }

    private fun isElementInAnnotations(target: Element, annotations: Set<TypeElement>) : Boolean {
        for (annotation in annotations) {
            //匹配注释
            if (target == annotation) {
                return true
            }
        }
        return false
    }

    //Element代表程序中的包名、类、方法。即注解所支持的作用类型。
    fun getMyElements(annotations: Set<TypeElement>, elements: Set<Element?>): Set<TypeElement> {
        val result: MutableSet<TypeElement> = HashSet()
        //遍历包含的 package class method
        for (element in elements) {
            //匹配 class or interface
            if (element is TypeElement) {
                log("element type ${element.qualifiedName}")
                for (annotationMirror in element.annotationMirrors) {
                    val found = isElementInAnnotations(annotationMirror.annotationType.asElement(), annotations)
                    if (found) {
                        result.add(element)
                        break
                    }
                }
            } else if (element is VariableElement) {
                log("element variable ${element.simpleName}")
            }
        }
        return result
    }

    /**
     * @param annotations 需要处理的注解 即getSupportedAnnotationTypes被系统解析得到的注解
     * @param roundEnv 注解处理器所需的环境，帮助进行解析注解。
     */
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        log("process")
        val elements = roundEnv?.rootElements?.let {
            if (annotations != null) {
                getMyElements(annotations, it)
            } else {
                null
            }
        }

        return true
    }

    //一定要修改这里，避免无法生效
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}
