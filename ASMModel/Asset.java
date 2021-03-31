/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the Asset Class
*/

package ASMModel;


import java.awt.*;
import java.awt.event.*;
import java.io.*;

import uchicago.src.sim.util.Random;
import cern.jet.random.*;

/**
 * It contains all basic methods for different assets. The structure with an abstract
 * asset class and one single derived class is due to the initial design of a multi-asset
 * model. In fact, this version has been stripped down from an existing two stock version
 * in which I create two stocks from the stock class, and an index, that also extends asset.
 */
public abstract class Asset {

   private static int memory = 2500;    // how many values are used for statistics last memory prices, dividends etc.

   protected final int maxStateWords = 2 ;
   protected long[] aState = new long[maxStateWords];    // holds the state of the assets, replace 2 by maxCondWord
   protected static int nbits = 32;                      // I don't really understand it why I need 33, even if I store only 32 bits, but it doesn't work otherwise with the arrays SHIFT, MASK, NMASK
   protected static int[] SHIFT;
   protected static long[] MASK;
   protected static long[] NMASK;

   public static double totalSupply = 0;
   protected static double pvCorr = 0; // holds price-value correlation for stock 0, not nice, but it works with correlationGraph
   protected double price;
   protected double priceMean;				// empirical price mean of simulated data
   protected double oldPrice;                           // faster for log-calculation
   protected double oldHreePrice;                       // faster for log-calculation
   protected double logReturn;
   protected double logHreePrice;
   protected double nextDividend;
   protected double dividend;
   protected double oldDividend;
   protected double dividendMean;
   protected double pricePlusDivMean;
   protected double hreePrice;
   protected double unitPrice;

   protected double hreePriceMean;
   protected double pricePlusDiv;

   protected final int FNMAS = 4 ;	// number of moving averages for fundamental bits to be calculated
   protected final int[] fmaTime = {5, 10, 100 , 500 } ;	// which moving averages
   protected final int TNMAS = 5 ;	// number of moving averages for fundamental bits to be calculated
   protected final int[] tmaTime = {5, 10, 20, 100 , 500 } ;	// which moving averages
   protected double[] priceMAs = new double[TNMAS];
   protected double[] oldPriceMAs = new double[TNMAS];
   protected double[] dividendMAs = new double[FNMAS];
   protected double[] oldDividendMAs = new double[FNMAS];
//   protected double[] aWeight = new double[NMAS];
//   protected double[] bWeight = new double[NMAS];
   protected boolean exponentialMAs = false;
   protected final int UPDOWNLOOKBACKDIV = 4;
   protected final int UPDOWNLOOKBACKPRICE = 5;
   protected boolean[] divUpDown = new boolean[UPDOWNLOOKBACKDIV];
   protected int divUpDown_Top = 0;
   protected boolean[] priceUpDown = new boolean[UPDOWNLOOKBACKPRICE];
   protected int priceUpDown_Top = 0;
   protected int maxHistory = Math.max(tmaTime[TNMAS-1],memory);
   public double[] priceHistory ;
   public double[] hreePriceHistory ;
   protected int priceHistoryTop = 0;
   protected int dividendHistoryTop = 0;
   public double[] dividendHistory ;
   public double[] pricePlusDivHistory ;

   public Asset() {
      stateInit();
//      if (exponentialMAs) {
//         for (int i = 0; i < NMAS ; i++) {
//            aWeight[i] = Math.exp(-1.0/maTime[i]);
//            bWeight[i] = 1.0 - bWeight[i];
//            bWeight[i] = 2d/(maTime[i] + 1);
//            aWeight[i] = 1;
//         }
//      }  // exponentialMAs ??
   }  // constructor


   public double getPrice() {
      return price;
   }
   public void setPrice(double price) {	// determined and set by market maker
      oldPrice = this.price;
      this.price = price;
      updatePriceHistory();
   }
   public double getPriceMean() {
      return priceMean;
   }
   public double getHreePriceMean() {
      return hreePriceMean;
   }

