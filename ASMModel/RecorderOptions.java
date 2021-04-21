/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the RecorderOptions Class
*/

package ASMModel;


 import java.io.*;

 import uchicago.src.sim.util.SimUtilities;
 import uchicago.src.sim.engine.CustomProbeable;

/**
 * This class allows to switch data recording options on or off over the GUI.
 * In order to have it not included in the main NESFI-Settings window, a separate
 * object is created which then can be probed from the NESFI-Settings windows.
 * RecorderOptions basically holds Boolean values which can be set through their
 * appropriate get- and set accessor methods. If a parameter file exists, the
 * preset values in the source code can be overwritten. The variable names for
 * the recorder options should be self explanatory and are often identical to
 * the variable names for the visual display.
 */
public class RecorderOptions implements CustomProbeable {

   private static String recorderOutputFile = "D:/LMSR-NESFI-Data.txt";
   private static int writeFrequency = 2;
   private static int recordFrequency = 1;
   private static int startFromPeriod = 1;
   private static int recordAllFromPeriod = 1;
   private static int recordAllToPeriod = 310000;
   private static boolean calculateCorrelations = false;
   private static boolean technicalBits = true;
   private static boolean fundamentalBits = false;
   private static boolean bitFractions = false;
   private static boolean bitAnalyzer = false;
   private static boolean price = true;
   private static boolean hreePrice = false;
   private static boolean crudePrice = false;
   private static boolean dividend = false;
   private static boolean averageWealth = false;
   private static boolean tradingVolume = false;
   private static boolean meanTradingVolume = false;
   private static boolean meanFitness = false;
   private static boolean minFitness = false;
   private static boolean maxFitness = false;
   private static boolean pvCorr = false;
   private static boolean activeRules = false;
   private static boolean wealthOfClassifierAgents = false;
   private static boolean wealthOfNoClassifierAgents = false;
   private static boolean wealthOfTechnicalTraders = false;
   private static boolean wealthOfFundamentalTraders = false;
   private static boolean wealthOfFastLearner = false;
   private static boolean wealthOfNormalLearner = false;
   private static boolean wealthOfSFIAgents = false;
   private static boolean wealthOfNESFIAgents = false;
   private static boolean numberOfGeneralizations = false;
   private static boolean forecastParameterA = false;
   private static boolean writeRuleUsage = true;
   private static boolean writeAverageStockHoldings = true;
   private static boolean zeroBitAgents = false;
   private static boolean newZeroBitAgentAt = false;
   private static boolean newZeroFundamentalBitAgentAt = false;
   private static boolean newZeroTechnicalBitAgentAt = false;
   private static boolean wealthZeroBitAgents = false;
   private static boolean wealthNonZeroBitAgents = false;
   private static boolean selectAverageCounter = false;
   private static boolean baseWealth = false;
   private static boolean hreeBaseWealth = false;
   private static boolean quantityLMSR = true;
   private static boolean probability = true;
   private static boolean bLiq = true;
   private static boolean marketMakerRevenue = true;
   private static boolean marketMakerLiabilities = true;
   private static boolean marketMakerProfit = true;

   public RecorderOptions() {}


   public boolean getWriteRuleUsage() {
      return writeRuleUsage;
   }
   public void setWriteRuleUsage(boolean number) {
      writeRuleUsage = number;
   }
   public boolean getWriteAverageStockHoldings() {
      return writeAverageStockHoldings;
   }
   public void setWriteAverageStockHoldings(boolean number) {
      writeAverageStockHoldings = number;
   }



   public boolean getNumberOfGeneralizations() {
      return numberOfGeneralizations;
   }
   public void setNumberOfGeneralizations(boolean number) {
      numberOfGeneralizations = number;
   }
   public boolean getTechnicalBits() {
      return technicalBits;
   }
   public void setTechnicalBits(boolean technical) {
      technicalBits = technical;
   }
   public boolean getFundamentalBits() {
      return fundamentalBits;
   }
   public void setFundamentalBits(boolean fundamental) {
      fundamentalBits = fundamental;
   }
   public void setBitFractions(boolean val) {
      bitFractions = val;
   }
   public boolean getBitFractions() {
      return bitFractions;
   }
   public boolean getPrice() {
      return price;
   }
   public void setPrice(boolean val) {
      price = val;
   }
   public boolean getHreePrice() {
      return hreePrice;
   }
   public void setHreePrice(boolean val) {
      hreePrice = val;
   }
   public boolean getCrudePrice() {
      return crudePrice;
   }
   public void setCrudePrice(boolean val) {
      crudePrice = val;
   }

