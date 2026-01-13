package com.cakesuite.api.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

public class IdGenerator {

  private static final char[] DIGIT = "0123456789".toCharArray();
  private static final int ORDER_NO_SIZE = 9;
  private static final DateTimeFormatter ORDER_NO_PREFIX_FORMAT = DateTimeFormatter.ofPattern("yy", Locale.ENGLISH);
  private static final SecureRandom RANDOM = new SecureRandom();

  
  public static String generateOrderNo() {
      String prefix = LocalDate.now().format(ORDER_NO_PREFIX_FORMAT).toUpperCase(Locale.ENGLISH);
      String randomId = NanoIdUtils.randomNanoId(RANDOM, DIGIT, ORDER_NO_SIZE);
      return prefix + randomId;
  }
}