   protected double getDivMean() {
      return dividendMean;
   }
   protected void setRePrice(double rePrice) {
      oldHreePrice = this.hreePrice;
      hreePrice = rePrice;
   }
   protected double getDividend() {
      return dividend;
   }
   /**
    * Informed trader are equipped with knowledge about next periods dividend. With this
    * knowledge we can simulate whether this knowledge can spread through the economy and
    * start a trend.
    * I don't think so since we can look only one period ahead which is to little to
    * create a trend
    */
   public double getNextDividend() {
      return nextDividend;
   }
   public double getDividend(int back) {
      return dividendHistory[(dividendHistoryTop - back) % maxHistory] ;
   }

   protected void updatePriceHistory() {
      int whichMA;
      int rago;
      double deltap;
      logReturn = Math.log(price)-Math.log(oldPrice);
      logHreePrice = Math.log(hreePrice)-Math.log(oldHreePrice);
      priceHistoryTop += 1 + maxHistory;
      if (exponentialMAs) {	// exponentialMAs
//         for (int i = 0; i < NMAS; i++) {
//            whichMA = maTime[i];
//            // rago = (priceHistoryTop - whichMA) % maxHistory;
//            priceMAs[i] = aWeight[i]*priceMAs[i] + bWeight[i]*(price - priceMAs[i]);
//         }
      } else {
         for (int i = 0 ; i < TNMAS ; i++) {
            whichMA = tmaTime[i];
            oldPriceMAs[i] = priceMAs[i];
            rago = (priceHistoryTop - whichMA) % maxHistory;
            deltap = price - priceHistory[rago];
            priceMAs[i] += deltap/whichMA;
         }
      }
      /* Althoug it's easy to calculate to means of price and hreePrice based on the total
         time series, just the values for the last <memory<-periods are taken into account.
      */
         rago = (priceHistoryTop - maxHistory) % maxHistory;
         deltap = price - priceHistory[rago];
         priceMean += deltap/maxHistory;
         rago = (priceHistoryTop - maxHistory) % maxHistory;
         deltap = hreePrice - hreePriceHistory[rago];
         hreePriceMean += deltap/maxHistory;

      /* Update the binary up/down indicator for price */

      priceUpDown_Top = (priceUpDown_Top + 1) % UPDOWNLOOKBACKPRICE;
      priceHistoryTop %= maxHistory;		// determines actual position at which new price is inserted into priceArray for MA-calculation
      priceUpDown[priceUpDown_Top] = price > priceHistory[priceHistoryTop];

      priceHistory[priceHistoryTop] = price ;
      hreePriceHistory[priceHistoryTop] = hreePrice;

      pricePlusDivHistory[priceHistoryTop] = price + dividend; // now that price is finally determined, insert p+d at the same index-position into pricePlusDividend
      pricePlusDivMean = priceMean + dividendMean;
      pricePlusDiv = price + dividend;

   }  // public void updatePriceHistory

   protected void updateDividendHistory() {
      /* cannot be put together with updatePriceHistory since it happens at different times
      */
      int whichMA;
      int rago;
      double deltad;
      dividendHistoryTop += 1 + maxHistory;
      if (exponentialMAs) {	// exponentialMAs
//         for (int i = 0; i < NMAS; i++) {
//            whichMA = maTime[i];
//            oldDividendMAs[i] = dividendMAs[i];
//            rago = (dividendHistoryTop - whichMA) % maxHistory;
//            dividendMAs[i] = aWeight[i]*dividendMAs[i] + bWeight[i]*(dividend - dividendHistory[rago]);
//         }
      } else {
         for (int i = 0 ; i < FNMAS ; i++) {
            whichMA = fmaTime[i];
            oldDividendMAs[i] = dividendMAs[i];
            rago = (dividendHistoryTop - whichMA) % maxHistory;
            deltad = dividend - dividendHistory[rago];
            dividendMAs[i] += deltad/whichMA;
         }
      }
         rago = (dividendHistoryTop - maxHistory) % maxHistory;
         deltad = dividend - dividendHistory[rago];
         dividendMean += deltad/maxHistory;
      /* Update the binary up/down indicator for dividend */
      divUpDown_Top = (divUpDown_Top + 1) % UPDOWNLOOKBACKDIV;
      dividendHistoryTop %= maxHistory;		// determines actual position at which new dividend is inserted

      divUpDown[divUpDown_Top] = dividend > dividendHistory[dividendHistoryTop];
      dividendHistory[dividendHistoryTop] = dividend;
   }  // updateDividendHistory

