//package com.au.jsonksp.infos;
//
///**
// * @author allan
// * @date :2024/7/8 14:01
// * @description:
// */
//public class CompiledJsonHelper {
//    private static final String[] REPLACEMENT_CHARS;
//    private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
//
//    static {
//        REPLACEMENT_CHARS = new String[128];
//        for (int i = 0; i <= 0x1f; i++) {
//            REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
//        }
//        REPLACEMENT_CHARS['"'] = "\\\"";
//        REPLACEMENT_CHARS['\\'] = "\\\\";
//        REPLACEMENT_CHARS['\t'] = "\\t";
//        REPLACEMENT_CHARS['\b'] = "\\b";
//        REPLACEMENT_CHARS['\n'] = "\\n";
//        REPLACEMENT_CHARS['\r'] = "\\r";
//        REPLACEMENT_CHARS['\f'] = "\\f";
//        HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
//        HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
//        HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
//        HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
//        HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
//        HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
//    }
//
////    public static void replacement(String value, boolean htmlSafe) {
////        String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
////        out.write('\"');
////        StringBuilder sb = new StringBuilder();
////
////        int last = 0;
////        int length = value.length();
////        for (int i = 0; i < length; i++) {
////            char c = value.charAt(i);
////            String replacement;
////            if (c < 128) {
////                replacement = replacements[c];
////                if (replacement == null) {
////                    continue;
////                }
////            } else if (c == '\u2028') {
////                replacement = "\\u2028";
////            } else if (c == '\u2029') {
////                replacement = "\\u2029";
////            } else {
////                continue;
////            }
////            if (last < i) {
////                out.write(value, last, i - last);
////            }
////            out.write(replacement);
////            last = i + 1;
////        }
////        if (last < length) {
////            out.write(value, last, length - last);
////        }
////        out.write('\"');
////    }
//}
