/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the SFIAgent Class
*/


package ASMModel;

import java.awt.Color;
import java.util.*;
import java.lang.*;
import java.io.*;

import uchicago.src.sim.space.*;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.engine.CustomProbeable;

/**
 * Some methods that differ between agents are invokeGA() or chooseRule(), for instance.
 * Thus, they are overwritten here.
 */
public class SFIAgent extends Agent implements CustomProbeable {

   protected static int minActiveRules = 1;
   protected static int newTR = 20;
   protected static int numRulesSFIAgent = 100;
   protected static int SFIActivatedRules = 0;
   protected static int SFISelectAverageCounter = 0;

   public SFIAgent(int traderType) {
      SFISelectAverageCounter = 0;
      nesfiAgent = false;
      useClassifier = true;   // use of classifier system or no condition checking
      if(traderType>-1) { // do the rest only for real agents, not for the "static" agent to set parameters from the GUI
         numRules = numRulesSFIAgent;
         traders++;
         this.ID = traders;
         initializeXBits();
         ruleSet = new TradingRule[numRules];   // holds all rules for the agent, a set for each stock
         // holds ID's for activated rules in ruleSet. It's filled each period starting from index zero with the activated rules, all remaining indices are set to -1.
         activeRuleSet = new int[numRules+1];
         oldRuleSet = new int[numRules+1];
//         World.increaseNumberOfClassifierAgents();
         useClassifier = true;
         if(instancesOfTechnicians < World.getNumberOfTechnicians()) {
               techTrader = true;
               instancesOfTechnicians++;
         } else {
            techTrader = false;
         }
         cash = initialCash;
         numberOfStocks = 1;    // initial endowment of stocks
         stock = World.Stocks;   //(Stock)World.stockList.get(type);
         stock.totalSupply += 1;
         wealth = cash + stock.getPrice();
         World.setTotalWealth(World.getTotalWealth()+wealth);
         for (int i = 0; i < numRules; i++) {   // create a set of numRules TradingRules
            ruleSet[i] = new TradingRule(useClassifier, techTrader, checkRules);
         }
         activeRuleSet[0] = -1;
         oldRuleSet[0] = -1 ;
         World.numRules += numRules;
         fProb = getAgentFBitFraction();
         tProb = getAgentTBitFraction();
      } else {
         staticAgent = true;
      }
   }  // constructor


   /**
    * Select the better parent from two arbitrarily chosen parents, provided that the parent had
    * been activated in its lifetime.
    * This is the tournament selection mechanism used by the the original SFI-Model. Yet,
    * the original tournament mechansim as suggested by Wetzel (1983) uses roulette wheel
    * selection to chose the two candidates, from which the one with higher fitness is
    * selected.
    *
    */
   protected int tournament() {
      int p1, p2, trial;
      trial = 0;
      do p1 = Random.uniform.nextIntFromTo(0,numRules-1);
         while ( (ruleSet[p1].birth == ruleSet[p1].lastActive) && trial++ < Specialist.MAXITERATIONS) ;
      trial = 0;
      do { p2=Random.uniform.nextIntFromTo(0,numRules-1); }
         while ( (ruleSet[p1].birth == ruleSet[p1].lastActive || p1==p2)  && trial++ < Specialist.MAXITERATIONS);
         /* The procedure above selects primarily those rules which have been activated at all.
            If there are only a few ones chosen, those get selected for reproduction over and
            over again.
            If rules are chosen according to fitness no matter how often thy have been activated,
            we allow for more variety in the selection process. The latter procedure is done in
            the non-SFI- version of the tournament selector.
         */
         //      p1 = Random.uniform.nextIntFromTo(0,numRules-1);
         //      p2=Random.uniform.nextIntFromTo(0,numRules-1);
      if(ruleSet[p1].fitness > ruleSet[p2].fitness) {
         return p1;
      } else {
         return p2;
      }
   }  // SFI-tournament


   /**
    * Performs the GA on all rules in ruleSet of the agent.
    */
   protected void invokeGA() {
      int parent1, parent2, trial, unUsed, temp ;
      TradingRule offspring;
      gaCount++;
      /* ruleSet ordered in ascending order. I don't bother to do it in descending order, so
         I just reverse the order of position-indeces.
         The ordering is in a lexicographic fashion. Rules which haven't been activated
         for more than maxNonActive are considered lowest, even though their fitness might
         be above average. Thus, those rules can be easily recognized for the generalization
         procedure.
      */
      Arrays.sort(ruleSet);
      medFitness = ruleSet[numRules/2].fitness;
      meanFitness = 0d;
      meanVariance = 0d;
      for (int i = 0; i < numRules; i++) {
         meanFitness += ruleSet[i].fitness ;
         meanVariance += ruleSet[i].forecastVar;
      }
      meanFitness /= numRules;
      meanVariance /= numRules;
      temp = numRules -1;
      unUsed=0;
      /* The sorting procedure has sorted all rules, that have not been matched for maxNonActive
         at the end of the ruleSet-array. Those rules are first generalized. The rule set is
         resorted afterwards.
      */
      while (temp >=0 && (World.period - ruleSet[temp--].lastActive) > TradingRule.maxNonActive) {
         unUsed++;
         ruleSet[temp+1].generalize(meanFitness);
      }
      Arrays.sort(ruleSet);   // we have generalized and changed some fitnesses, hence sort again
//       System.out.println("Agent "+ID+":  Unused :"+unUsed);

      /* pretending there were no active rules in the previous period for checking the
         forecast-accuracy. By changing the order of the ruleSet and by creating and deleting
         rules, the oldruleSet is obsolet, anyway.
      */
      oldRuleSet[0]= -1;

      for (int i = 0; i < newTR ; i++ ) {
         parent1 = tournament();
         trial=0;
         if (Random.uniform.nextDoubleFromTo(0d,1d) < probCrossover) {
            do {
               parent2=tournament();
            } while (parent1==parent2 && trial++ < Specialist.MAXITERATIONS);
            offspring = crossover(parent1, parent2);
         } else { // no crossover, but mutation
             offspring = copyRule(parent1);
             offspring.mutateSFI(meanVariance);  // give medianstrength as parameter
         }
         // now insert it into ruleset on order to replace a bad rule, the bad rules have low position indeces
         ruleSet[i] = offspring;
      }  // all newTR created, initialized and inserted into ruleSet
      fProb = getAgentFBitFraction();  // after GA is completed, calculate fBitFraction and tBitFraction
      tProb = getAgentTBitFraction();  // they won't change until the next GA-call
   }  // invokeGA

