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
   private Stock stock;
   private LMSRStock stockLMSR;
   private double minExcess = 0.1;
   // private double eta = 0.0005;  // for price-adjustment
   protected static final int MAXITERATIONS = 10;
   private final double MAXPRICE = 200;
   private final double MINPRICE = 0.1;
   private double slopeTotal, bidTotal, offerTotal, imbalance;
   public static double bidFrac, offerFrac, volume;
   public static double volumeNeg, volumePos;

   protected static double reF; // holds parameters f for the hree-mode for each stock
   protected static double reG; // holds parameters g for the hree-mode
   protected static double reA; // holds parameters a for the hree-mode (for agents though, should be held in TradingRule, for memory reason just calculated and stored once here)
   protected static double reB; // holds parameters b for the hree-mode (for agents though, should be held in TradingRule, for memory reason just calculated and stored once here)

   public static final int SLOPESPECIALIST = 0 ;
   public static final int RESPECIALIST = 4 ;
   // the next three specialist types that were sometimes used in the DFI-ASM have not
   // been implemented yet.
   public static final int FIXEDETASPECIALIST = 1 ;
   public static final int ADAPTIVEETASPECIALIST = 2 ;
   public static final int VCVARSPECIALIST = 3 ;
   public static final int LMSRSPECIALIST = 5 ;

   public static int type = SLOPESPECIALIST ;

   public Specialist() { // constructor
      World.numberOfAgents = World.numberOfSFIAgents + World.numberOfNESFIAgents + World.numberOfLMSRAgents;
      tradeMatrix = new double[World.numberOfAgents][2];

      /* Now determine the hree-forecast parameters. They are highly on the dividend process and agent
         forecast types. To figure out how these formulas were are actually derived, please
         refer to the appendix.
      */

      stockLMSR = World.LMSRStocks;
      stock = World.Stocks;
      reF = stock.getRho()/(World.interestRatep1-stock.getRho());
      reG = ( (1 + reF)*(stock.getDividendMean()*(1-stock.getRho())-Agent.riskAversion*stock.getNoiseVar()*(1 + reF) ) ) / World.interestRate;
      reA = stock.getRho();
      reB = (1-reA)*((1+reF)*stock.getDividendMean()+reG);
      // System.out.println("f= "+reF+ "   g= "+reG+ "   a= "+reA+ "   b= "+reB);
   }  // end of constructor

   public double getCostLMSR(double order, boolean pos) { // calculates cost function, used for price setting
      double costFunction;
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

      costFunction = stockLMSR.getBLiq()*Math.log(Math.exp((stockLMSR.getQPosLMSR()+orderpos)/stockLMSR.getBLiq())+Math.exp((stockLMSR.getQNegLMSR()+orderneg)/stockLMSR.getBLiq()));

      return costFunction;
   }

   public void adjustPricePrediction() { // different price adjustment for LMSR, determined by cost function
      Agent agent;
      int iteration;
      boolean done;
      double priceLMSR = 0; // = new double[World.differentStocks]; //mudar
      type = LMSRSPECIALIST; //mudar

      iteration = 0;
      done = false;
      stockLMSR = World.LMSRStocks;

      for (int i = 0 ; i < World.numberOfLMSRAgents ; i++) {
         agent = World.Agents[i];
         priceLMSR = getCostLMSR(1, true) - getCostLMSR(0, true);
         stockLMSR.setPrice(priceLMSR);
         stock.setPrice(priceLMSR); //mudar // o display usa esse preço
         agent.setDemandAndSlope(priceLMSR);
         if (agent.pos) {
            tradeMatrix[i][0] = agent.getDemand();
            stockLMSR.setQPosLMSR(tradeMatrix[i][0]);
            volumePos += tradeMatrix[i][0];
         } else {
            tradeMatrix[i][0] = agent.getDemand();
            stockLMSR.setQNegLMSR(tradeMatrix[i][0]);
            volumeNeg += tradeMatrix[i][0];
         }

         volume += tradeMatrix[i][0]; // mudar

      }  // while

      stockLMSR.setPrice(priceLMSR);
      stockLMSR.setTradingVolume(volume); // mudar
      // System.out.println(volume);
   }  // adjustPricePrediction


   public void adjustPrice() {
      if (AsmModel.LMSR) { //changes price setting in case LMSR is chosen in GUI
         adjustPricePrediction();
      } else {

         Agent agent;
         int iteration;
         boolean done;
         double trialPrice = 0; // = new double[World.differentStocks];
         double rePrice;   // = new double[World.differentStocks];

         iteration = 0;
         done = false;
         stock = World.Stocks;
         rePrice = reF * (stock.getDividend()) + reG;
         while (iteration < MAXITERATIONS && !done) {
            switch (type) {
               case RESPECIALIST:
                  trialPrice = rePrice;
                  done = true;
                  break;
               case SLOPESPECIALIST:
                  if (iteration == 0) {
                     trialPrice = stock.getPrice();
                  } else {
                     trialPrice -= imbalance / slopeTotal;
                  }
                  iteration++;
                  break;
               case FIXEDETASPECIALIST:
                  break;
               default:
                  break;
            }
            // now clip trialPrice
            if (trialPrice < MINPRICE) trialPrice = MINPRICE;
            if (stock.getDividendProcess() != Stock.RANDOMWALK) {
               if (trialPrice > MAXPRICE) trialPrice = MAXPRICE;
            }
            // Get each agent's requests and sum up bids, offers, and slopes
            bidTotal = 0.0;
            offerTotal = 0.0;
            slopeTotal = 0.0;
            for (int i = 0; i < World.numberOfAgents; i++) {
               agent = World.Agents[i];
               agent.setDemandAndSlope(trialPrice);
               tradeMatrix[i][0] = agent.getDemand();
               tradeMatrix[i][1] = agent.getSlope();
               if (tradeMatrix[i][0] > 0) {
                  bidTotal += tradeMatrix[i][0];
               } else {
                  offerTotal -= tradeMatrix[i][0];
               }
               slopeTotal += tradeMatrix[i][1];
            }
            imbalance = bidTotal - offerTotal;
            // System.out.println("Bids: "+bidTotal+"  Offers: "+offerTotal+"  Imbalance: "+imbalance+"  SlopeTotal: "+slopeTotal);
            if (imbalance <= minExcess && imbalance >= -minExcess) {
               done = true;
               break;
            }
         }  // while
         // Match up the bids and offers, this tertiary operator is quite slow in JAVA, could make it faster with if-clauses
         //         volume = (bidTotal > offerTotal ? offerTotal : bidTotal);
         //         bidFrac = (bidTotal > 0.0 ? volume / bidTotal : 0.0);
         //         offerFrac = (offerTotal > 0.0 ? volume / offerTotal : 0.0);
         if (bidTotal > offerTotal) {
            volume = offerTotal;
         } else {
            volume = bidTotal;
         }
         if (bidTotal > 0.0) {
            bidFrac = volume / bidTotal;
         } else {
            bidFrac = 0.0;
         }
         if (offerTotal > 0.0) {
            offerFrac = volume / offerTotal;
         } else {
            offerFrac = 0.0;
         }
         stock.setRePrice(rePrice);    //hreePrice has to be set before normal price because of updatePriceHistory
         stock.setPrice(trialPrice);
         stock.setTradingVolume(volume);
         // System.out.println(volume);
      }  // adjustPrice

   }

}
