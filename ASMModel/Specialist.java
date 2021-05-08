/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the Specialist Class
*/

package ASMModel;

import java.math.BigDecimal;

/**
 * The specialist handles market clearing. There is one instance created and the object
 * finds a market clearing price. From the various types of the original SFI-ASM,
 * only the hree-specialist and the slope specialist have been implemented.
 */
class Specialist {

   private static double[][] tradeMatrix;       // holds individual demands and slopes of individual agents
   private LMSRStock stockLMSR;
   private double minExcess = 0.1;
   // private double eta = 0.0005;  // for price-adjustment
   protected static final int MAXITERATIONS = 10;
   public static double volumeNeg, volumePos;
   public static double specialistRevenue = 0;
   public static double specialistPayout = 0;
   public static double specialistLiabilities = 0;
   protected static final int SELECT_2DIGITS = 0;
   protected static final int SELECT_6DIGITS = 1;
   protected static final int SELECT_FLOAT = 2;
   public static int selectionMethod = SELECT_2DIGITS;

   public static final int LMSRSPECIALIST = 0 ;


   public Specialist() { // constructor
      World.numberOfAgents = World.numberOfLMSRAgents;
      tradeMatrix = new double[World.numberOfAgents][2];

      /* Now determine the hree-forecast parameters. They are highly on the dividend process and agent
         forecast types. To figure out how these formulas were are actually derived, please
         refer to the appendix.
      */

      stockLMSR = World.LMSRStocks;
      // System.out.println("f= "+reF+ "   g= "+reG+ "   a= "+reA+ "   b= "+reB);
   }  // end of constructor

   public double getCostLMSR(double order, boolean pos) { // calculates cost function, used for price setting
      double costFunction;
      double costFunctionPost;
      double orderCost;
      double orderpos;
      double orderneg;
      stockLMSR = World.LMSRStocks;

      if (pos) {
         orderneg = 0;
         orderpos = order;
      } else {
         orderneg = order;
         orderpos = 0;
      }

      costFunction = stockLMSR.getBLiq()*Math.log(Math.exp((stockLMSR.getQPosLMSR())/stockLMSR.getBLiq())+Math.exp((stockLMSR.getQNegLMSR())/stockLMSR.getBLiq()));
      costFunctionPost = stockLMSR.getBLiq()*Math.log(Math.exp((stockLMSR.getQPosLMSR()+orderpos)/stockLMSR.getBLiq())+Math.exp((stockLMSR.getQNegLMSR()+orderneg)/stockLMSR.getBLiq()));
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

   public void adjustPricePrediction() { // price adjustment for LMSR, determined by cost function
      Agent agent;
      int iteration;
      boolean done;
      double priceLMSR = 0;
      double priceYesLMSR = 0;
      double priceNoLMSR = 0;

      iteration = 0;
      done = false;
      stockLMSR = World.LMSRStocks;

      for (int i = 0 ; i < World.numberOfLMSRAgents ; i++) {
         if (AsmModel.LS_LMSR) {
            stockLMSR.liquiditySensitiveB(stockLMSR.getAlphaLS(), stockLMSR.getQPosLMSR(), stockLMSR.getQNegLMSR());
            // System.out.println("BLS: " + stockLMSR.getBLiq());
         }
         agent = World.Agents[i];
         priceLMSR = getCostLMSR(1, true) - getCostLMSR(0, true);
         stockLMSR.setPrice(priceLMSR);
         agent.setDemandAndSlope(priceLMSR);
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
}