   public boolean getDividend() {
      return dividend;
   }
   public void setDividend(boolean val) {
      dividend = val;
   }
   public boolean getAverageWealth() {
      return averageWealth;
   }
   public void setAverageWealth(boolean val) {
      averageWealth = val;
   }

   public boolean getTradingVolume() {
      return tradingVolume;
   }
   public void setTradingVolume(boolean val) {
      tradingVolume = val;
   }
   public boolean getMeanTradingVolume() {
      return meanTradingVolume;
   }
   public void setMeanTradingVolume(boolean val) {
      meanTradingVolume = val;
   }
   public boolean getPVCorr() {
      return pvCorr;
   }
   public void setPVCorr(boolean val) {
      pvCorr = val;
      if (pvCorr) {
         calculateCorrelations = true;
      } else {
         calculateCorrelations = false;
      }
   }
   /**
    * The frequency with which data are written to the recording file.
    */
   public int getWriteFrequency() {
      return writeFrequency;
   }
   /**
    * The frequency with which data are written to the recording file.
    */
   public void setWriteFrequency(int frequency) {
      writeFrequency = frequency;
   }
   /**
    * The frequency with which data are recorded for writing into the data file.
    * A recordFrequency of 1000 means, for instance, that only every 1000th period the
    * data are recorded to be written into the data file.
    */
   public int getRecordFrequency() {
      return recordFrequency;
   }
   /**
    * The frequency with which data are recorded for writing into the data file.
    * A recordFrequency of 1000 means, for instance, that only every 1000th period the
    * data are recorded to be written into the data file.
    */
   public void setRecordFrequency(int frequency) {
      recordFrequency = frequency;
   }
   public boolean getCalculateCorrelations() {
      return calculateCorrelations ;
   }

   public boolean getMeanFitness() {
      return meanFitness;
   }
   public void setMeanFitness(boolean val) {
      meanFitness = val;
   }
   public boolean getMinFitness() {
      return minFitness;
   }
   public void setMinFitness(boolean val) {
      minFitness = val;
   }
   public boolean getMaxFitness() {
      return maxFitness;
   }
   public void setMaxFitness(boolean val) {
      maxFitness = val;
   }
   public void setActiveRules(boolean val) {
      activeRules = val;
   }
   public boolean getActiveRules() {
      return activeRules;
   }
   public void setStartFromPeriod(int val) {
      startFromPeriod = val;
   }
   public int getStartFromPeriod() {
      return startFromPeriod;
   }
   public void setRecordAllFromPeriod(int val) {
      recordAllFromPeriod = val;
   }
   public int getRecordAllFromPeriod() {
      return recordAllFromPeriod;
   }
   public void setRecordAllToPeriod(int val) {
      recordAllToPeriod = val;
   }
   public int getRecordAllToPeriod() {
      return recordAllToPeriod;
   }
   public void setBitAnalyzer(boolean val) {
      bitAnalyzer = val;
   }
   public  boolean getBitAnalyzer() {
      return bitAnalyzer;
   }
   public boolean getAverageWealthOfClassifierAgents() {
      return wealthOfClassifierAgents;
   }
   public void setAverageWealthOfClassifierAgents(boolean val) {
      this.wealthOfClassifierAgents = val;
   }
   public boolean getAverageWealthOfNoClassifierAgents() {
      return this.wealthOfNoClassifierAgents;
   }
   public void setAverageWealthOfNoClassifierAgents(boolean val) {
      this.wealthOfNoClassifierAgents = val;
   }

   public boolean getAverageWealthOfFundamentalTraders() {
      return this.wealthOfFundamentalTraders;
   }
   public void setAverageWealthOfFundamentalTraders(boolean val) {
      this.wealthOfFundamentalTraders = val;
   }
   public boolean getAverageWealthOfTechnicalTraders() {
      return this.wealthOfTechnicalTraders;
   }
   public void setAverageWealthOfTechnicalTraders(boolean val) {
      this.wealthOfTechnicalTraders = val;
   }

   public boolean getAverageWealthOfFastLearner() {
      return this.wealthOfFastLearner;
   }
   public void setAverageWealthOfFastLearner(boolean val) {
      this.wealthOfFastLearner = val;
   }
   public boolean getAverageWealthOfNormalLearner() {
      return this.wealthOfNormalLearner;
   }
   public void setAverageWealthOfNormalLearner(boolean val) {
      this.wealthOfNormalLearner = val;
   }

   public boolean getAverageWealthOfSFIAgents() {
      return this.wealthOfSFIAgents;
   }
   public void setAverageWealthOfSFIAgents(boolean val) {
      this.wealthOfSFIAgents = val;
   }

