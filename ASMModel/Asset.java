/*
 * The LMSR-ASM (Logarithmic Market Scoring Rule Artificial Stock Market)
 * Copyright (C) Athos Carvalho 2021 under a under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Based on the NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the Agent Class
 */

package ASMModel;


import java.awt.*;
import java.awt.event.*;
import java.io.*;

import uchicago.src.sim.util.Random;
import cern.jet.random.*;

public abstract class Asset {

   private static int memory = 2500;    // how many values are used for statistics last memory prices, dividends etc.

   public static double totalSupply = 0;
   protected double price;
   protected double oldPrice;
   protected double unitPrice;


   public Asset() {
   }  // constructor


   public double getPrice() {
      return price;
   }
   public void setPrice(double price) {	// determined and set by market maker
      oldPrice = this.price;
      this.price = price;
   }

   public double getTotalSupply() {
      return totalSupply;
   }
   protected void finalize() throws Throwable {
      totalSupply = 0;
      super.finalize();
   }


   protected static void setMemory(int val) {
      memory = val;
   }
   protected static int getMemory() {
      return memory;
   }


}  // class Asset