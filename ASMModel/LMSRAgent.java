/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the NESFIAgent Class
*/

package ASMModel;

import uchicago.src.sim.engine.CustomProbeable;
import uchicago.src.sim.util.Random;

import java.util.Arrays;


/**
 * Some methods that differ between agents are invokeGA() or chooseRule(), for instance.
 * Thus, they are overwritten here.
 */
public class LMSRAgent extends Agent implements CustomProbeable {

   protected static int minActiveRules;
   protected static int newTR =20  ;
   protected static int numRulesNESFIAgent = 100 ;
   protected static int NESFIActivatedRules = 0;
   protected static int NESFISelectAverageCounter = 0;  // compare how often select average and wh

   public LMSRAgent() {
      NESFISelectAverageCounter = 0;
   }

   public LMSRAgent(int traderType) {
      nesfiAgent = false;
      minActiveRules = 1;  // minimum # of rules to be activated before using roulette, if less, then use average values
      techTrader = true ; // no usage of technical trading bits, false only fundamental bits, initialized in constructr to set parameter
      useClassifier = true;   // use of classifier system or no condition checking
      if (traderType > -1 ) { // real trader, not the static one to adjust some parameters
         traders++;
         this.ID = traders;
         numRules = numRulesNESFIAgent ;      // how many trading rules
         ruleSet = new TradingRule[numRules];   // holds all rules for the agent, a set for each stock
         activeRuleSet = new int[numRules+1];
         oldRuleSet = new int[numRules+1];
         if(Random.uniform.nextDoubleFromTo(0d,1d) < World.getFracClassifierAgents()) {
//            World.increaseNumberOfClassifierAgents();
            useClassifier = true;
         } else {
            useClassifier = false;
            zeroFundamentalBitAgent = true;
            zeroTechnicalBitAgent=true;
            zeroBitAgent=true;
         }
         initializeXBits();
         cash = initialCash;
         numberOfStocks =0;    // initial endowment of stocks
         stock = World.Stocks;   //(Stock)World.stockList.get(type); //mudar
         stock.totalSupply += 1;
         wealth = cash + stock.getPrice();
         World.setTotalWealth(World.getTotalWealth()+wealth);
         for (int i = 0; i < numRules; i++) {   // create a set of numRules TradingRules
            ruleSet[i] = new TradingRule(useClassifier, techTrader, checkRules );   // true = ruleCheck
         }
         activeRuleSet[0] = -1;
         oldRuleSet[0] = -1 ;
         World.numRules += numRules;
         fProb = getAgentFBitFraction();
         tProb = getAgentTBitFraction();
      } else { // actual instance of a trader which is only used to set the static parameters through the gui
         staticAgent = true;
         // no endowment with stocks
      }
   }  // constructor


