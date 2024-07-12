package com.au.compiledjson.bean

import com.au.jsonannotations.CompiledJsonField
import com.au.jsonannotations.CompiledJsonKtBean

@CompiledJsonKtBean
class PersonBean(
    var name:String?,
    var info:String?,
    @CompiledJsonField(serialize = false) var sex:Boolean?,
    @CompiledJsonField(altName = "age") var mAge:Int?,
    @Transient
    var isSelected: Boolean = false) {

    @CompiledJsonKtBean
    class PersonKtSubBean(var ex:String, val n:Int)

    companion object {
        val sIndex = 10
    }
}