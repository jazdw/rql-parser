package net.jazdw.rql.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class DefaultTextDecoder implements TextDecoder {
    @Override
    public String apply(String s) {
        // URLDecoder converts plus to space, percent encode the plus signs first
        return URLDecoder.decode(s.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
