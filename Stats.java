/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the Stats Class
*/

package ASMModel;


public abstract class Stats {

   public static double correlation(double[] list1, double[] list2) {
      double correlation = 0;
      int size = list1.length;
      double mean1 = 0;
      double mean2 = 0;
      double x, y;
      double sum1 = 0;
      double sum2 = 0;
      double sum3 = 0;
      for (int i = 0;i<size; i++) {
         mean1 += list1[i];
         mean2 += list2[i];
      }
      mean1 = mean1/size;
      mean2 = mean2/size;
      for (int i = 0;i<size; i++) {
         x = list1[i] - mean1;
         y = list2[i] - mean2;
         sum1 += x*y;
         sum2 += x*x;
         sum3 += y*y;
      }
      correlation = sum1/Math.sqrt(sum2*sum3);
      return correlation;
   }

   public static double correlation(double[] list1, double[] list2, int size) {
      double correlation = 0;
      double mean1 = 0;
      double mean2 = 0;
      double x, y;
      double sum1 = 0;
      double sum2 = 0;
      double sum3 = 0;
      for (int i = 0;i<size; i++) {
         mean1 += list1[i];
         mean2 += list2[i];
      }
      mean1 = mean1/size;
      mean2 = mean2/size;
      for (int i = 0;i<size; i++) {
         x = list1[i] - mean1;
         y = list2[i] - mean2;
         sum1 += x*y;
         sum2 += x*x;
         sum3 += y*y;
      }
      correlation = sum1/Math.sqrt(sum2*sum3);
      return correlation;
   }

   public static double correlation(double[] list1, double[] list2, int size, double mean1, double mean2) {
      double correlation = 0;
      double x, y;
      double sum1 = 0;
      double sum2 = 0;
      double sum3 = 0;
      for (int i = 0;i<size; i++) {
         x = list1[i] - mean1;
         y = list2[i] - mean2;
         sum1 += x*y;
         sum2 += x*x;
         sum3 += y*y;
      }
      correlation = sum1/Math.sqrt(sum2*sum3);
      return correlation;
   }

   public static double sqr(double toSquare) {
      return toSquare*toSquare;
   }

}