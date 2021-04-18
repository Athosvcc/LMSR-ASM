/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the Stock Class
*/

package ASMModel;

import uchicago.src.reflector.DescriptorContainer;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.CustomProbeable;
import uchicago.src.sim.util.Random;

import java.util.Hashtable;


public class LMSRStock extends Asset implements CustomProbeable, DescriptorContainer   {

   protected final static int OUP = 0 ;
   protected final static int RANDOMWALK = 1 ;
   protected final static int LINEARGROWTH = 2 ;
   protected final static int LOGNORMAL = 3 ;
   protected final static int CONSTANT = 4;
   protected final static int SQUARE = 5;
   protected final static int TRIANGULAR = 6;
   protected final static int SINUS = 7;
   protected final static double MINDIVIDEND = 0.00005; // in general dividend could be zero, but this causes problems for forecasts. (same value as in SFI-ASM used)

   private int dividendProcess = 0  ; 	// which valueprocess-type, standard AR(1)
   protected double rho = 0.95;
   protected double dividendMeanTheoretical = 10.0 ;	// theoretical dividend mean of stochastic process, should exceed risk free interestpayment on one unit of capital
   private double tradingVolume = 0;
   private double tradingVolumeYes = 0;
   private double tradingVolumeNo = 0;
   private double cumulatedTradingVolume = 0;
   private double cumulatedTradingVolumeYes = 0;
   private double cumulatedTradingVolumeNo = 0;
   private double meanTradingVolume = 0;
   private double meanTradingVolumeYes = 0;
   private double meanTradingVolumeNo = 0;
   protected double noiseVar = 0.07429;
   protected double noise = 0;
   protected double bLiq = 10;
   protected double alphaLS = 0.05; // used in Othman (2013): 0.05
   protected boolean liquiditySensitive = false;
   protected double probability = 0.8;
   protected double initialPrice = 0.5;
   protected double probAfterShock = 0.2;
   protected double periodShock = 50;
   protected int qPosLMSR = 0;
   protected int qNegLMSR = 0;
   protected double qPosInitial = 0;
   protected double qNegInitial = 0;
   protected double initialQuantity = 0;
   protected double priceNoStock = 0;

   private double[] pRatios =  {0.25, 0.5, 0.75, 0.875, 1.0, 1.125, 1.25};
   private double[] dRatios =  {0.6, 0.8, 0.9, 1.0, 1.1, 1.12, 1.4};
   private double[] pdRatios = {0.25, 0.5, 0.75, 0.875, 0.95, 1.0, 1.125 }; // ??

   private int[] dStat = {0,0,0,0,0,0,0}; // for debugging; holds number of conditions fulfilled
   private int[] pidStat = {0,0,0,0,0,0,0}; // for debugging; holds number of conditions fulfilled
   private int[] pStat = {0,0,0,0,0,0,0}; // for debugging; holds number of conditions fulfilled
   private int[] fWord = new int[32];      // for debugging, how often are bits set
   private int[] tWord = new int[32];     // for debugging, how often are bits set

   private final int NRATIOS = dRatios.length;

   protected static int time = 0;    // time within period if periodical dividend process
   protected static int waveLength = 0;   // wavelength for periodocal process
   protected static double amplitude = 0;

   protected double oldOldDividend = 0;

   private Hashtable descriptors = new Hashtable();

   public Hashtable getParameterDescriptors() {
    return descriptors;
   }

   public LMSRStock() { // constructor to initialize different value processes
      Hashtable h1 = new Hashtable();
      h1.put(new Integer(LMSRStock.OUP), "Ohrnst.-Uhlenbeck");
      h1.put(new Integer(LMSRStock.RANDOMWALK), "Random Walk");
      h1.put(new Integer(LMSRStock.CONSTANT),"Const. Dividend");
      h1.put(new Integer(LMSRStock.SQUARE),"Square Wave");
      h1.put(new Integer(LMSRStock.TRIANGULAR),"Triangular Wave");
      h1.put(new Integer(LMSRStock.SINUS),"Sinus Wave");
      ListPropertyDescriptor pd = new ListPropertyDescriptor("DividendProcess", h1);
      descriptors.put("DividendProcess", pd);
   }  // constructor

