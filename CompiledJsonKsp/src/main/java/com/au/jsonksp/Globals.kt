package com.au.jsonksp

import com.au.jsonksp.codes.BeanFromJSONObjectGenerator
import com.au.jsonksp.codes.BeanToJSONObjectGenerator
import com.au.jsonksp.codes.TemplateCode
import com.au.jsonksp.infos.ArrayFieldInfoGetType
import com.au.jsonksp.infos.FieldInfo
import com.au.jsonksp.infos.ObjectFieldInfoGetType
import java.util.concurrent.atomic.AtomicInteger

object Globals {
    /**
     * 存储 classQualifiedName 和 它的fields的信息
     * 比如com.au.compiledjson.bean.JavaPersonBean和它的所有属性。
     */
    private val class2FieldInfos:HashMap<String, ArrayList<FieldInfo>> = HashMap()

    /**
     * classQualifiedName 一个类的外层类。key是类，value是它的外层类
     */
    val classAndParentMap : HashMap<String, String?> = HashMap()

    private val alreadyExportClassList:ArrayList<String> = ArrayList()

    private fun fix() {
        if (class2FieldInfos.size <= 1) return

        //每次添加一个新的class进来。都进行修正
        class2FieldInfos.forEach { (_, fieldInfos) ->
            fieldInfos.forEach { fieldInfo->
                val type = fieldInfo.type
                if (!type.checked) { //需要检测是否存在这种类型
                    if (type is ArrayFieldInfoGetType) {
                        if (class2FieldInfos.containsKey(type.genericClass)) {
                            type.checked = true
                        }
                    } else if (type is ObjectFieldInfoGetType) {
                        if (class2FieldInfos.containsKey(type.clazz)) {
                            type.checked = true
                        }
                    }
                }
            }
        }
    }

    /**
     * 检测修正完成了某个类。即，某个类，已经全部被check过，可以进行代码生成了。
     */
    private fun export() {
        class2FieldInfos.forEach { (classQualifiedName, fieldInfos) ->
            if (xxx !alreadyExportClassList.contains(classQualifiedName)) {
                val hasUnchecked = fieldInfos.map { it.type.checked }.contains(false)
                if (!hasUnchecked) {
                    aClassExport(classQualifiedName, fieldInfos)
                }
            }
        }
    }

    private fun aClassExport(classQualifiedName:String, fieldInfos : List<FieldInfo>) {
        val index = classQualifiedName.lastIndexOf('.')
        val pkg = classQualifiedName.substring(0, index)
        val name = classQualifiedName.substring(index + 1) + "Compiled"
        val code = TemplateCode()
        code.insertPackage(pkg)
        code.insertBeanSimpleName(name)
        val generator1 = BeanFromJSONObjectGenerator(classQualifiedName)
        val generator2 = BeanToJSONObjectGenerator(classQualifiedName)

        code.insertBody(generator1.generate() + "\n" + generator2.generate())

        writeToFile(pkg, name, code.code)
        alreadyExportClassList.add(classQualifiedName)
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
    fun fixAndExport() {
        fix()
        export()
    }

    private val atomicInteger = AtomicInteger(0)
    fun nextRandomIndex() : String {
        return String.format("%d", atomicInteger.addAndGet(1))
    }
}