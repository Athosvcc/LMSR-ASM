/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the ExecutePeriod Class
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
   private static Stock stock;
   private static LMSRStock stockLMSR;

   public ExecutePeriod() {  // constructor
   }


   public static void execute() {
      if (AsmModel.LMSR) {
         Specialist specialist = AsmModel.specialist;
         System.out.println("PERIODO: " + World.period);
         stockLMSR = World.LMSRStocks;
         if (World.period == 0) {
            int qInitial = stockLMSR.qInitLMSR(stockLMSR.getInitialPrice());
            if (stockLMSR.getInitialPrice()<0.5) {
               stockLMSR.setQNegLMSR(qInitial);
            } else {
               stockLMSR.setQPosLMSR(qInitial);
            }
            World.period++;
         } else if (World.period == stockLMSR.periodShock) {
            stockLMSR.probShock();
            World.period++;
            System.out.println("Wealth: " + agent.getWealth());
         } else if (World.period == World.numberOfPeriods-1) { // pays out agent investments
            double totalWealth = 0;
            for (int j = 0; j < World.numberOfLMSRAgents; j++) {
               agent = World.Agents[j];
               agent.setPayout();
               System.out.println("Wealth: " + agent.getWealth());
               totalWealth += agent.getWealth();
            }
            System.out.println("Revenue: " + specialist.getSpecialistRevenue());
            System.out.println("Payout: " + specialist.getSpecialistPayout());
            System.out.println("MM Loss: " + (specialist.getSpecialistRevenue()-specialist.getSpecialistPayout()));
            System.out.println("totalWealth: " + totalWealth);
         } else {
            double totalWealth = 0;
            World.period++;       // initial values for period 0 are set and shouldn't be altered anymore
            for (int j = 0; j < World.numberOfLMSRAgents; j++) {
               agent = World.Agents[j];
               agent.chooseRule();        // abstract in agent.java
            }    // for all agents
            AsmModel.specialist.adjustPrice();  // specialist gets market maker price for 1 stock
            for (int j = 0; j < World.numberOfLMSRAgents; j++) {
               agent = World.Agents[j];
               agent.executeOrder();    // updates money, stockPosition and wealth of agents
               agent.getEarningsAndPayTaxes();
               totalWealth += agent.getWealth();
               System.out.println("Wealth: " + agent.getWealth());
            }        // for all agents
            World.setTotalWealth(totalWealth);
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
            System.out.println("totalYesStocks: " + stockLMSR.getQPosLMSR());
            System.out.println("totalNoStocks: " + stockLMSR.getQNegLMSR());
         }
      } else { // end of LMSR execution

         double totalWealth = 0;
         World.period++;       // initial values for period 0 are set and shouldn't be altered anymore
         stock = World.Stocks;
         stock.updateDividend(); // new dividend announced, updating of all statistics and state-of-the-stock
         Agent.activatedRules = 0;  // for debugging, how many rules are activated in total in this period
         NESFIAgent.NESFIActivatedRules = 0;
         SFIAgent.SFIActivatedRules = 0;
         for (int j = 0; j < World.numberOfAgents; j++) {
            agent = World.Agents[j];
            if (agent.isFastLearner()) {
               if (World.period > World.firstGATime && (Random.uniform.nextDouble() < 1d / World.gaIntervalFastLearner) && !AsmModel.hree) {
                  agent.invokeGA();
               }
            } else {
               if (World.period > World.firstGATime && (Random.uniform.nextDouble() < World.gaProb) && !AsmModel.hree) {
                  agent.invokeGA();
               }
            }
            agent.chooseRule();        // abstract in agent.java
         }    // for all agents

         //      System.out.println("T-Bits in use: "+ World.getTechnicalBits() + "  T-Fraction: "+World.getWorldTBitFraction());
         //      System.out.println("F-Bits in use: "+ World.getFundamentalBits() + "  F-Fraction: "+World.getWorldFBitFraction());

         AsmModel.specialist.adjustPrice();  // specialist tries to find a market clearing price for stock i
         for (int j = 0; j < World.numberOfAgents; j++) {
            agent = World.Agents[j];
            agent.executeOrder();    // updates money, stockPosition and wealth of agents
            agent.getEarningsAndPayTaxes();
            totalWealth += agent.getWealth();
            if (!AsmModel.hree) agent.updatePerformance();
         }        // for all agents
         World.setTotalWealth(totalWealth);
         World.detBaseWealth();  // determines BaseWealth for inactivity (always holding his initial endowment of stock) for an imaginary agent
         if ((ObserverOptions.calculateCorrelations && AsmModel.showDisplays) || (AsmModel.recordData && AsmModel.recorderOptions.getCalculateCorrelations())) {
            AsmModel.world.detCorrelations();
         }
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
      }
   }  // execute()


   static void graphDisplay() {
         displayPeriod++;

         if(AsmModel.observer.showStocks) {
            AsmModel.priceGraph.record();
            if(AsmModel.observer.getShowHreePrice_Price()) {
               AsmModel.hreePrice_PriceGraph.record();
            }
         }
         if(AsmModel.observer.showLogReturns && (World.period > World.firstPeriodForShowingLogs)) {
            AsmModel.logReturnGraph.record();
         }
         if(AsmModel.observer.getShowVolume() && (World.period > 2)) {
            AsmModel.volumeGraph.record();
         }

         if(AsmModel.observer.getShowWealthClassifierAgents() ) {
            AsmModel.wealthGraph.record();
         } else if (AsmModel.observer.getShowWealthFundamentalTraders() ) {
            AsmModel.wealthGraph.record();
         } else if (AsmModel.observer.getShowWealthNoClassifierAgents() ) {
            AsmModel.wealthGraph.record();
         } else if (AsmModel.observer.getShowWealthTechnicalTraders() ) {
            AsmModel.wealthGraph.record();
         } else if (AsmModel.observer.getShowWealthFastLearner() ) {
            AsmModel.wealthGraph.record();
         } else if(AsmModel.observer.getShowWealthSFIAgents() ) {
            AsmModel.wealthGraph.record();
         } else if(AsmModel.observer.getShowWealthNESFIAgents() ) {
            AsmModel.wealthGraph.record();
         } else if(AsmModel.observer.getShowWealthNormalLearner() ) {
            AsmModel.wealthGraph.record();
         } else if(AsmModel.observer.getShowWealthNonZeroBitAgents() ) {
            AsmModel.wealthGraph.record();
         }  else if(AsmModel.observer.getShowWealthZeroBitAgents() ) {
            AsmModel.wealthGraph.record();
         }  else if(AsmModel.observer.getShowBaseWealth() ) {
            AsmModel.wealthGraph.record();
         }  else if(AsmModel.observer.getShowLongTermHreeBaseWealth() ) {
            AsmModel.wealthGraph.record();
         }  // there seemed to be a limitation of how many conditions can be evaluated at once

         if (displayPeriod == updateDisplayPeriod ) { // updating the display at longer time intervall, saves time
            if(AsmModel.observer.showStocks) {
               AsmModel.priceGraph.updateGraph();
               if(AsmModel.observer.getShowHreePrice_Price()) {
                  AsmModel.hreePrice_PriceGraph.updateGraph();
               }
            }
            if(AsmModel.observer.showLogReturns && (World.period > World.firstPeriodForShowingLogs) ) {
               AsmModel.logReturnGraph.updateGraph();
            }
            if(AsmModel.observer.getShowVolume() && (World.period > 2)) {
               AsmModel.volumeGraph.updateGraph();
            }
            if(AsmModel.observer.getShowPriceValueCorr()) {
               AsmModel.correlationGraph.step();
            }
            if(AsmModel.observer.getShowTechnicalBits() || AsmModel.observer.getShowFundamentalBits() || AsmModel.observer.getShowBitFractions() ) {
               AsmModel.bitUsageGraph.step();
            }
            if(AsmModel.observer.getShowWealthClassifierAgents() || AsmModel.observer.getShowWealthFundamentalTraders() || AsmModel.observer.getShowWealthNoClassifierAgents() || AsmModel.observer.getShowWealthTechnicalTraders() || AsmModel.observer.getShowWealthNormalLearner() || AsmModel.observer.getShowWealthFastLearner() || AsmModel.observer.getShowWealthSFIAgents() || AsmModel.observer.getShowWealthNESFIAgents() || AsmModel.observer.getShowWealthNonZeroBitAgents() || AsmModel.observer.getShowWealthZeroBitAgents() || AsmModel.observer.getShowBaseWealth() || AsmModel.observer.getShowLongTermHreeBaseWealth()) {
               AsmModel.wealthGraph.updateGraph();
            }

            displayPeriod = 0;
         }	// updating Displays
   }  // graphDisplays



}

