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

import uchicago.src.sim.util.Random;
import cern.jet.random.*;

/**
 * This class contains the action sequence within a period. The main method execute()
 * is called every period by the Repast scheduler.
 */
abstract class ExecutePeriod {
   private final static int updateDisplayPeriod = ObserverOptions.updateFrequency;
   private static int displayPeriod = 0;
   private static int recordPeriod = 0;
   private static Agent agent;
   private static LMSRStock stockLMSR;

   public ExecutePeriod() {  // constructor
   }


   public static void execute() {
      Specialist specialist = AsmModel.specialist;
//       System.out.println("PERIODO: " + World.period);
      stockLMSR = World.LMSRStocks;
      if (World.period == 0) { // at period 0, create artificial stocks necessary for initial price
         stockLMSR.baseQLMSR(stockLMSR.getInitialQuantity());
         int qInitial = stockLMSR.qInitLMSR(stockLMSR.getInitialPrice());
         if (stockLMSR.getInitialPrice()<0.5) {
            stockLMSR.setQNegLMSR(qInitial);
         } else {
            stockLMSR.setQPosLMSR(qInitial);
         }
         stockLMSR.setQPosInitial(stockLMSR.getQPosLMSR());
         stockLMSR.setQNegInitial(stockLMSR.getQNegLMSR());
         World.period++;
      } else if (World.period == stockLMSR.periodShock) { // if it's the shock period, change underlying probability
         stockLMSR.probShock();
         World.period++;
         // System.out.println("Wealth: " + agent.getWealth());
      } else if (World.period == World.numberOfPeriods-1) { // at the last period, pays out agent investments
         double totalWealth = 0;
         for (int j = 0; j < World.numberOfLMSRAgents; j++) {
            agent = World.Agents[j];
            agent.setPayout();
            // System.out.println("Wealth: " + agent.getWealth());
            totalWealth += agent.getWealth();
         }
         specialist.setSpecialistLiabilities();
//            System.out.println("Revenue: " + specialist.getSpecialistRevenue());
//            System.out.println("Payout: " + specialist.getSpecialistLiabilities());
//            System.out.println("MM Loss: " + (specialist.getSpecialistRevenue()-specialist.getSpecialistLiabilities()));
//            System.out.println("totalWealth: " + totalWealth);
      } else { // behavior in all other periods
         double totalWealth = 0;
         World.period++;       // initial values for period 0 are set and shouldn't be altered anymore
         stockLMSR.updateProbability();
         for (int j = 0; j < World.numberOfLMSRAgents; j++) {
            agent = World.Agents[j];
         }    // for all agents
         AsmModel.specialist.adjustPricePrediction();  // specialist gets market maker price for 1 stock
         for (int j = 0; j < World.numberOfLMSRAgents; j++) {
            agent = World.Agents[j];
            agent.getEarningsAndPayTaxes(); // sets agent wealth
            totalWealth += agent.getWealth();
            // System.out.println("Wealth: " + agent.getWealth());
         }        // for all agents
         World.setTotalWealth(totalWealth);
      specialist.setSpecialistLiabilities();
//            System.out.println("Revenue: " + specialist.getSpecialistRevenue());
//            System.out.println("Profit: " + specialist.getSpecialistProfit());
//            System.out.println("Liabilities: " + specialist.getSpecialistLiabilities());
         if (AsmModel.showDisplays) graphDisplay();
         if (AsmModel.recordData) {
            if (AsmModel.recorderOptions.getRecordAllFromPeriod() <= World.period && AsmModel.recorderOptions.getRecordAllToPeriod() >= World.period) {
               AsmModel.recorder.record();
            } else if (AsmModel.recorderOptions.getStartFromPeriod() <= World.period) {
               if ((recordPeriod == AsmModel.recorderOptions.getRecordFrequency()) || (recordPeriod == 0) || (AsmModel.recorderOptions.getRecordAllFromPeriod() >= World.period && AsmModel.recorderOptions.getRecordAllToPeriod() <= World.period)) { // just record data at the given frequency
                  AsmModel.recorder.record();
                  recordPeriod = 0;
               }
               recordPeriod++;
            }
         }
//            System.out.println("Yes Stocks: " + stockLMSR.getQPosLMSR());
//            System.out.println("No Stocks: " + stockLMSR.getQNegLMSR());
      }
   }  // execute()


   static void graphDisplay() {
         displayPeriod++;

         if(AsmModel.observer.showStocks) {
            AsmModel.priceGraph.record();
         }
         if(AsmModel.observer.getShowVolume() && (World.period > 2)) {
            AsmModel.volumeGraph.record();
         }
         if(AsmModel.observer.getShowMarketMakerRevenue() ) {
            AsmModel.MMGraph.record();
         } else if (AsmModel.observer.getShowMarketMakerLiabilities() ) {
            AsmModel.MMGraph.record();
         } else if (AsmModel.observer.getShowMarketMakerProfit() ) {
            AsmModel.MMGraph.record();
         }
         if(AsmModel.observer.getShowBLiq() ) {
            AsmModel.LMSRGraph.record();
         }

         if (displayPeriod == updateDisplayPeriod ) { // updating the display at longer time interval, saves time
            if(AsmModel.observer.showStocks) {
               AsmModel.priceGraph.updateGraph();
            }
            if(AsmModel.observer.getShowVolume() && (World.period > 2)) {
               AsmModel.volumeGraph.updateGraph();
            }
            if(AsmModel.observer.getShowMarketMakerRevenue() || AsmModel.observer.getShowMarketMakerLiabilities() || AsmModel.observer.getShowMarketMakerProfit() ) {
               AsmModel.MMGraph.updateGraph();
            }
            if(AsmModel.observer.getShowBLiq() ) {
               AsmModel.LMSRGraph.updateGraph();
            }

            displayPeriod = 0;
         }	// updating Displays
   }  // graphDisplays



}

