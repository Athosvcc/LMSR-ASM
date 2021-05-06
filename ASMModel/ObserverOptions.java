/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the ObserverOptions Class
*/

package ASMModel;


import uchicago.src.sim.engine.CustomProbeable;

/**
 * This class allows to switch display options on and off over the GUI.
 *
 */
public class ObserverOptions implements CustomProbeable {

   protected static int updateFrequency = 1 ;
   protected boolean showAgentGrid = false ;
   protected boolean showStocks = false;
   protected boolean showPrice = true;
   protected boolean showVolume = true;
//   protected boolean showWealthSFIAgents = false;
//   protected boolean showWealthNESFIAgents = false;
   protected boolean showMarketMakerRevenue = true;
   protected boolean showMarketMakerLiabilities = true;
   protected boolean showMarketMakerProfit = true;
   protected boolean showBLiq = false;
   protected boolean showProbability = true;

   protected static int displayTo = 0;

   public ObserverOptions() {
      getPriceDisplay();
   }  // constructor

   public boolean getShowAgentGrid() {
      return showAgentGrid;
   }
   public void setShowAgentGrid(boolean showAgents) {
      showAgentGrid = showAgents;
   }
   public boolean getShowPrice() {
      return showPrice;
   }
   public void setShowPrice(boolean price) {
      showPrice = price;
      getPriceDisplay();
   }
   public int getUpdateFrequency() {
      return updateFrequency;
   }
   public void setUpdateFrequency(int frequency) {
      updateFrequency = frequency;
   }

   public boolean getShowVolume() {
      return showVolume;
   }
   public void setShowVolume(boolean val) {
      showVolume = val;
   }

   protected void getPriceDisplay() {
      if(showPrice) {
         showStocks = true;
      }  else {
         showStocks = false;
      }
   }

//   public boolean getShowWealthSFIAgents() {
//      return showWealthSFIAgents;
//   }
//   public void setShowWealthSFIAgents(boolean val) {
//      showWealthSFIAgents = val;
//   }
//   public boolean getShowWealthNESFIAgents() {
//      return showWealthNESFIAgents;
//   }
//   public void setShowWealthNESFIAgents(boolean val) {
//      showWealthNESFIAgents = val;
//   }
   public boolean getShowMarketMakerRevenue() {
      return showMarketMakerRevenue;
   }
   public void setShowMarketMakerRevenue(boolean val) {
      showMarketMakerRevenue = val;
   }
   public boolean getShowMarketMakerLiabilities() {
      return showMarketMakerLiabilities;
   }
   public void setShowMarketMakerLiabilities(boolean val) {
      showMarketMakerLiabilities = val;
   }
   public boolean getShowMarketMakerProfit() {
      return showMarketMakerProfit;
   }
   public void setShowMarketMakerProfit(boolean val) {
      showMarketMakerProfit = val;
   }
   public boolean getShowBLiq() {
      return showBLiq;
   }
   public void setShowBLiq(boolean val) {
      showBLiq = val;
   }
   public boolean getShowProbability() {
      return showProbability;
   }
   public void setShowProbability(boolean val) {
      showProbability = val;
   }


   /**
    * This determines which checkboxes are to be displayed in the GUI. It basically avoids
    * one unwanted entry of <Class>. I also wanted to have it ordered according to the
    * sequence as shown in the String. Yet switching off ALPHA_ORDER seems to work only
    * on the top level GUI, not for the objects like this that are onyl created to set
    * certain parameter values.
    */
   public String[] getProbedProperties() {
      return new String[] {"showAgentGrid",
         "showBitDistribution","showBitFractions","showFundamentalBits","showTechnicalBits",
         "showDividend","showDivMean","showCrudePrice","showHreeMean",
         "showPrice","showHreePrice","showHreePrice_Price","showPriceValueCorr",
         "showPriceMean","showPriceMA5","showPriceMA25","showPriceMA50","showPriceMA100",
         "showLogReturns","showVolume",
         "showWealthSFIAgents","showWealthNESFIAgents",
         "showWealthClassifierAgents","showWealthNoClassifierAgents",
         "showWealthFundamentalTraders","showWealthTechnicalTraders",
         "showWealthNormalLearner","showWealthFastLearner",
         "showWealthZeroBitAgents","showWealthNonZeroBitAgents",
         "showBaseWealth","showLongTermHreeBaseWealth",
         "showMarketMakerRevenue","ShowMarketMakerLiabilities","ShowMarketMakerProfit",
         "ShowBLiq","ShowProbability",
         "updateFrequency"
      };
   }  // getProbedProperties()

}  // ObserverOptions()