   public double getPriceMA(int whichMA) {
      return priceMAs[whichMA];
   }
   public double getDividendMA(int whichMA) {
      return dividendMAs[whichMA];
   }

   public double getTotalSupply() {
      return totalSupply;
   }
   protected void finalize() throws Throwable {
      totalSupply = 0;
      super.finalize();
   }

   /**
   * Here I use the fast bit packing and condition checking algorithms
   * from the original Santa Fe Model. I first tried an implementation with BitSet,
   * which was much easier to implement, but it really slowed down the application. Having a
   * fast algorithm here is at the core of the simulation, since condition checking is the
   * routine which is most often called in the whole simulation run.
   *
   * Construct tables for fast bit packing and condition checking for
   * classifier systems.  Sets up the global tables SHIFT[], MASK[], and
   * NMASK[] to cover "nbits" condition bits.
   *
   * Assumes 64 bit words (long). Since the last bit is used for sign, only bits 0 - 61
   * can be used. Thus, one condition word can store nbits = 31 ternary values (0, 1, or *)
   * per word, with one of the following codings:
   * Value           World coding           Rule coding
   *   0			2			1
   *   1			1			2
   *   *			-			0
   * Thus rule satisfaction can be checked with a simple AND between
   * the two types of codings. If the result is >0, then the rule is not satisfied.
   *
   * Since we need more than 31 world bits to code, the asset has an array long[] aState
   * in which the single words are stored. It is straightforward to group the words, thus
   * fundamental traders just have access to aState[0] which is big enough to hold all
   * fundamental bits. Technical traders have also access to aState[1] and aState[2] which
   * code also technical condotions.
   *
   * After calling this routine, given an array declared as
   *		int aState[maxStateWords];
   * you can do the following:
   *
   * a. Store "value" (0, 1, 2, using one of the codings above) for bit n with
   *	aState[WORD(n)] |= value << SHIFT[n];
   *    if the stored value was previously 0; or
   *
   * b. Store "value" (0, 1, 2, using one of the codings above) for bit n with
   *	aState[WORD(n)] = (array[WORD(n)] & NMASK[n]) | (value << SHIFT[n]);
   *    if the initial state is unknown.
   *
   * c. Store value 0 for bit n with
   *	aState[WORD(n)] &= NMASK[n];
   *
   * d. Extract the value of bit n (0, 1, 2, or possibly 3) with
   *	value = (aState[WORD(n)] >> SHIFT[n]) & 3;
   *
   * e. Test for value 0 for bit n with
   *	if ((aState[WORD(n)] & MASK[n]) == 0) ...
   *
   * f. Check whether a condition is fulfilled (using the two codings) with
   *	for (i=0; i<maxwords; i++)
   *	    if (condition[i] & aState[i]) break;
   *	if (i != maxwords) ...
   *
   */
   protected void stateInit() {
      for (int i = 0 ; i < maxStateWords ; i++ ) { // initialize array with state of the asset
         aState[i] = 0l;
      }
      SHIFT = new int[nbits];
      MASK = new long[nbits];
      NMASK = new long[nbits];
      for (int bit = 0 ; bit < nbits ; bit++) {
         SHIFT[bit] = (bit % 32)*2;
         MASK[bit] = 3l << SHIFT[bit];
         NMASK[bit] = ~MASK[bit];
      }
      int a =1;
   }  // stateInit
   /**
    * Updates the state of the asset using the fast bit packing and condition
    * checking algoarithm as described above.
    */
   protected abstract void updateState();

   protected static void setMemory(int val) {
      memory = val;
   }
   protected static int getMemory() {
      return memory;
   }


}  // class Asset