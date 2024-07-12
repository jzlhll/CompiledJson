package com.au.jsonksp

import com.au.jsonannotations.CompiledJsonBean
import com.au.jsonksp.JsonKspProvider.Companion.log
import com.au.jsonksp.infos.FieldInfo
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

/**
 * creator: lt  2022/10/20  lt.dygzs@qq.com
 * effect : ksp处理程序
 * warning:
 */
class KspSymbolProcessor() : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        log("process start....")
        val ret = mutableListOf<KSAnnotated>()

        do {
            val symbols = resolver.getSymbolsWithAnnotation(CompiledJsonBean::class.java.canonicalName)
            symbols.toList().forEach { symbol->
                if (!symbol.validate())
                    ret.add(symbol)
                else {
                    if (symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS) {
                        processKClass(symbol)
                    } else {
                        ret.add(symbol)
                    }
                }
            }
        } while(false)

        log("process collection classes over...")

        Globals.export()

        log("process end.")
        //返回无法处理的符号
        return ret
    }

    //解析类
    private fun processKClass(classDeclar:KSClassDeclaration) {
        val qualifiedClassName = classDeclar.qualifiedName?.asString()!!
        val simpleName = classDeclar.simpleName.asString()
        val lowerSimpleName = simpleName.lowercase()
        log("\n\n----processKClass..start..$qualifiedClassName($simpleName) ---")
        if (lowerSimpleName == simpleName) {
            throw RuntimeException("Please change your class $qualifiedClassName's name to CamelCase!")
        }

        val constructors = classDeclar.getConstructors()
        val constructorFieldNames = ArrayList<String>()
        for (constructor in constructors) { //warning: 只管第一个构造函数。
            val parameters = constructor.parameters
            if (parameters.isNotEmpty()) {
                for (parameter in parameters) {
                    constructorFieldNames.add(parameter.name?.asString()!!)
                }
            }
        }

        val pkgAndParent = qualifiedNameToPkgAndParent(qualifiedClassName)
        log("pkgAndParent $pkgAndParent, fieldNames $constructorFieldNames")
        Globals.classAndParentMap[qualifiedClassName] = pkgAndParent.second

        classDeclar.declarations.forEach { declaration ->
            if (declaration is KSPropertyDeclaration) {
                processPropertyDeclaration(classDeclar, declaration, constructorFieldNames)
            }
        }
    }

    private fun isFieldTransientOrStatic(propertyDeclaration: KSPropertyDeclaration) : Boolean {
        //java的忽略字段
        if (propertyDeclaration.modifiers.contains(Modifier.JAVA_TRANSIENT)
            || propertyDeclaration.modifiers.contains(Modifier.JAVA_STATIC)
            || propertyDeclaration.modifiers.contains(Modifier.FINAL)
            || propertyDeclaration.modifiers.contains(Modifier.CONST)) return true

        propertyDeclaration.annotations.forEach { annotation ->
            val shortName = annotation.shortName.getShortName()
            if (shortName == "Transient") {
                return true
            }
        }
        return false
    }

    //解析一个字段
    private fun processPropertyDeclaration(classDeclar:KSClassDeclaration,
                                           propertyDeclaration: KSPropertyDeclaration,
                                           constructorFieldNames:List<String>) {
        val classQualifiedName = classDeclar.qualifiedName?.asString()!!
        val log = "{" + classQualifiedName + " " + propertyDeclaration.simpleName.asString() + "}"

        val isFieldTransient = isFieldTransientOrStatic(propertyDeclaration)
        if (isFieldTransient) {
            log("$log isFieldTransient or static or const ignored.")
            return
        }

        val hasAnyPriv = Modifier.PRIVATE in propertyDeclaration.modifiers
                || Modifier.PROTECTED in propertyDeclaration.modifiers
                || Modifier.INTERNAL in propertyDeclaration.modifiers
        val isPublic = Modifier.PUBLIC in propertyDeclaration.modifiers || !(hasAnyPriv)
        if (!isPublic) {
            throw RuntimeException("$log is not public! Please change it to >public< .")
        }

        //todo 根据是否是公开的字段还需要补set get函数的获取
        log("$log, type: ${propertyDeclaration.type}")

        val fieldName = propertyDeclaration.simpleName.asString()
        val isInConstructor = constructorFieldNames.indexOf(fieldName)
        val type = propertyDeclaration.type.toCompiledJsonType(log)
        var altName:String? = null
        var deserialize = true
        var serialize = true

        val annotation = propertyDeclaration.annotations.firstOrNull{ it.shortName.asString() == "CompiledJsonField" }
        annotation?.arguments?.forEach { arg->
            val n = arg.name?.asString()
            val v = arg.value
            when (n) {
                "altName" -> {
                    altName = v.toString()
                }
                "deserialize" -> {
                    deserialize = v.toString().toBoolean()
                }
                "serialize" -> {
                    serialize = v.toString().toBoolean()
                }
            }
        }
        //得到了一个class的信息。添加进入数组并进行修正
        val fi = FieldInfo(fieldName, type, isInConstructor, altName=altName, serialize = serialize, deserialize = deserialize)
        Globals.addField(classQualifiedName, fi)
    }
}