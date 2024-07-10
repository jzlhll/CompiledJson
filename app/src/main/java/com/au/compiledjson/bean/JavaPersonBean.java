package com.au.compiledjson.bean;

import com.au.jsonannotations.CompiledJsonBean;
import com.au.jsonannotations.CompiledJsonField;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Transient;

@CompiledJsonBean
public class JavaPersonBean {
    public String name;

    public List<SubBean> infoList;

    public SubBean[] infos;

    @CompiledJsonField(altName = "info")
    public SubBean inf;

    public List<Integer> numberList;
    public int[] numberArray;

    @CompiledJsonField(altName = "age")
    public int mAg;
    @CompiledJsonField(serialize = false)
    public boolean sex;

    public transient boolean isSelected;

    public String extra;

    @CompiledJsonBean
    public static final class SubBean {
        public String address;
        public String custom;

        public static final class ThirdBean {
            public int ex;
        }
    }
}
