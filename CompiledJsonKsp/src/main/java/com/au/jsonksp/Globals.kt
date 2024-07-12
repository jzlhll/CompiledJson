package com.au.jsonksp

import com.au.jsonksp.JsonKspProvider.Companion.log
import com.au.jsonksp.codes.BeanFromJSONObjectGenerator
import com.au.jsonksp.codes.BeanToJSONObjectGenerator
import com.au.jsonksp.codes.InnerTemplateCode
import com.au.jsonksp.codes.TopTemplateCode
import com.au.jsonksp.infos.ClassTree
import com.au.jsonksp.infos.FieldInfo
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    /**
     * 存储 classQualifiedName 和 它的fields的信息
     * 比如com.au.compiledjson.bean.JavaPersonBean和它的所有属性。
     */
    val class2FieldInfos:HashMap<String, ArrayList<FieldInfo>> = HashMap()

    /**
     * classQualifiedName 一个类的外层类。key是类，value是它的外层类
     */
    val classAndParentMap : HashMap<String, String?> = HashMap()

    val classAndIsJavaMap : HashMap<String, Boolean> = HashMap()

    private val classTreeManager = ClassTreeManager()

//    @Synchronized
//    private fun fix() {
//        if(true) return //no need check
//        if (class2FieldInfos.size <= 1) return
//
//        class2FieldInfos.forEach { (_, fieldInfos) ->
//            fieldInfos.forEach { fieldInfo->
//                val type = fieldInfo.type
//                if (!type.checked) { //需要检测是否存在这种类型
//                    if (type is ArrayFieldInfoGetType) {
//                        if (class2FieldInfos.containsKey(type.genericClass)) {
//                            type.checked = true
//                        }
//                    } else if (type is ObjectFieldInfoGetType) {
//                        if (class2FieldInfos.containsKey(type.clazz)) {
//                            type.checked = true
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun oneClassExport(clazz:String, subs:List<ClassTree>, isInner:Boolean) : String {
        log("onClass Export $clazz ${subs.size}")

        val index = clazz.lastIndexOf('.')
        val name = clazz.substring(index + 1)
        val code = if(isInner) InnerTemplateCode() else TopTemplateCode().also { it.insertPackage(clazz.substring(0, index)) }
        code.insertBeanSimpleName(name)
        val generator1 = BeanFromJSONObjectGenerator(clazz)
        val generator2 = BeanToJSONObjectGenerator(clazz)
        val sb = StringBuilder()
        sb.append(generator1.generate()).appendLine().append(generator2.generate()).appendLine()
        for (sub in subs) {
            sb.append(oneClassExport(sub.qualifiedName, sub.innerClasses, true)).appendLine()
        }
        code.insertBody(sb.toString())
        return code.endCode
    }

    private fun classTreeExport(classTree: ClassTree) {
        val code = oneClassExport(classTree.qualifiedName, classTree.innerClasses, false)
        val index = classTree.qualifiedName.lastIndexOf('.')
        val name = classTree.qualifiedName.substring(index + 1) + "Compiled"
        val pkg = classTree.qualifiedName.substring(0, index)
        writeToFile(pkg, name, code)

        classTreeManager.addAlreadyExportedTopClass(classTree.qualifiedName)
    }

    @Synchronized
    fun addField(classQualifiedName:String, fi: FieldInfo) {
        val fis = class2FieldInfos[classQualifiedName]
        if (fis.isNullOrEmpty()) {
            class2FieldInfos[classQualifiedName] = ArrayList<FieldInfo>().also {
                it.add(fi)
            }
        } else {
            fis.add(fi)
        }
    }

    @Synchronized
    fun export() {
//        fix()
        while (true) {
            val classTree = classTreeManager.findClassTree() ?: break
            classTreeExport(classTree)
        }
    }

    private val atomicInteger = AtomicInteger(0)
    fun nextRandomIndex() : String {
        return String.format("%d", atomicInteger.addAndGet(1))
    }
}