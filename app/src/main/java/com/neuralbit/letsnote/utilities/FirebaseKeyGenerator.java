package com.neuralbit.letsnote.utilities;

import org.apache.commons.lang3.RandomStringUtils;

public class FirebaseKeyGenerator {
    public static String generateKey(){
        return RandomStringUtils.randomAlphanumeric(20);
    }
}
