package com.au.jsonksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class JsonKspProvider : SymbolProcessorProvider {
    companion object {
        lateinit var env: SymbolProcessorEnvironment
        fun log(s:String) {
            env.logger.warn("[CompiledJson]: $s")
        }
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        env = environment
        log("create...")
        return KspSymbolProcessor()
    }
}
