package com.au.jsonksp

import com.au.jsonksp.infos.ClassTree

/**
 * @author allan
 * @date :2024/7/11 9:46
 * @description:
 */
class ClassTreeManager {
    private val alreadyExportTopClassList:ArrayList<String> = ArrayList()
    fun addAlreadyExportedTopClass(exported:String) {
        alreadyExportTopClassList.add(exported)
    }

    fun findClassTree() : ClassTree? {
        val topClass = findTopClass() ?: return null
        return findAClassTree(topClass)
    }

    private fun findAClassTree(topClass:String) : ClassTree {
        val classTree = ClassTree(topClass, ArrayList())

        for (kv in Globals.classAndParentMap) {
            if (kv.value == topClass) {
                classTree.innerClasses.add(findAClassTree(kv.key))
            }
        }

        return classTree
    }

    private fun findTopClass() : String? {
        for (kv in Globals.classAndParentMap) {
            if (kv.value.isNullOrEmpty()) {
                if (!alreadyExportTopClassList.contains(kv.key)) {
                    return kv.key
                }
            }
        }
        return null
    }
}