   public boolean getForecastParameterA() {
      return this.forecastParameterA;
   }
   public void setForecastParameterA(boolean val) {
      this.forecastParameterA = val;
   }

   public boolean getZeroBitAgents() {
      return this.zeroBitAgents;
   }
   public void setZeroBitAgents(boolean val) {
      this.zeroBitAgents = val;
   }
   public boolean getNewZeroBitAgentAt() {
      return this.newZeroBitAgentAt;
   }
   public void setNewZeroBitAgentAt(boolean val) {
      this.newZeroBitAgentAt = val;
   }
   public boolean getNewZeroFundamentalBitAgentAt() {
      return this.newZeroFundamentalBitAgentAt;
   }
   public void setNewZeroFundamentalBitAgentAt(boolean val) {
      this.newZeroFundamentalBitAgentAt = val;
   }
   public boolean getNewZeroTechnicalBitAgentAt() {
      return this.newZeroTechnicalBitAgentAt;
   }
   public void setNewZeroTechnicalBitAgentAt(boolean val) {
      this.newZeroTechnicalBitAgentAt = val;
   }
   public boolean getAverageWealthOfNESFIAgents() {
      return this.wealthOfNESFIAgents;
   }
   public void setAverageWealthOfNESFIAgents(boolean val) {
      this.wealthOfNESFIAgents = val;
   }
   public void setRecorderOutputFile(String val) { recorderOutputFile = val; }
   public String getRecorderOutputFile() { return recorderOutputFile; }

   public boolean getSelectAverageCounter() {
      return this.selectAverageCounter;
   }
   public void setSelectAverageCounter(boolean val) {
      this.selectAverageCounter = val;
   }
   public void setWealthZeroBitAgents(boolean val) {
      this.wealthZeroBitAgents = val;
   }
   public boolean getWealthZeroBitAgents() {
      return this.wealthZeroBitAgents;
   }
   public void setWealthNonZeroBitAgents(boolean val) {
      this.wealthNonZeroBitAgents = val;
   }
   public boolean getWealthNonZeroBitAgents() {
      return this.wealthNonZeroBitAgents;
   }
   public void setBaseWealth(boolean val) {
      this.baseWealth = val;
   }
   public boolean getBaseWealth() {
      return this.baseWealth;
   }
   public void setHreeBaseWealth(boolean val) {
      this.hreeBaseWealth = val;
   }
   public boolean getHreeBaseWealth() {
      return this.hreeBaseWealth;
   }
   public boolean getQuantityLMSR() {
      return quantityLMSR;
   }
   public void setQuantityLMSR(boolean val) { quantityLMSR = val; }
   public boolean getProbability() {
      return probability;
   }
   public void setProbability(boolean val) {
      probability = val;
   }
   public boolean getMarketMakerRevenue() {
      return marketMakerRevenue;
   }
   public void setMarketMakerRevenue(boolean val) { marketMakerRevenue = val; }
   public boolean getMarketMakerLiabilities() {
      return marketMakerLiabilities;
   }
   public void setMarketMakerLiabilities(boolean val) {
      marketMakerLiabilities = val;
   }
   public boolean getMarketMakerProfit() {
      return marketMakerProfit;
   }
   public void setMarketMakerProfit(boolean val) {
      marketMakerProfit = val;
   }
   public boolean getBLiq() {
      return bLiq;
   }
   public void setBLiq(boolean val) {
      bLiq = val;
   }

  public String[] getProbedProperties() {
      return new String[] {"maxFitness","writeFrequency","recordFrequency","dividend",
      "technicalBits","fundamentalBits","bitFractions","numberOfGeneralizations",
      "price","hreePrice","forecastparameterA",
      "averageWealth","tradingVolume",
      "minFitness","maxFitness","activeRules","pVCorr","startFromPeriod",
      "recordAllFromPeriod","recordAllToPeriod","bitAnalyzer","averageWealthOfClassifierAgents",
      "averageWealthOfNoClassifierAgents","averageWealthOfFundamentalTraders","averageWealthOfTechnicalTraders",
      "averageWealthOfSFIAgents",
      "averageWealthOfNESFIAgents","wealthZeroBitAgents","wealthNonZeroBitAgents",
      "recorderOutputFile","zeroBitAgents","newZeroBitAgentAt","newZeroFundamentalBitAgentAt","newZeroTechnicalBitAgentAt",
      "selectAverageCounter","baseWealth",
      "quantityLMSR","probability","bLiq",
      "marketMakerRevenue","marketMakerLiabilities","marketMakerProfit",
      };
      // deletetd hreeBaseWealth, meanTradingVolume, meanPrice, averageWealthOfNormalLearners, averageWealthOfFastLearner, meanFitness
   }

}  // Recorder