   protected void initialize() {
      maxHistory = Math.max(fmaTime[FNMAS-1],Asset.getMemory());
      priceHistory = new double[maxHistory];
      hreePriceHistory = new double[maxHistory];
      dividendHistory = new double[maxHistory];
      pricePlusDivHistory = new double[maxHistory];
      this.dividend = dividendMeanTheoretical;
      this.dividendMean = dividendMeanTheoretical;
      this.nextDividend = dividend;
      this.oldDividend = dividend;
      this.price = initialPrice;     // add probability for LMSR
      this.oldPrice = price;
      this.priceMean = price;
      this.hreePriceMean = price;
      logReturn = 0;     // do I need it??
      for (int i = 0 ; i < maxHistory ; i++) {
         priceHistory[i] = price;
         hreePriceHistory[i] = price;
         dividendHistory[i] = dividend;
         pricePlusDivHistory[i] = price + dividend;
      }
      for (int i = 0 ; i < TNMAS ; i++ ) {
         priceMAs[i] = price;
      }
      for (int i = 0 ; i < FNMAS ; i++ ) {
         dividendMAs[i] = dividend;
         oldDividendMAs[i] = dividend;
      }
      updateState(); // updates the state of the stock, i.e. world state
      if(dividendProcess >= 5 ) {   // periodical process
         waveLength = 50;  // should be smaller than theta and smaller than GA-invocation interval such that agents are able to detect periodicity
         if (dividendProcess == TRIANGULAR) {
            amplitude = dividendMeanTheoretical/waveLength ;
         } else if (dividendProcess == SQUARE) {
            amplitude = dividendMeanTheoretical/5 ;
         } else if (dividendProcess == SINUS) {
            amplitude = dividendMeanTheoretical/15 ;
         }
         // time = waveLength/4;
      }
   }  // initialize()

   protected void liquiditySensitiveB (double alpha, double qPos, double qNeg) { // implements Liquidity Sensitive LMSR (Othman 2003)
      double b;
      double qTot = qPos + qNeg;
      b = alpha*qTot;
      this.bLiq = b;
   }

   protected void baseQLMSR (double quantity) { // adds artificial initial stocks
      setQNegLMSR(quantity);
      setQPosLMSR(quantity);
   }

   protected double firstPrice (int qPos,int qNeg, double bLiq, boolean pos) { // gets price of buying one stock in LMSR
      double costFunc;
      double costFuncPost;
      double cost;

      costFunc = getBLiq()*Math.log(Math.exp(qPos/getBLiq())+Math.exp(qNeg/getBLiq()));
      if (pos) { // differentiates between buying positive or negative stocks
         costFuncPost = getBLiq()*Math.log(Math.exp((qPos+1)/getBLiq())+Math.exp(qNeg/getBLiq()));
      } else {
         costFuncPost = getBLiq()*Math.log(Math.exp((qPos)/getBLiq())+Math.exp((qNeg+1)/getBLiq()));
      }

      cost = costFuncPost - costFunc;

      return cost;
   }

   protected int qInitLMSR (double initialProbability) { // creates artificial stocks so next price equals given initial probability
      int qInit = 0;
      int iterator = 0;
      double priceLim;

      if (getInitialPrice() < 0.5) { // price of positive and negative stocks are complementary
         priceLim = 1 - getInitialPrice();
      } else {
         priceLim = getInitialPrice();
      }

      while (qInit == 0) {
         iterator++;
         double priceMid = firstPrice(iterator, 0, getBLiq(), true);
         if (priceLim < priceMid) {
            qInit = iterator;
         }
      }
      return qInit;
   }

   protected void probShock () { // adds probability shock at time set in GUI
      setProbability(getProbAfterShock()); //called in ExecutePeriod
   }