   /**
    * Performs the GA on the worst newTR rules in ruleSet of the agent.
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
      if(useClassifier) {
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
      }
      for (int i = 0; i < newTR ; i++ ) {
         parent1 = tournament();
         trial=0;
         double prob = Random.uniform.nextDoubleFromTo(0d,1d);
         if (prob < probCrossover) {
            do {
               parent2=tournament();
            } while (parent1==parent2 && trial++ < Specialist.MAXITERATIONS);
            offspring = crossover(parent1, parent2);
         } else { // no crossover, but mutation
             offspring = copyRule(parent1);  // parent1
//             System.out.println("Fitness Rule 0 : "+ruleSet[0].fitness+ "  Fitness rule numRule : "+ruleSet[numRules-1].fitness);
//             System.out.print("Before Mutation                          : "); offspring.show_bits(0);
             offspring.mutateNESFI(meanVariance, fProb, tProb);  // give medianstrength and dynamically adjsuting bit probabilities as parameter
//             System.out.print("After  Mutation / After Consistency Check: ");offspring.show_bits(0);
         }
         // now insert it into ruleset on order to replace a bad rule, the bad rules have low position indeces
         ruleSet[i] = offspring;
      }  // all newTR created, initialized and inserted into ruleSet
      // now determine new fBit- and tBitFractions
      fProb = getAgentFBitFraction();
      tProb = getAgentTBitFraction();
   }  // invokeGA


/** I decided to add a 1-point crossover to the original SFI-mutation operator, which was only
 *  a uniform crossover. With the updated mutation operator and uniform crossover as the
 *  exclusive crossover procedure, corner solutions for individual bits tended to be replicated,
 *  i.e., once a bit position was set for all trading rules, uniform crossover replicated this
 *  exactly and we were in a lock-in. Only mutation was a way to escape from that trap.
 *  Two point crossover is another way of avoiding those corner solutions. It should also
 *  simplify the interpretation of bit settings for periodic dividends. Sometimes I had no
 *  idea whether the increased existence of those corner solution was a mere technical
 *  coincidence or whether it indeed reflected useful information by this bit.
 *  The 1-point crossover is only invoked on the bit string. The procedure for the
 *  real valued parameters is not changed. Uniform and 1-point crossover are invoked
 *  with equal probability.
 */

//   protected TradingRule crossover(int rule1, int rule2) {
//      double prob, weight1, weight2, prob2;
//      long bit;
//      int words;
//      TradingRule offspring;
//
//      /* uniform crossover on the condition bits*/
//      offspring = copyRule(rule1);
//      words = (!offspring.technicalRule ? 1 : 2  );
//
//      System.out.println("Before Crossover:");
//      ruleSet[rule1].show_bits(0);
//      ruleSet[rule2].show_bits(0);
//      if (Random.uniform.nextIntFromTo(0,1)==0) {
//         // with equal probability uniform crossover
//         for (int j = 0 ; j < words ; j++ ) {
//            for (int i = 0; i<32 ; i++ ) {
//               if (Random.uniform.nextIntFromTo(0,1)==0) {  // with equal probability
//                  bit = (ruleSet[rule2].conditionWords[j] >> Asset.SHIFT[i] & 3l );
//               } else {
//                  bit = (ruleSet[rule1].conditionWords[j] >> Asset.SHIFT[i] & 3l );
//               }
//               offspring.conditionWords[j] = offspring.conditionWords[j] & Asset.NMASK[i] | (bit << Asset.SHIFT[i]) ;
//            }  // for all condition bits
//         }  // for all condition words
//         // end uniform crossover
//         System.out.println("Offspring after uniform crossover before check:");
//         offspring.show_bits(0);
//      } else {
//         // now perform the 2-point crossover
//         int crossoverPoint = Random.uniform.nextIntFromTo(0,31);
//         for (int j = 0 ; j < words ; j++ ) {
//            for (int i = 0; i < crossoverPoint ; i++ ) {
//               bit = (ruleSet[rule1].conditionWords[j] >> Asset.SHIFT[i] & 3l );
//               offspring.conditionWords[j] = offspring.conditionWords[j] & Asset.NMASK[i] | (bit << Asset.SHIFT[i]) ;
//            }
//            for (int i = crossoverPoint; i < 32 ; i++ ) {
//               bit = (ruleSet[rule2].conditionWords[j] >> Asset.SHIFT[i] & 3l );
//               offspring.conditionWords[j] = offspring.conditionWords[j] & Asset.NMASK[i] | (bit << Asset.SHIFT[i]) ;
//            }
//         }  // for all condition words
//         System.out.println("Offspring after 1-point crossover, crossover point = "+crossoverPoint+ ":");
//         offspring.show_bits(0);
//      }
//      // now, do crossover with forecast-parameters
//      prob = Random.uniform.nextDoubleFromTo(0d,1d);
//      if (prob < probLinear) {
//         // choos e a linear combination of parents parameters, weighted by strength
//         if(ruleSet[rule1].forecastVar > 0d && ruleSet[rule2].forecastVar > 0d) {
//            weight1 = ruleSet[rule2].forecastVar/(ruleSet[rule1].forecastVar+ruleSet[rule2].forecastVar);
//         } else {
//            weight1 = 0.5;
//         }
//         weight2 = 1d - weight1;
//         offspring.forecastPart[0] = weight1*ruleSet[rule1].forecastPart[0]
//            + weight2*ruleSet[rule2].forecastPart[0];
//         offspring.forecastPart[1] = weight1*ruleSet[rule1].forecastPart[1]
//            + weight2*ruleSet[rule2].forecastPart[1];
//      } else if (prob < probLinear + probRandom) {
//         // choose each parameter randomly from each parent
//         for (int j=0 ; j<2 ; j++ ) {
//            prob2 = Random.uniform.nextIntFromTo(0,1);
//            if(prob2==0) {
//               offspring.forecastPart[j] = ruleSet[rule1].forecastPart[j];
//            } else {
//               offspring.forecastPart[j] = ruleSet[rule2].forecastPart[j];
//            }
//         }  // for all 2 parameters
//      } else {
//         // parameters are taken solely from one randomly chosen parent
//         prob2 = Random.uniform.nextIntFromTo(0,1);
//         if(prob2==0) {
//            offspring.forecastPart[0] = ruleSet[rule1].forecastPart[0];
//            offspring.forecastPart[1] = ruleSet[rule1].forecastPart[1];
//         } else {
//            offspring.forecastPart[0] = ruleSet[rule2].forecastPart[0];
//            offspring.forecastPart[1] = ruleSet[rule2].forecastPart[1];
//         }
//      }  // all crossover methods for forecast-parameters
//      offspring.forecastVar = meanVariance;
//      if (checkRules) {
//         offspring.check_consistency();
//      } else
//         offspring.getSpecificity();
//      System.out.println("Offspring after uniform crossover after check:");
//      offspring.show_bits(0);
//
//      offspring.detFitness();
//      return offspring;
//   }  // crossover