   /** Returns the index number of the selected rule in ruleSet[].
    *  First, the method determineActiveRules is called in order to get an array with all
    *  indices of active rules. According to the choosen selection method (SELECT_BEST,
    *  SELECT_AVERAGE, or SELECT_ROULETTE), a particular is activated by returning the index
    *  in ruleSet of that particular rule.
    *
   */
   protected void chooseRule() {
      boolean done = false;
      selectedAverageOld = selectedAverage;
      selectedAverage = false;
      int selected = 0;
      int j = 0;
      double maxFitness = 0.0;
      if(useClassifier) {
         while (activeRuleSet[j]>=0) {
            oldRuleSet[j] = activeRuleSet[j++];
         }
         oldRuleSet[j] = -1;  // stop-condition for performance-updating
         activeRules = determineActiveRules(); // now determine the new activeRuleSet
         activatedRules += activeRules;   // updates # of activated rules for the economy
         SFIActivatedRules += activeRules; // updates # of activated rules for SFI-Agents
      } else { // no classifier mode
         for (int i = 0; i < numRules; i++) {
            activeRuleSet[i] = i; // activates all rules
         }  // for all rules
         activeRuleSet[numRules] = -1;  // -1 indicates the end of the list of activated rule; later used with while not -1
      }  // classifier mode or not ?
      if (selectionMethod==SELECT_ROULETTE && activeRules >= minActiveRules) {
         double cumFitness = 0f;
         j=0;
         while (activeRuleSet[j]>=0) {
            if (ruleSet[activeRuleSet[j++]].activeCounter >= TradingRule.minCount)
            cumFitness += ruleSet[activeRuleSet[j-1]].getFitness();
         }
         j=0;
         double x = Random.uniform.nextDoubleFromTo(0f,cumFitness);
         cumFitness = 0;
         while (activeRuleSet[j]>=0) {
            if (ruleSet[activeRuleSet[j++]].activeCounter >= TradingRule.minCount) {
               cumFitness += ruleSet[activeRuleSet[j-1]].getFitness();
               if (cumFitness > x ) {
                  selected = activeRuleSet[--j];
                  done = true;
                  break;
               }
            }
       }
      }  // select Roulette
      else if (selectionMethod==SELECT_BEST) {
         j=0;
         while (activeRuleSet[j]>=0) {
            if (ruleSet[activeRuleSet[j]].fitness > maxFitness && ruleSet[activeRuleSet[j]].activeCounter >= TradingRule.minCount) {
               maxFitness = ruleSet[activeRuleSet[j]].fitness;
               selected = activeRuleSet[j];
               done = true;
            }
            j++;
         }
      }  // Select Best
      //if (done && ruleSet[activeRuleSet[j]].activeCounter < TradingRule.minCount) {
      if (done && ruleSet[selected].activeCounter >= TradingRule.minCount) {
         ruleSet[selected].use();   // sets lastUsed and Counter
         pdCoeff = ruleSet[selected].getAij();
         offset = ruleSet[selected].getBij();
         fcVar = ruleSet[selected].getForecastVar();
      }
      if (!done) { // || selectionMethod == SELECT_AVERAGE || activeRuleSet[stockID][0] == -1) { // if select average or no rules match, use global means of all rules
         selectedAverage = true;
         selectAverageCounter++;
         SFISelectAverageCounter++;
         double a = 0f;
         double b = 0f;
         double sum = 0f;
         double sumVar = 0f;
         double weight = 0f;
         for (int i = 0; i < numRules; i++) {
            sum += weight = ruleSet[i].fitness;
            a += ruleSet[i].getAij()*weight;
            b += ruleSet[i].getBij()*weight;
            sumVar += ruleSet[i].getForecastVar();
         }  // for all rules
         pdCoeff = a/sum;
         offset = b/sum;
         fcVar = sumVar/numRules;
         done = true;
      }  // select average or no rules selected
      divisor = riskAversion * fcVar;
   }  // chooseRule

   public int getMinActiveRules() {
      return minActiveRules;
   }
   public void setMinActiveRules(int val) {
      minActiveRules = val;
   }
   public String[] getProbedProperties() {
      if (!staticAgent) {
               return new String[] {"ID","wealth","meanFitness","minFitness","maxFitness","fundamentalBitsSet","technicalBitsSet","cash","numberOfStock"};
         } else { // set static properties for all properties with this actual instance of a trader
             return new String[] {"numRules","initialCash","minCash","riskAversion","replaceXXRules","probCrossover","minActiveRules","checkRules"};
         }
   }
   public int getNumRules() {
      return this.numRulesSFIAgent;
   }
   public void setNumRules(int val) {
      numRulesSFIAgent = val;
      this.numRules = val;
   }

   public void setReplaceXXRules(int val) {
      newTR = val;
   }
   public int getReplaceXXRules() {
      return newTR;
   }
   public void setCheckRules(boolean val) {
      checkRules = val;
   }
   public boolean getCheckRules() {
      return checkRules;
   }

}