   // Standard-update-process for OUP
   public void updateDividend() {
      oldDividend = dividend;
      dividend = nextDividend;      // we could announce nextDividend to some better informed agents, not yet implemented, all use just dividend
      noise = AsmModel.stockNormal.nextDouble();
      switch (dividendProcess) {
         case OUP : // mean-reverting Ohrnstein-Uhlenbeck process with non-iid shocks
                  nextDividend = Math.max(dividendMeanTheoretical + rho * (dividend - dividendMeanTheoretical) + noise,MINDIVIDEND);
                  break;
         case RANDOMWALK : // simple random walk with iid-shocks, as long as the values does not drop below zero
                  nextDividend = Math.max(dividend + noise,MINDIVIDEND) ;
                  break;
         case LINEARGROWTH :
                  dividend += 0.1 ;
                  break;
         case LOGNORMAL :
                  nextDividend = Math.max(dividendMeanTheoretical + rho * (dividend - dividendMeanTheoretical) + Random.normal.nextDouble(0,noiseVar),0.0);
                  break;
         case CONSTANT :
                  nextDividend = dividendMeanTheoretical;
                  break;
         case SQUARE :
                  if ((World.period % waveLength) < waveLength/2 ) {
                     dividend = dividendMeanTheoretical + amplitude;
                  } else {
                     dividend = dividendMeanTheoretical - amplitude;
                  }
                  break;
         case TRIANGULAR:
         // somehow not working, has an upward trend
                  int treduced = World.period % waveLength;
                  if (treduced < waveLength/4) {
                     time++;
                     // dividend = dividendMean + time;
                  } else if (treduced < 3*waveLength/4) {
                     time--;
                     //dividend = dividendMean - treduced;
                  //    dvdnd = baseline + deviation*(2.0*(period - treduced)/spacetime - 1.0);
                  } else {
                     time++;
                  }
                  dividend = dividendMeanTheoretical + time*amplitude;
                  break;
         case SINUS:
                  dividend = dividendMeanTheoretical + amplitude*Math.sin(2*Math.PI*(World.period%waveLength)/waveLength);

                  break;
         default:
                  break;
      }	// switch
      updateDividendHistory();
      updateState();
   }  // updateValue