   public boolean getCheckRules() {
      return checkRules;
   }
   /**
    * NESFI agents can switch rule checking on or off.
    */
   public void setCheckRules(boolean val) {
//      if(World.period>0) {
//         System.out.println("Period: "+World.period+": CheckRules: "+checkRules+ " Fund. bits: "+World.getWorldFBitFraction()+"; techn. bits: "+World.getWorldTBitFraction()+" total bit fraction: "+World.getWorldBitFraction());
//      }
      this.checkRules = val;
      if(World.period>0) {
         for (int j = 0 ; j < World.numberOfAgents ; j++) {
            if(World.Agents[j].nesfiAgent) {
               World.Agents[j].initializeXBits();
               if (val) {
                  for (int k=0 ; k < World.Agents[j].numRules; k++ ) {
                     World.Agents[j].ruleSet[k].check_consistency();
                  }
               }
               World.Agents[j].fProb = World.Agents[j].getAgentFBitFraction();
               World.Agents[j].tProb = World.Agents[j].getAgentTBitFraction();
            }
         }
//         System.out.println("Period: "+World.period+": CheckRules: "+checkRules+ " Fund. bits: "+World.getWorldFBitFraction()+"; techn. bits: "+World.getWorldTBitFraction()+" total bit fraction: "+World.getWorldBitFraction());
      }
   }

   /** Returns the index number of the selected rule in ruleSet[].
    *  First, the method determineActiveRules is called in order to get an array with all
    *  indices of active rules. According to the choosen selection method (SELECT_BEST,
    *  SELECT_AVERAGE, or SELECT_ROULETTE), a particular is activated by returning the index
    *  in ruleSet of that particular rule.
    *  Even though the code is identical for all three sub classes, it accesses a static
    *  variable minActiveRules that it defined in the derived classes, thus not available
    *  from the super class.
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
         NESFIActivatedRules += activeRules ; // updates # of activated rules for NESFI-Agents
      } else { // no classifier mode
         for (int i = 0; i < numRules; i++) {
            activeRuleSet[i] = i; // mark rule as active only if it was activated at least TradingRule.minActive times
            ruleSet[activeRuleSet[i]].activeCounter++;
         }  // for all rules
         activeRuleSet[numRules] = -1;  // -1 indicates the end of the list of activated rule; later used with while not -1
         activeRules = numRules;
         activatedRules += activeRules;
         NESFIActivatedRules += numRules ; // updates # of activated rules for NESFI-Agents
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
      if (done) {
         ruleSet[selected].use();   // sets lastUsed and Counter
         System.out.println(ruleSet[selected]);
         pdCoeff = ruleSet[selected].getAij();
         offset = ruleSet[selected].getBij();
         fcVar = ruleSet[selected].getForecastVar();
      } else { // || selectionMethod == SELECT_AVERAGE || activeRuleSet[stockID][0] == -1) { // if select average or no rules match, use global means of all rules
         selectedAverage = true;
         selectAverageCounter++;
         NESFISelectAverageCounter++;
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

   public void setReplaceXXRules(int val) {
      newTR = val;
   }
   public int getReplaceXXRules() {
      return newTR;
   }
   public int getNumRules() {
      return this.numRulesNESFIAgent;
   }
   public void setNumRules(int val) {
      numRulesNESFIAgent = val;
      this.numRules = val;
   }

   //   public boolean getCheckRules() { return this.checkRules; }
//   public void setCheckRules(boolean val) { this.checkRules = val; }



//   public String getName() {
//      return "NESFI-Agent";
//   }


}