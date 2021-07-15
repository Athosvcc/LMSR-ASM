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

import java.util.ArrayList;
import uchicago.src.sim.util.Random;

import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;

/**
 * There is one world object created in the buildModel() method. It holds certain
 * parameters that describe the economic environment of the model, e.g., interest
 * rate, periods to run, how many agents of what kind, etc. It also has some methods
 * to gather and report data that make only sense on a "world" level.
 */
class World {

   // static class variables
   public static double interestRate = 0.0;
   public static double interestRatep1 = interestRate + 1.0;
   public static int period = 0 ;

   public static ASMModel.Agent[] Agents ;
   public static LMSRStock LMSRStocks;

   public static int numberOfLMSRAgents = 25;
   public static int numberOfAgents = numberOfLMSRAgents;
   private static double baseWealth = 0;

   public static int numberOfPeriods = 200 ;
   private static double totalWealth = 0 ;

   public World() { // constructor
   }  // constructor

   public static void createAgents() {
      Agents = new Agent[numberOfLMSRAgents];
      for(int i = 0; i< numberOfLMSRAgents; i++) {
         Agent newLMSRAgent = new LMSRAgent(0);
         Agents[i] = newLMSRAgent;
      }
      baseWealth = Agents[0].cash; // assume that all agents have same endowments; then, baseWealth is the wealth due to inactivity, i.e., an agents does not trade and holds his initial endowment of one unit of stock.
      System.gc();
      if (AsmModel.recordData && World.period>0)
         AsmModel.recorder.record();
   }	// createAgents()

   protected static double getTotalWealth() {
      return totalWealth;
   }

   public static void setTotalWealth(double wealth) {
      totalWealth = wealth;
   }

   protected void finalize() throws Throwable {
      period = 0 ;
      totalWealth = 0;
      super.finalize();
   }  // finalize

   public static double getAverageWealth() {
      int totalWealth = 0;
        for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
          totalWealth += Agents[i].getWealth() ;
        }
      // System.out.println("Average Wealth of agents in the economy :"+totalWealth/World.numberOfAgents );
      return totalWealth/World.numberOfAgents;
   }  // getAverageWealth

   protected static double getBaseWealth() {
      return baseWealth;
   }
   protected static void setBaseWealth(double val) {
      baseWealth = val;
   }



}