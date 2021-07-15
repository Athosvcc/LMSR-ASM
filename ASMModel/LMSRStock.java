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

import uchicago.src.reflector.DescriptorContainer;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.CustomProbeable;

import java.util.Hashtable;


public class LMSRStock extends Asset implements CustomProbeable, DescriptorContainer   {

   protected final static int FIXED = 0 ;
   protected final static int LOGIT = 1 ;
   protected final static int RANDOMWALK = 2 ;

   public static int probabilityProcess = 1  ; 	// sets which probability process is chosen according to previous values
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
   protected static double bLiq = 10;
   protected static double alphaLS = 0.15; // used in Othman (2013): 0.05 // 0.15 corresponds to a "vig" of 20%
   protected static double probability = 0.5;
   protected static double initialPrice = 0.5;
   protected static double probAfterShock = 0.2;
   protected static double periodShock = 0;
   protected int qPosLMSR = 0;
   protected int qNegLMSR = 0;
   protected double qPosInitial = 0;
   protected double qNegInitial = 0;
   protected static double initialQuantity = 10;
   protected double priceNoStock = 0;
   protected double priceSum = 0;

   protected double nextProbability = 0;
   protected double pLagged1 = probability;
   protected double pLagged2 = probability;
   protected double beta1 = 0.02044511;
   protected double beta2 = -1.21038522;
   protected double beta3 = -1.50537449;
   protected double RHS = 0;

   private Hashtable descriptors = new Hashtable();

   public Hashtable getParameterDescriptors() {
    return descriptors;
   }

   public LMSRStock() { // constructor to initialize different value processes
      Hashtable h2 = new Hashtable();
      h2.put(new Integer(LMSRStock.FIXED), "Fixed");
      h2.put(new Integer(LMSRStock.LOGIT), "Logit");
      h2.put(new Integer(LMSRStock.RANDOMWALK),"Random Walk");
      ListPropertyDescriptor pd = new ListPropertyDescriptor("ProbabilityProcess", h2);
      descriptors.put("ProbabilityProcess", pd);
   }  // constructor

   protected void initialize() {
      this.price = initialPrice;     // add probability for LMSR
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
   public void updateProbability() {
      noise = AsmModel.LMSRNormal.nextDouble();
      switch (probabilityProcess) {
         case FIXED: // a fixed probability, subject only to shocks
                  nextProbability = probability;
                  break;
         case LOGIT: // logit specification for event simulation
                  pLagged2 = pLagged1;
                  pLagged1 = probability;
                  RHS = beta1*(World.period) + beta2*pLagged1 + beta3*pLagged2;
//                  System.out.println("RHS: " + RHS);
                  nextProbability = Math.exp(RHS)/(1+Math.exp(RHS));
//                  System.out.println("nextProbability: " + nextProbability);
                  break;
         case RANDOMWALK:
                  nextProbability = probability + noise;
                  if (nextProbability >= 1) {
                     nextProbability = 0.999;
                  }
                  if (nextProbability <= 0) {
                     nextProbability = 0.001;
                  }
                  break;
         default:
                  break;
      }	// switch
      setProbability(nextProbability);
   }  // updateValue


   protected void updateState() {
   }  // updateState


   protected void finalize() throws Throwable {
      super.finalize();
   }
   public void setProbabilityProcess(int process) {
      probabilityProcess = process;
   }
   public int getProbabilityProcess() {
      return probabilityProcess;
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
   }
   public double getProbAfterShock() { return probAfterShock; }
   public void setProbAfterShock(double value) {
      this.probAfterShock = value;
   }
   public double getPeriodShock() { return periodShock; }
   public void setPeriodShock(double value) { this.periodShock = value; }
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
   public double getPriceSum() {return priceSum; }
   public void setPriceSum(double value) { this.priceSum = value; }


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
   public double getUnitPrice() {
      return unitPrice;
   }
   public void setResetLMSRStocks() {
      this.qNegLMSR = 0;
      this.qPosLMSR = 0;
   }

   public String[] getProbedProperties() {
      return new String[] {"probabilityProcess","initialPrice","probability","probAfterShock","periodShock","bLiq", "alphaLS", "initialQuantity"};
   }


}