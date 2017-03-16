package com.edxavier.childgrowthstandards.db;

import java.util.UUID;

/**
 * Created by Eder Xavier Rojas on 20/09/2016.
 */
public class UUIDGenerator {
    public static String nextUUID() {
        return UUID.randomUUID().toString();
    }
}
