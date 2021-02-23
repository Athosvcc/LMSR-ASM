/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the FastAgent Class
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
public class FastAgent extends NESFIAgent  {

   protected static int minActiveRules = 5;

   public FastAgent(int type) { // only trader with classifier and access to technical bits
      fastLearner = true;
      checkRules = true;
      minActiveRules = 5;
      techTrader = true;
      numRules = 50 ;      // how many trading rules
      newTR = 10;    // how many trading rules to replace by the GA
      ruleSet = new TradingRule[numRules];   // holds all rules for the agent, a set for each stock
      activeRuleSet = new int[numRules+1];
      oldRuleSet = new int[numRules+1];
      if(type > -1) {   // do the following only for real traders, not the "static" agent for parameter settings
         traders++;
         this.ID = traders;
         initializeXBits();
         World.increaseNumberOfClassifierAgents();
         useClassifier = true;
         cash = initialCash;
         numberOfStocks = 1;    // initial endowment of stocks
         stock = World.Stocks;   //(Stock)World.stockList.get(type);
         stock.totalSupply += 1;
         wealth = cash + stock.getPrice();
         World.setTotalWealth(World.getTotalWealth()+wealth);
         for (int i = 0; i < numRules; i++) {   // create a set of numRules TradingRules
            ruleSet[i] = new TradingRule(true, true, true); // (useclassifier, technicalTrader, ruleCheck)
         }
         activeRuleSet[0] = -1;
         oldRuleSet[0] = -1 ;
         World.numRules += numRules;
         fProb = getAgentFBitFraction();
         tProb = getAgentTBitFraction();
      } else { // do the rest for the "static" agent

      }
   }  //constructor

   /**
    * Performs the GA on all rules in ruleSet of the agent.
    */
   protected void invokeGA() {
      int parent1, parent2, trial, unUsed, temp, meanSpec;
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
      meanSpec = 0;
      for (int i = 0; i < numRules; i++) {
         meanFitness += ruleSet[i].fitness ;
         meanSpec += ruleSet[i].specificity;
         meanVariance += ruleSet[i].forecastVar;
      }
      meanFitness /= numRules;
      meanSpec /= numRules;
      meanVariance /= numRules;
      temp = numRules -1;
      unUsed=0;
      while (temp >=0 && (World.period - ruleSet[temp--].lastActive) > TradingRule.maxNonActive) {
         unUsed++;
         ruleSet[temp+1].generalize(meanFitness);
      }
      Arrays.sort(ruleSet);   // we have generalized and changed some fitnesses, hence sort again
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
             offspring.mutateNESFI(meanVariance, fProb, tProb);  // give medianstrength and dynamically adjsuting bit probabilities as parameter
         }
         // now insert it into ruleset on order to replace a bad rule, the bad rules have low position indeces
         ruleSet[i] = offspring;
      }  // all newTR created, initialized and inserted into ruleSet

      fProb = getAgentFBitFraction();
      tProb = getAgentTBitFraction();
   }  // invokeGA

   /** Returns the index number of the selected rule in ruleSet[].
    *  First, the method determineActiveRules is called in order to get an array with all
    *  indices of active rules. According to the choosen selection method (SELECT_BEST,
    *  SELECT_AVERAGE, or SELECT_ROULETTE), a particular is activated by returning the index
    *  in ruleSet of that particular rule.
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
               cumFitness += ruleSet[activeRuleSet[j++]].getFitness();
            }
            j=0;
            double x = Random.uniform.nextDoubleFromTo(0f,cumFitness);
            cumFitness = 0;
            while (activeRuleSet[j]>=0) {
               cumFitness += ruleSet[activeRuleSet[j++]].getFitness();
               if (cumFitness > x ) {
                  selected = activeRuleSet[--j];
                  done = true;
                  break;
               }
            }
         }  // select Roulette
         else if (selectionMethod==SELECT_BEST) {
            j=0;
            while (activeRuleSet[j]>=0) {
               if (ruleSet[activeRuleSet[j]].fitness > maxFitness) {
                  maxFitness = ruleSet[activeRuleSet[j]].fitness;
                  selected = activeRuleSet[j];
                  done = true;
               }
               j++;
            }
         }  // Select Best
         if (done && ruleSet[activeRuleSet[j]].activeCounter < TradingRule.minCount) {
            done = false;
         }
         if (done && ruleSet[activeRuleSet[j]].activeCounter >= TradingRule.minCount) {
            ruleSet[selected].use();   // sets lastUsed and Counter
            pdCoeff = ruleSet[selected].getAij();
            offset = ruleSet[selected].getBij();
            fcVar = ruleSet[selected].getForecastVar();
         }
         if (!done) { // || selectionMethod == SELECT_AVERAGE || activeRuleSet[stockID][0] == -1) { // if select average or no rules match, use global means of all rules
            selectedAverage = true;
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


}