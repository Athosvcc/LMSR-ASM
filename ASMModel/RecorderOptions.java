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

   private static String recorderOutputFile = "D:/LMSR-ASM-Data.txt";
   private static int writeFrequency = 2;
   private static int recordFrequency = 1;
   private static int startFromPeriod = 1;
   private static int recordAllFromPeriod = 1;
   private static int recordAllToPeriod = 310000;
   private static boolean price = true;
   private static boolean averageWealth = false;
   private static boolean tradingVolume = false;
//   private static boolean wealthOfSFIAgents = false; // mudar
//   private static boolean wealthOfNESFIAgents = false;
   private static boolean baseWealth = false;
   private static boolean quantityLMSR = true;
   private static boolean probability = true;
   private static boolean bLiq = true;
   private static boolean marketMakerRevenue = true;
   private static boolean marketMakerLiabilities = true;
   private static boolean marketMakerProfit = true;

   public RecorderOptions() {}

   public boolean getPrice() {
      return price;
   }
   public void setPrice(boolean val) {
      price = val;
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
//   public boolean getAverageWealthOfSFIAgents() { // mudar
//      return this.wealthOfSFIAgents;
//   }
//   public void setAverageWealthOfSFIAgents(boolean val) {
//      this.wealthOfSFIAgents = val;
//   }
//   public boolean getAverageWealthOfNESFIAgents() {
//      return this.wealthOfNESFIAgents;
//   }
//   public void setAverageWealthOfNESFIAgents(boolean val) {
//      this.wealthOfNESFIAgents = val;
//   }
   public void setRecorderOutputFile(String val) { recorderOutputFile = val; }
   public String getRecorderOutputFile() { return recorderOutputFile; }

   public void setBaseWealth(boolean val) {
      this.baseWealth = val;
   }
   public boolean getBaseWealth() {
      return this.baseWealth;
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
      return new String[] {"writeFrequency","recordFrequency",
      "price","averageWealth",
      "recordAllFromPeriod","recordAllToPeriod",
      "averageWealthOfSFIAgents",
      "averageWealthOfNESFIAgents",
      "recorderOutputFile","baseWealth",
      "quantityLMSR","probability","bLiq",
      "marketMakerRevenue","marketMakerLiabilities","marketMakerProfit",
      };
   }

}  // Recorder