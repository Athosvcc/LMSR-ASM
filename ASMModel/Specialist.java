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

import java.math.BigDecimal;

/**
 * The specialist handles market clearing.
 */
class Specialist {

   private static double[][] tradeMatrix;       // holds individual demands
   private LMSRStock stockLMSR;
   public static double volumeNeg, volumePos;
   public static double specialistRevenue = 0;
   public static double specialistPayout = 0;
   public static double specialistLiabilities = 0;
   protected static final int SELECT_2DIGITS = 0;
   protected static final int SELECT_6DIGITS = 1;
   protected static final int SELECT_FLOAT = 2;
   public static int selectionMethod = SELECT_2DIGITS;


   public Specialist() { // constructor
      World.numberOfAgents = World.numberOfLMSRAgents;
      tradeMatrix = new double[World.numberOfAgents][2];
      stockLMSR = World.LMSRStocks;
   }  // end of constructor

   public double getCostLMSR(double order, boolean pos) { // calculates cost function, used for price setting
      double costFunction;
      double costFunctionPost;
      double orderCost;
      double orderPos;
      double orderNeg;
      stockLMSR = World.LMSRStocks;

      if (pos) {
         orderNeg = 0;
         orderPos = order;
      } else {
         orderNeg = order;
         orderPos = 0;
      }

      costFunction = stockLMSR.getBLiq()*Math.log(Math.exp((stockLMSR.getQPosLMSR())/stockLMSR.getBLiq())+Math.exp((stockLMSR.getQNegLMSR())/stockLMSR.getBLiq()));
      costFunctionPost = stockLMSR.getBLiq()*Math.log(Math.exp((stockLMSR.getQPosLMSR()+orderPos)/stockLMSR.getBLiq())+Math.exp((stockLMSR.getQNegLMSR()+orderNeg)/stockLMSR.getBLiq()));
      orderCost = costFunctionPost - costFunction;

      switch(selectionMethod){
         case SELECT_2DIGITS: // rounds to nearest two digits
            if (order > 0) { // rounds the cost up when buying and down when selling
               BigDecimal bd = new BigDecimal(orderCost).setScale(2, BigDecimal.ROUND_UP);
               orderCost = bd.doubleValue();
            } else {
               BigDecimal bd = new BigDecimal(orderCost).setScale(2, BigDecimal.ROUND_DOWN);
               orderCost = bd.doubleValue();
            }
            break;
         case SELECT_6DIGITS: // rounds to nearest six digits
            if (order > 0) { // rounds the cost up when buying and down when selling
               BigDecimal bd = new BigDecimal(orderCost).setScale(6, BigDecimal.ROUND_UP);
               orderCost = bd.doubleValue();
            } else {
               BigDecimal bd = new BigDecimal(orderCost).setScale(6, BigDecimal.ROUND_DOWN);
               orderCost = bd.doubleValue();
            }
            break;
         case SELECT_FLOAT: // uses float values
            break;
      }


      return orderCost;
   }

   public double getLastPriceLMSR(double order, boolean pos, boolean buy) { // calculates price of the last stock in an order
      double priceStock;
      if (buy) {
         priceStock = getCostLMSR(order, pos) - getCostLMSR(order-1, pos);
      } else {
         priceStock = getCostLMSR(order-1, pos) - getCostLMSR(order, pos);
      }
      return priceStock;
   }

   public void adjustPricePrediction() { // price adjustment for LMSR, determined by cost function
      Agent agent;
      double priceLMSR = 0;
      double priceYesLMSR = 0;
      double priceNoLMSR = 0;

      stockLMSR = World.LMSRStocks;

      for (int i = 0 ; i < World.numberOfLMSRAgents ; i++) {
         if (AsmModel.LS_LMSR) {
            stockLMSR.liquiditySensitiveB(stockLMSR.getAlphaLS(), stockLMSR.getQPosLMSR(), stockLMSR.getQNegLMSR());
            // System.out.println("BLS: " + stockLMSR.getBLiq());
         }
         agent = World.Agents[i];
         priceLMSR = getCostLMSR(1, true) - getCostLMSR(0, true);
         stockLMSR.setPrice(priceLMSR);
         agent.setDemandAndSlope();
         if (agent.pos) {
            tradeMatrix[i][0] = agent.getDemand();
            volumePos += tradeMatrix[i][0];
            stockLMSR.setTradingVolumeYes(volumePos);
         } else {
            tradeMatrix[i][0] = agent.getDemand();
            volumeNeg += tradeMatrix[i][0];
            stockLMSR.setTradingVolumeNo(volumeNeg);
         }
      }  // while
      priceYesLMSR = getCostLMSR(1, true) - getCostLMSR(0, true);
      priceNoLMSR = getCostLMSR(1, false) - getCostLMSR(0, false);
      stockLMSR.setPriceSum(priceYesLMSR + priceNoLMSR);
      stockLMSR.setPrice(priceYesLMSR);
      stockLMSR.setPriceNoStock(priceNoLMSR);
//      if (stockLMSR.getLiquiditySensitive()) {
//         System.out.println("BLS: " + stockLMSR.getBLiq());
//      }
   }  // adjustPricePrediction


   public void adjustPrice() {
      adjustPricePrediction();
   }

   public void setSpecialistRevenue(double val) { specialistRevenue += val; }
   public double getSpecialistRevenue() { return specialistRevenue; }
   public void setSpecialistPayout(double val) { specialistPayout += val; }
   public double getSpecialistPayout() { return specialistPayout; }
   public void setSpecialistLiabilities() {
      stockLMSR = World.LMSRStocks;
      if (stockLMSR.probability > 0.5) {
         specialistLiabilities = stockLMSR.getQPosLMSR() - stockLMSR.getQPosInitial(); // Market Maker doesn't pay for initial stocks
      } else {
         specialistLiabilities = stockLMSR.getQNegLMSR() - stockLMSR.getQNegInitial(); // Market Maker doesn't pay for initial stocks
      }
   }
   public double getSpecialistLiabilities() { return specialistLiabilities; }
   public double getSpecialistProfit() { return getSpecialistRevenue() - getSpecialistLiabilities(); }
   public void setResetSpecialist() {
       specialistLiabilities = 0;
       specialistRevenue = 0;
   }
}
