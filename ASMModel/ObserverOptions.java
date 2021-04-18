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
   protected static boolean calculateCorrelations = false;
   protected boolean showTechnicalBits = false;
   protected boolean showFundamentalBits = false;
   protected boolean showBitFractions = false;
   protected boolean showAgentGrid = false ;
   protected boolean showStocks = false;
   protected boolean showPrice = true;
   protected boolean showHreePrice = false;
   protected boolean showHreePrice_Price = false;
   protected boolean showCrudePrice = false ;
   protected boolean showDividend = false;
   protected boolean showPriceMA5 = false;
   protected boolean showPriceMA25 = false;
   protected boolean showPriceMA50 = false;
   protected boolean showPriceMA100 = false;
   protected boolean showPriceMean = false;
   protected boolean showDivMean = false;
   protected boolean showHreeMean = false;
   protected boolean showLogReturns = false;
   protected boolean showVolume = true;
   protected boolean showPriceValueCorr = false;
   protected boolean showWealthClassifierAgents = false;
   protected boolean showWealthNoClassifierAgents = false;
   protected boolean showWealthTechnicalTraders = false;
   protected boolean showWealthFundamentalTraders = false;
   protected boolean showWealthNormalLearner = false;
   protected boolean showWealthFastLearner = false;
   protected boolean showWealthSFIAgents = false;
   protected boolean showWealthNESFIAgents = false;
   protected boolean showWealthZeroBitAgents = false;  // are they as ggod as those who discarded bit usage
   protected boolean showWealthNonZeroBitAgents = false; // those who keep using bits, are they doing better?, used to analyze the classifier system with periodic dividend data.
   protected boolean showBaseWealth = false;
   protected boolean showLongTermHreeBaseWealth = false;
   protected boolean showMarketMakerRevenue = true;
   protected boolean showMarketMakerLiabilities = true;
   protected boolean showMarketMakerProfit = true;

   protected boolean showBitDistribution = false;

   protected static int displayTo = 0;

   public ObserverOptions() {
      getPriceDisplay();
   }  // constructor

   public boolean getShowBitDistribution() {
      return showBitDistribution;
   }
   public void setShowBitDistribution(boolean val) {
      showBitDistribution = val;
   }


   public boolean getShowTechnicalBits() {
      return showTechnicalBits;
   }
   public void setShowTechnicalBits(boolean technical) {
      showTechnicalBits = technical;
   }
   public boolean getShowFundamentalBits() {
      return showFundamentalBits;
   }
   public void setShowFundamentalBits(boolean fundamental) {
      showFundamentalBits = fundamental;
   }
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
   public boolean getShowHreePrice() {
      return showHreePrice;
   }
   public void setShowHreePrice(boolean hree) {
      showHreePrice = hree;
      getPriceDisplay();
   }
   public void setShowHreePrice_Price(boolean val) {
      showHreePrice_Price = val;
      getPriceDisplay();
   }
   public boolean getShowHreePrice_Price() {
      return showHreePrice_Price;
   }
   public boolean getShowCrudePrice() {
      return showCrudePrice;
   }
   public void setShowCrudePrice(boolean crude) {
      showCrudePrice = crude;
      getPriceDisplay();
   }
   public boolean getShowDividend() {
      return showDividend;
   }
   public void setShowDividend(boolean dividend) {
      showDividend = dividend;
      if(showPrice || showHreePrice || showCrudePrice || showDividend || showPriceMA5 || showPriceMA25 || showPriceMA50 || showPriceMA100) {
         showStocks = true;
      } else {
         showStocks = false;
      }
      getPriceDisplay();
   }
   public boolean getShowPriceMA5() {
      return showPriceMA5;
   }
   public void setShowPriceMA5(boolean ma5) {
      showPriceMA5 = ma5;
      if(showPrice || showHreePrice || showCrudePrice || showDividend || showPriceMA5 || showPriceMA25 || showPriceMA50 || showPriceMA100) {
         showStocks = true;
      } else {
         showStocks = false;
      }
      getPriceDisplay();
   }
   public boolean getShowPriceMA25() {
      return showPriceMA25;
   }
   public void setShowPriceMA25(boolean ma25) {
      showPriceMA25 = ma25;
      if(showPrice || showHreePrice || showCrudePrice || showDividend || showPriceMA5 || showPriceMA25 || showPriceMA50 || showPriceMA100) {
         showStocks = true;
      } else {
         showStocks = false;
      }
      getPriceDisplay();
   }
   public boolean getShowPriceMA50() {
      return showPriceMA50;
   }
   public void setShowPriceMA50(boolean ma50) {
      showPriceMA50 = ma50;
      if(showPrice || showHreePrice || showCrudePrice || showDividend || showPriceMA5 || showPriceMA25 || showPriceMA50 || showPriceMA100) {
         showStocks = true;
      } else {
         showStocks = false;
      }
      getPriceDisplay();
   }
   public boolean getShowPriceMA100() {
      return showPriceMA100;
   }
   public void setShowPriceMA100(boolean ma100) {
      showPriceMA100 = ma100;
      if(showPrice || showHreePrice || showCrudePrice || showDividend || showPriceMA5 || showPriceMA25 || showPriceMA50 || showPriceMA100) {
         showStocks = true;
      } else {
         showStocks = false;
      }
      getPriceDisplay();
   }
   public boolean getShowPriceValueCorr() {
      return showPriceValueCorr;
   }
   public void setShowPriceValueCorr(boolean val) {
      showPriceValueCorr = val;
      calculateCorrelations = true;
   }
   public int getUpdateFrequency() {
      return updateFrequency;
   }
   public void setUpdateFrequency(int frequency) {
      updateFrequency = frequency;
   }
   public boolean getShowLogReturns() {
      return showLogReturns;
   }
   public void setShowLogReturns(boolean val) {
      showLogReturns = val;
   }

   public boolean getShowPriceMean() {
      return showPriceMean;
   }
   public void setShowPriceMean(boolean val) {
      showPriceMean = val;
      getPriceDisplay();
   }
   public boolean getShowDivMean() {
      return showDivMean;
   }
   public void setShowDivMean(boolean val) {
      showDivMean = val;
      getPriceDisplay();
   }
   public boolean getShowHreeMean() {
      return showHreeMean;
   }
   public void setShowHreeMean(boolean val) {
      showHreeMean = val;
      getPriceDisplay();
   }
   public boolean getShowVolume() {
      return showVolume;
   }
   public void setShowVolume(boolean val) {
      showVolume = val;
   }

   protected void getPriceDisplay() {
      if(showPrice || showHreePrice || showCrudePrice || showDividend || showPriceMA5 || showPriceMA25 || showPriceMA50 || showPriceMA100 || showPriceMean || showDivMean || showHreeMean || showHreePrice_Price) {
         showStocks = true;
      }  else {
         showStocks = false;
      }
   }
   public boolean getShowBitFractions() {
      return showBitFractions;
   }
   public void setShowBitFractions(boolean val) {
      showBitFractions = val;
   }
   public boolean getShowWealthClassifierAgents() {
      return showWealthClassifierAgents;
   }
   public void setShowWealthClassifierAgents(boolean val) {
      showWealthClassifierAgents = val;
   }
   public boolean getShowWealthNoClassifierAgents() {
      return showWealthNoClassifierAgents;
   }
   public void setShowWealthNoClassifierAgents(boolean val) {
      showWealthNoClassifierAgents = val;
   }

   public boolean getShowWealthTechnicalTraders() {
      return showWealthTechnicalTraders;
   }
   public void setShowWealthTechnicalTraders(boolean val) {
      showWealthTechnicalTraders = val;
   }
   public boolean getShowWealthFundamentalTraders() {
      return showWealthFundamentalTraders;
   }
   public void setShowWealthFundamentalTraders(boolean val) {
      showWealthFundamentalTraders = val;
   }

   public boolean getShowWealthNormalLearner() {
      return showWealthNormalLearner;
   }
   public void setShowWealthNormalLearner(boolean val) {
      showWealthNormalLearner = val;
   }
   public boolean getShowWealthFastLearner() {
      return showWealthNormalLearner;
   }
   public void setShowWealthFastLearner(boolean val) {
      showWealthNormalLearner = val;
   }
   public boolean getShowWealthSFIAgents() {
      return showWealthSFIAgents;
   }
   public void setShowWealthSFIAgents(boolean val) {
      showWealthSFIAgents = val;
   }
   public boolean getShowWealthNESFIAgents() {
      return showWealthNESFIAgents;
   }
   public void setShowWealthNESFIAgents(boolean val) {
      showWealthNESFIAgents = val;
   }
   public boolean getShowWealthZeroBitAgents() {
      return showWealthZeroBitAgents;
   }
   public void setShowWealthZeroBitAgents(boolean val) {
      showWealthZeroBitAgents = val;
   }
   public boolean getShowWealthNonZeroBitAgents() {
      return showWealthNonZeroBitAgents;
   }
   public void setShowWealthNonZeroBitAgents(boolean val) {
      showWealthNonZeroBitAgents = val;
   }
   public boolean getShowBaseWealth() {
      return showBaseWealth;
   }
   public void setShowBaseWealth(boolean val) {
      showBaseWealth = val;
   }
   public boolean getShowLongTermHreeBaseWealth() {
      return showLongTermHreeBaseWealth;
   }
   public void setShowLongTermHreeBaseWealth(boolean val) {
      showLongTermHreeBaseWealth = val;
   }
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
         "updateFrequency"
      };
   }  // getProbedProperties()

}  // ObserverOptions()