   /**
    * Update the state of the stock. This serves as the mask against which the condition
    * parts of the rules are compared with.
    */
   protected void updateState() {
         int i = 0;
         int temp = divUpDown_Top + UPDOWNLOOKBACKDIV;
         for (int j = 0 ; j < maxStateWords ; j++) {
            aState[j] = 0; // initializing with zero
         }
            /* Construct tables for fast bit packing and condition checking for
             * classifier systems.  Sets up the global tables SHIFT[], MASK[], and
             * NMASK[] to cover "nbits" condition bits.
             *
             * Assumes 32 bit words, and storage of 16 ternary values (0, 1, or #)
             * per word, with one of the following codings:
             * Value           World coding           Rule coding
             *   0			2			1
             *   1			1			2
             *   #			-			0
             * Thus rule satisfaction can be checked with a simple AND between
             * the two types of codings.

               a. Store "value" (0, 1, 2, using one of the codings above) for bit n with
                  aState[i] |= longValue << SHIFT[n];
                  if the stored value was previously 0;

               b. Store "value" (0, 1, 2, using one of the codings above) for bit n with
                  array[WORD(n)] = (array[WORD(n)] & NMASK[n]) | (value << SHIFT[n]);
                  if the initial state is unknown.

               c. Store value 0 for bit n with
                  array[WORD(n)] &= NMASK[n];

               d. Extract the value of bit n (0, 1, 2, or possibly 3) with
                  value = (array[WORD(n)] >> SHIFT[n]) & 3;

               e. Test for value 0 for bit n with
                  if ((array[WORD(n)] & MASK[n]) == 0) ...

               f. Check whether a condition is fulfilled (using the two codings) with
                  for (i=0; i<maxwords; i++)
                  if (condition[i] & array[i]) break;
                  if (i != maxwords) ...
            */

            /* dividend as multiples of emprirical meanDividend; NRATiOS= 6 bits used */
            for (int j = 0 ; j < NRATIOS ; j++) {
               if (dividend/dividendMean > dRatios[j]) {
                  aState[0] |= 1l << SHIFT[i++];
                  fWord[i-1]++;
                  dStat[j]++;
               }
               else {
                  aState[0] |= 2l << SHIFT[i++];
               }
            }
            /* crude over- or underpricing, NRATIOS=6 bits used */
            for (int j = 0 ; j < NRATIOS ; j++) {
               if (price*World.interestRate/oldDividend > pdRatios[j]) {
                  aState[0] |= 1l << SHIFT[i++];
                     fWord[i-1]++;
                     pidStat[j]++;
              }
               else {
                  aState[0] |= 2l << SHIFT[i++];
               }
            }
            /* Dividend went up or down, for the last UPDOWNLOOKBACK-periods, 5 bits used */
            for (int j = 0; j < UPDOWNLOOKBACKDIV ; j++, temp-- ) {
               if(divUpDown[temp % UPDOWNLOOKBACKDIV]) {
                  aState[0] |= 1l << SHIFT[i++];
                  fWord[i-1]++;
               } else {
                  aState[0] |= 2l << SHIFT[i++];
               }
            }  // for
            /* Dividend-MAs went up or down, NMAS=4 bits used */
            for (int j = 0 ; j < FNMAS ; j++) {
               if (oldDividendMAs[j] > dividendMAs[j]) {
                  aState[0] |= 1l << SHIFT[i++];
                  fWord[i-1]++;
               }
               else {
                  aState[0] |= 2l << SHIFT[i++];
               }
            }
            /* Dividend > DividendMAs, NMAS=4 bits used */
            for (int j = 0 ; j < FNMAS ; j++) {
               if (dividend > dividendMAs[j]) {
                  aState[0] |= 1l << SHIFT[i++];
                  fWord[i-1]++;
               }
               else {
                  aState[0] |= 2l << SHIFT[i++];
               }
            }
            /* relations between DividendMAs; 3+2+1=6 bits used*/
            for (int j = 0 ; j < FNMAS-1 ; j++) {
               for (int k = j+1 ; k < FNMAS ; k++ ) {
                  if (dividendMAs[j] > dividendMAs[k]) {
                     aState[0] |= 1l << SHIFT[i++];
                     fWord[i-1]++;
                  }
                  else {
                     aState[0] |= 2l << SHIFT[i++];
                  }
               }
            }
            /* Now we are done with the 31 fundamental state bits. They are all stored in aState[0].
               The SFI-ASM uses more fundamental bits, but restricting them to 31 makes it possible
               to store them all in one long-word.
               Now we are constructing the technical bit states which are all stored in aState[1].
            */
            i = 0;
            temp = priceUpDown_Top + UPDOWNLOOKBACKPRICE;
            /* price as multiples of emprirical meanDividend; NRATiOS= 6 bits used */
            for (int j = 0 ; j < NRATIOS ; j++) {
               if (price/priceMean > pRatios[j]) {
                  aState[1] |= 1l << SHIFT[i++];
                     tWord[i-1]++;
                     pStat[j]++;
               }
               else {
                  aState[1] |= 2l << SHIFT[i++];
               }
            }
            /* Prices went up or down, for the last UPDOWNLOOKBACK-periods, 5 bits used */
            for (int j = 0; j < UPDOWNLOOKBACKPRICE ; j++, temp-- ) {
               if(priceUpDown[temp % UPDOWNLOOKBACKPRICE]) {
                  aState[1] |= 1l << SHIFT[i++];
                  tWord[i-1]++;
               } else {
                  aState[1] |= 2l << SHIFT[i++];
               }
            }  // for
            /* Price-MAs went up or down , NMAS=4 bits used */
            for (int j = 0 ; j < TNMAS ; j++) {
               if (oldPriceMAs[j] > priceMAs[j]) {
                  aState[1] |= 1l << SHIFT[i++];
                  tWord[i-1]++;
               }
               else {
                  aState[1] |= 2l << SHIFT[i++];
               }
            }
            /* Price > priceMAs, NMAS=4 bits used */
            for (int j = 0 ; j < TNMAS ; j++) {
               if (price >  priceMAs[j]) {
                  aState[1] |= 1l << SHIFT[i++];
                  tWord[i-1]++;
               }
               else {
                  aState[1] |= 2l << SHIFT[i++];
               }
            }
            /* relations between DividendMAs; 4+3+2+1=10 bits used*/
            for (int j = 0 ; j < TNMAS-1 ; j++) {
               for (int k = j+1 ; k < TNMAS ; k++ ) {
                  if (priceMAs[j] > priceMAs[k]) {
                     aState[1] |= 1l << SHIFT[i++];
                     tWord[i-1]++;
                  }
                  else {
                     aState[1] |= 2l << SHIFT[i++];
                  }
               }
            }
//         show_state();
   }  // updateState


   /**
    * This procedure prints the state of the world, i.e. of the stock to the console.
    * The coding has already been changed such that "1" means "condition fulfilled"
    * and "0" means "condition not fulfilled".
    * The signs "|" marks the logical trits, i.e., those sequences which consist of
    * several bits.
   */
   protected void show_state() {
      String bitString0, bitString1, temp;
      long val;
      bitString0 = "Word 0: ";
      bitString1 = "Word 1: ";
      for (int i = 0; i < 2 ; i++) {
         temp="";
         for (int j = 0; j < 32; j++) {
            val = ((aState[i] >> Asset.SHIFT[j]) & 3l);
            if (val == 1l) {
               temp += "1";
            } else {
               temp += "0";
            }
            if(j==6 || j==13 || j==24) {temp += "|";}
         }
         if(i==0) {bitString0 += temp;} else {bitString1 += temp;}
      }
      System.out.println("Period "+World.period+"  "+bitString0);
      System.out.println("Period "+World.period+"  "+bitString1);
   }  // show_state


