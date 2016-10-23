package com.github.yangweigbh;

import com.github.yangweigbh.utils.Pair;

import java.util.Map;

/**
 * Created by yangwei on 2016/10/23.
 */
public interface Strategy {
    Pair<Float, String> apply(Map<String, Map<String, Float>> prefs, String user1, String user2);
}
