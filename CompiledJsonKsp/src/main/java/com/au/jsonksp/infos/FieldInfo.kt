package com.au.jsonksp.infos

import com.au.jsonannotations.JSONObjectGetType

data class FieldInfo(val fieldName:String,
                     val type: FieldInfoGetType,
                     val isInConstructor:Int = -1, //-1表示不是构造函数的参数；0~N表示第几个参数位置
                     val altName:String? = null,
                     val serialize:Boolean = true,
                     val deserialize:Boolean = true)

open class FieldInfoGetType(val getType: JSONObjectGetType) //, var checked:Boolean = true

/**
 * 带泛型的类型。基本上只支持List和Array
 */
class ArrayFieldInfoGetType(type: JSONObjectGetType, var genericClass:String, var arrayOrList:Boolean)
    : FieldInfoGetType(type) { //, false
    }

class KtIntArrayFieldInfoGetType(type: JSONObjectGetType)
    : FieldInfoGetType(type) { //, false
}
class KtBooleanArrayFieldInfoGetType(type: JSONObjectGetType)
    : FieldInfoGetType(type) { //, false
}
class KtDoubleArrayFieldInfoGetType(type: JSONObjectGetType)
    : FieldInfoGetType(type) { //, false
}
class KtLongArrayFieldInfoGetType(type: JSONObjectGetType)
    : FieldInfoGetType(type) { //, false
}

class ObjectFieldInfoGetType(type: JSONObjectGetType, var clazz:String)
    : FieldInfoGetType(type) //, false

//todo 现在强制要求public
//class GetFuncFieldInfoGetType(functionName:String, returnClass:String) : FieldInfoGetType(JSONObjectGetType.Function)
//class SetFuncFieldInfoGetType(functionName:String, setClass:String, genericClass: String? = null) : FieldInfoGetType(JSONObjectGetType.Function)