   protected void finalize() throws Throwable {
      super.finalize();
   }
   public void setLiquiditySensitive(boolean sensitive) {
      liquiditySensitive = sensitive;
   }
   public boolean getLiquiditySensitive() {
      return liquiditySensitive;
   }
   public void setDividendProcess(int process) {
      dividendProcess = process;
   }
   public int getDividendProcess() {
      return dividendProcess;
   }
   public double getRho() {
      return rho;
   }
   public void setRho(double value) {
      this.rho = value;
   }
   public void setNoiseVar(double value) {
      this.noiseVar = value;
   }
   public double getNoiseVar() {
      return noiseVar;
   }
   public double getDividendMean() {
      return dividendMeanTheoretical;
   }
   public void setDividendMean(double value) {
      this.dividendMeanTheoretical = value;
   }
   public double getBLiq() { return bLiq; }
   public void setBLiq(double value) {
      this.bLiq = value;
   }
   public double getAlphaLS() { return alphaLS; }
   public void setAlphaLS(double value) {
      this.alphaLS = value;
   }
   public double getProbability() { return probability; }
   public void setProbability(double value) {
      this.probability = value;
   }
   public double getInitialPrice() { return initialPrice; }
   public void setInitialPrice(double value) {
      this.initialPrice = value;
   }
   public double getInitialQuantity() { return initialQuantity; }
   public void setInitialQuantity(double value) {
      this.initialQuantity = value;
   } // mudar com double funciona sl pq
   public double getProbAfterShock() { return probAfterShock; }
   public void setProbAfterShock(double value) {
      this.probAfterShock = value;
   }
   public double getPeriodShock() { return periodShock; }
   public void setPeriodShock(double value) { this.periodShock = value; } // mudar com double funciona sl pq
   public double getQPosLMSR() { return qPosLMSR; }
   public void setQPosLMSR(double value) { this.qPosLMSR += value; }
   public double getQNegLMSR() { return qNegLMSR; }
   public void setQNegLMSR(double value) { this.qNegLMSR += value; }
   public double getQPosInitial() { return qPosInitial; }
   public void setQPosInitial(double value) { this.qPosInitial = value; }
   public double getQNegInitial() { return qNegInitial; }
   public void setQNegInitial(double value) { this.qNegInitial = value; }
   public double getPriceNoStock() {return priceNoStock; }
   public void setPriceNoStock(double value) { this.priceNoStock = value; }


   public double getTradingVolume() {
      return tradingVolume;
   }
   public void setTradingVolume(double volume) {
      tradingVolume = volume;
      cumulatedTradingVolume += volume;
      meanTradingVolume = cumulatedTradingVolume / World.period ;
   }
   public double getTradingVolumeYes() {
      return tradingVolumeYes;
   }
   public void setTradingVolumeYes(double volume) {
      tradingVolumeYes = volume;
      cumulatedTradingVolumeYes += volume;
      meanTradingVolumeYes = cumulatedTradingVolumeYes / World.period ;
   }
   public double getTradingVolumeNo() {
      return tradingVolumeNo;
   }
   public void setTradingVolumeNo(double volume) {
      tradingVolumeNo = volume;
      cumulatedTradingVolumeNo += volume;
      meanTradingVolumeNo = cumulatedTradingVolumeNo / World.period ;
   }
   public double getMeanTradingVolume() {
      return meanTradingVolume;
   }
   public double getMeanTradingVolumeYes() {
      return meanTradingVolumeYes;
   }
   public double getMeanTradingVolumeNo() {
      return meanTradingVolumeNo;
   }
   public double getHreePrice() {
      return hreePrice;
   }
   public double getPVCorr() {
      return pvCorr;
   }
   public double getUnitPrice() {
      return unitPrice;
   }

   public String[] getProbedProperties() {
      return new String[] {"initialPrice","probability","probAfterShock","periodShock","dividendProcess", "bLiq", "liquiditySensitive", "alphaLS", "initialQuantity"};
   }


}