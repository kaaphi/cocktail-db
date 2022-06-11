package com.kaaphi.cocktails.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fractions {
  private static final Pattern FRACTION = Pattern.compile("((\\d+)\\s*)?((\\d+)/(\\d+))?");

  public static String[] toStringParts(int... f) {
    if(f[0] == 0) {
      return new String[0];
    }
    
    int wholeNumberPart = f[0]/f[1];
    int remainder = f[0] % f[1];

    if(wholeNumberPart > 0) {
      if(remainder > 0)
        return new String[] {Integer.toString(wholeNumberPart), Integer.toString(remainder), Integer.toString(f[1])};
      else
        return new String[] {Integer.toString(wholeNumberPart)};
    } else {
      return new String[] {Integer.toString(f[0]), Integer.toString(f[1])};
    }
  }

  public static String toString(int... frac) {
    String[] parts = toStringParts(frac);

    StringBuilder sb = new StringBuilder();

    int i = 0;
    
    if(parts.length == 0) {
      return "";
    }

    if(parts.length != 2) {
      sb.append(parts[i++]);
    }

    if(parts.length == 3) {
      sb.append(" ");
    }

    if(parts.length != 1) {
      sb.append(parts[i++]).append("/").append(parts[i]);
    }

    return sb.toString();
  }

  public static int[] toIntArray(String frac) {
    int[] parts = new int[2];
    
    if(frac.trim().isEmpty()) {
      return parts;
    }

    Matcher m = FRACTION.matcher(frac);
    if(m.matches()) {
      String whole = m.group(2);
      String n = m.group(4);
      String d = m.group(5);


      if(whole != null) {
        parts[0] = Integer.parseInt(whole);
      } else {
        parts[0] = 0;
      }

      if(m.group(3) != null) {
        parts[0] = (parts[0] * Integer.parseInt(d)) + Integer.parseInt(n);
        parts[1] = Integer.parseInt(d);
      } else {
        parts[1] = 1;
      }

      return parts;
    } else {
      throw new IllegalArgumentException(frac);
    }
  }

  public static void main(String[] args) {
    System.out.format("<%s>%n", Arrays.toString(toIntArray("1/8")));
  }
}
