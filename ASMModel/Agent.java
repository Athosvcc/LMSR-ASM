/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the Agent Class
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
 * Contains all the variables and procedures that are common to all agents.
 * Some of the procedures are overwritten in the derived classes
 */
public class Agent implements Drawable {

   // static class variables
   // coding for selection method for the trading rules
   protected static final int SELECT_BEST = 0;
   protected static final int SELECT_AVERAGE = 1;
   protected static final int SELECT_ROULETTE = 2;
   protected static final int SELECT_RANDOM = 3;
   public static int selectionMethod = SELECT_BEST;

   protected boolean selectedAverageOld = false;  // need both variable to check whether last period was selectAverage
   protected boolean selectedAverage = false;     // need that for updatePerformance
   protected static int selectAverageCounter = 0;  // for SFIagents, compare how often select average and whether there is any connection to wealth levels
   /* These variables are set if the primary selection mechanism is not selectAverage, yet
   *  select average was effectively used since there were no or to few rules activated.
   *  Then, the performance of all rules are updated.
   */

   protected static double MAXHOLDINGS = 1000000000; // no one can own or owe more than the absolute number of stocks available in the economy
   protected static int traders = 0;   // needed for ID-initialization in constructor
   protected static int instancesOfTechnicians = 0;

   protected static double riskAversion = 0.3;
   protected static int gaCount = 0;
   protected int numRules ;      // how many trading rules
   protected static double probCrossover = 0.1;
   protected static double probLinear = 0.333333;
   protected static double probRandom = 0.333333;
   protected static boolean checkRules = false;  // affects both SFI- and NESFI-Agents since static
   protected boolean nesfiAgent;

   // Instance-Variables
   protected int ID ;
   protected boolean techTrader = false ; // no usage of technical trading bits, false only fundamental bits, initialized in constructr to set parameter
   protected boolean useClassifier = true;   // use of classifier system or no condition checking

   protected boolean fastLearner = false;
   protected double wealth = 0;
   protected static double MINCASH = -2000;
   protected double cash;
   protected double initialCash = 100.0;

   protected TradingRule[] ruleSet ;
   protected int[] activeRuleSet ;
   protected int[] oldRuleSet ;
   protected int selectedRule;  // ID of selected Rule in activeRuleSet[]

   protected boolean pos;
   protected double optimalDemand;
   protected double supply; // used in LSMR
   protected double order ; // = new double[World.differentStocks];
   protected double slope ; // = new double[World.differentStocks];
   protected double numberOfPosStocks;
   protected double numberOfNegStocks;
   protected double numberOfStocks ;
   protected double cumulatedNumberOfStocks; // introduced to check the theoretically derived hypothesis that wealthier agents hold, on average, more stock than poorer agents.
   protected double averageNumberOfStocks ;  // introduced to check the theoretically derived hypothesis that wealthier agents hold, on average, more stock than poorer agents.
   protected double fcVar ; // = new double[World.differentStocks];
   protected double divisor ; // = new double[World.differentStocks];
   protected Stock stock;
   protected LMSRStock stockLMSR;
   protected Specialist specialist;
   protected int x;  // for graphical display
   protected int y;  // for graphical display

   protected double pdCoeff, offset ;
   protected double forecast ;
   protected double medFitness, meanFitness, meanVariance ;

   private double Eofdtp1 = 0;   // holds expectation value of next dividends given the others stock realization of the dividend
   protected static long activatedRules = 0; // holds total # of activated rules for the whole economy
   protected int activeRules = 0;  //  holds number of activated rules for this agent
   protected boolean staticAgent = false;
   protected int maxTBits;   // maximum of technical bits per rule that can be set, depends on ruleChecking, too
   protected int maxFBits;    // maximum of fundamental bits per rule that can be set, depends on ruleChecking, too

   // the following 4 variables are determined after rule creation and after GA-invocation; saves time when accessing them
   protected int fBitsInUse;
   protected int tBitsInUse;
   protected double fProb ;
   protected double tProb ;
   protected boolean zeroFundamentalBitAgent = false;
   protected boolean zeroTechnicalBitAgent = false;
   protected boolean zeroBitAgent = false;



   public Agent() {
      selectAverageCounter=0;
      // traders = 0;
   }

   public Agent(int type) {
      if (type>-1) {
         traders++;
         this.ID = traders;
      } else {
         staticAgent= true;
      }
   }  // constructor

   public double getOrder() {
      return order;
   }
   public double getID() {
      return ID;
   }
   public double getCash() {
      return cash;
   }
   public double getInitialCash() {
      return initialCash;
   }
   public void setInitialCash(double val) {
      this.initialCash = val;
   }

   public double getWealth() {
      return wealth;
   }
   public double getNumberOfStocks() {
      return numberOfStocks;
   }

   public void executeOrder() {
      if (AsmModel.LMSR) { // gets cost of order and adds stocks to current holdings of agents and of the system as a whole, as well as sets revenue and subtracts cash paid
         double costLMSR;
         stockLMSR = World.LMSRStocks;
         specialist = AsmModel.specialist;
         costLMSR = specialist.getCostLMSR(order, pos);
         if (pos) { // if agent will buy or sell positive stocks
            stockLMSR.setQPosLMSR(order); // adds or subtracts to the total of positive stocks in the system
            numberOfPosStocks += order; // adds or subtracts to total of positive stocks the agent holds
            specialist.setSpecialistRevenue(costLMSR); // adds to Market Maker revenue
            cash -= costLMSR; // subtracts the cost of the order from the agent's cash
         } else { // if agent will buy or sell negative stocks
            stockLMSR.setQNegLMSR(order); // adds or subtracts to the total of positive stocks in the system
            numberOfNegStocks += order; // adds or subtracts to total of positive stocks the agent holds
            specialist.setSpecialistRevenue(costLMSR); // adds to Market Maker revenue
            cash -= costLMSR; // subtracts the cost of the order from the agent's cash
         }
      } else {
         double bfp, ofp;
         stock = World.Stocks;
         bfp = Specialist.bidFrac*stock.getPrice();
         ofp = Specialist.offerFrac*stock.getPrice();
         if (order > 0.0) {
            numberOfStocks += order*Specialist.bidFrac;
            cash -= order*bfp;
         }
         else if (order < 0.0) {
            numberOfStocks += order*Specialist.offerFrac;
            cash -= order*ofp;
         }
         cumulatedNumberOfStocks += numberOfStocks;
         averageNumberOfStocks = cumulatedNumberOfStocks / World.period ;
         // wealth is updated in getEarnings... etc   // ?
      }

   }  // executeOrder()

   /**
    * This is done in each period after the new dividend is declared.  It is
    * not normally overridden by subclases.  The taxes are assessed on the
    * previous wealth at a rate so that there's no net effect on an agent
    * with position = 0.
    * Taxes are introduced to avoid explosive wealth behavior since interest
    * payments would cause the cash position to rise exponentially. For long
    * simulation runs, this means trouble.
    *
    * In principle we do:
    *	wealth = cash + price*numberOfStocks;			// previous wealth
    *	cash += interestRate*cash + numberOfStocks*dividend;	// earnings
    *	cash -= wealth*interestRate;				// taxes
    * but we cut directly to the cash:
    *	cash -= numberOfStocks*(interestRate*price - dividend)
    */
   public void getEarningsAndPayTaxes() {
      if (AsmModel.LMSR) {
         stockLMSR = World.LMSRStocks;
         if (cash < MINCASH) {
            cash = MINCASH;
         }
         wealth = cash + numberOfPosStocks*stockLMSR.getPrice() + numberOfNegStocks*stockLMSR.getPriceNoStock();// update wealth
      } else {
         stock = World.Stocks;
         cash -= numberOfStocks*(World.interestRate*stock.price-stock.getDividend());
         if (cash < MINCASH) {
            cash = MINCASH;
         }
         wealth = cash + numberOfStocks*stock.getPrice();;     // update wealth
      }
   } // receiveInterestAndDividend

   public void setPayout() { // mudar // adicionar boolean pra acao que se realizou?
      stockLMSR = World.LMSRStocks;
      specialist = AsmModel.specialist;
      if (stockLMSR.probability > 0.5) { // if stock probability is over 0.5 at the last period, "Yes" stocks pay
         if (numberOfPosStocks > 0) {
            wealth = cash + numberOfPosStocks*1;
            // specialist.setSpecialistPayout(numberOfPosStocks*1);
            numberOfPosStocks = 0;
         } else { // if they aren't, stock is value 0
            wealth = cash; // Yes stocks pay 0
            numberOfPosStocks = 0;
         }
      } else { // if stock probability is less than 0.5 at the last period, "No" stocks pay
         if (numberOfNegStocks > 0) {
            wealth = cash + numberOfNegStocks * 1;
            // specialist.setSpecialistPayout(numberOfNegStocks*1);
            numberOfNegStocks = 0;
         } else { // if they aren't, stock is value 0
            wealth = cash; // No stocks pay 0
            numberOfNegStocks = 0;
         }
      }
   }
   


   public void draw(SimGraphics g) {
     g.drawFastRoundRect(Color.green);
   }
   public int getX() {
     return x;
   }
   public void setX(int x) {
     this.x = x;
   }
   public int getY() {
     return y;
   }
   public void setY(int y) {
     this.y = y;
   }

   public void setDemandAndSlope(double trialPrice) {
      order = 0;     // if trader don't trade in that stock, then
      slope = 0;     // set order and slope to zero
      stock = World.Stocks;
      stockLMSR = World.LMSRStocks;
      if (AsmModel.hree) {
         forecast = trialPrice * World.interestRatep1 +
            (Agent.riskAversion*World.interestRatep1*stock.getNoiseVar()) /
            (World.interestRatep1 - stock.getRho());
      } else if (AsmModel.LMSR) {
         forecast = stockLMSR.getProbability() + offset; // gets the real probability and adds own perception
         if (forecast > trialPrice) { // if the agent thinks the probability is higher than the current price
            if (numberOfNegStocks == 0) { // if agent has no "No" stocks
               pos = true; // agent will buy "Yes" stocks
               optimalDemand = ((forecast-trialPrice)/(divisor) - numberOfPosStocks); // optimal CARA demand and Bernoulli standard deviation // mudar numero arbitrario
               order = Math.round(optimalDemand);
               executeOrder();
               // System.out.println("orderPos: " + order);
            } else {
               pos = false;
               order = -numberOfNegStocks; // sells all No stocks
               executeOrder();
               // System.out.println("orderPos: " + order);
            }
         } else { // if the agent thinks the probability is lower than the current price
            if (numberOfPosStocks == 0) { // if agent has no "Yes" stocks
               pos = false; // agent will buy "No" stocks
               optimalDemand = (((trialPrice-forecast))/(divisor) - numberOfNegStocks); // optimal CARA demand and Bernoulli standard deviation // mudar numero arbitrario
               order = Math.round(optimalDemand);
               executeOrder();
               // System.out.println("orderNeg: " + order);
            } else { // agent will sell all "Yes" stocks
               pos = true;
               order = -numberOfPosStocks; // sells all Yes stocks
               executeOrder();
               // System.out.println("orderNeg: " + order);
            }
         }
         // System.out.println("trial price: " + trialPrice);
      } else {
         forecast = (trialPrice+stock.getDividend())*pdCoeff + offset;
      }
      if (!AsmModel.LMSR) {
         if (forecast >= 0.0) {
            order = (forecast-trialPrice*World.interestRatep1)/(divisor) - numberOfStocks;
            slope = (pdCoeff - World.interestRatep1)/(divisor);
         } else {
            forecast = 0.0;
            order = -trialPrice*World.interestRatep1/divisor - numberOfStocks;
            slope = -World.interestRatep1/divisor;
         }
         // clip bids or offers such that (-)MAXHOLDINGS will not be violated
         clipBidOffer();
      }
      constrainDemand(trialPrice);    // make sure that budget constraints are not violated
   }	 // setDemandAndSlope


   /**
    * Constrains demand such that neither MINCASH nor MAXBID are violated for one single stock
    * This applies only if there is one stock to check.
   */
   public void constrainDemand(double trialPrice) {
      if (AsmModel.LMSR) {
         specialist = AsmModel.specialist;
         while (specialist.getCostLMSR(order, pos) > (cash - MINCASH)) {
            order--;
         }
      } else {
         if (order > 0.0) {
            if (order * trialPrice > (cash - MINCASH)) {
               if ((cash - MINCASH) > 0.0) {
                  order = (cash - MINCASH) / trialPrice;
                  slope = -order / trialPrice;
               } else {
                  order = 0.0;
                  slope = 0;
               }
            }
         } else {
            if (order + numberOfStocks < -MAXHOLDINGS) {
               order = -MAXHOLDINGS - numberOfStocks;
               slope = 0.0;
            } else if ((order + numberOfStocks < 0.0) && cash < 0.0) {
               order = 0.0;
               slope = 0.0;
            }
         }
      }
   }  // constrainDemand

   public double getDemand() {
      return order;
   }
   public double getSlope() {
      return slope;
   }

   /** After all agents have been created and equipped with their initial endowments, the
    *  MAXHOLDINGS[] are initialized. See also method clipAtMaxBids.
    */
   public static void initMinMaxs() {
      MAXHOLDINGS = World.Stocks.getTotalSupply()/5;   // just a fifth of total stock supply
   }  // initMinMaxs

   /** Every bid is checked whether it is feasible to obtain in general. This procedure
    *  ensures that no agent can own or owe more than the total supply of a particular
    *  asset in the economy. (-MAXHOLDINGS <= numberOfStocks <= MAXHOLDINGS)
    *  The bids and offers are checked whether their execution would violate this constraint.
    */
   public void clipBidOffer() {
      if (numberOfStocks + order > MAXHOLDINGS) {
         order = Math.min(10,MAXHOLDINGS - numberOfStocks);  //restricted to trade no more than 10 shares, as in LeBaron et.al.
         // System.out.println("Constrained a Bid");
      } else if (numberOfStocks + order < -MAXHOLDINGS) {
         order = Math.max(-10,-MAXHOLDINGS - numberOfStocks);    //restricted to trade no more than 10 shares, as in LeBaron et.al.
         // order = 0.0; //
         // System.out.println("Constrained an Offer");
      }
   }  // clipBidOffer


   /** Determines which rules in ruleSet match the current state-of-the-world. The ID's of
    *  these rules are saved in activeRuleSet.
    *  This method is called from chooseRule().
   */
   protected int determineActiveRules() {
      int activeIndex = 0;
      for (int i = 0; i < numRules; i++) {
         if (ruleSet[i].match() ) {   // check here whether state-of-the-world is matched
            activeRuleSet[activeIndex++] = i;
         }
      }  // for all rules
      // System.out.println(activeIndex+"/"+numRules+" activated");
      activeRuleSet[activeIndex] = -1;  // -1 indicates the end of the list of activated rule; later used with while not -1
      return activeIndex;
   }  // determineActiveRules

   /**
    * Updates the performance of all rules that were activated in the last period.
    *
   */
   public void updatePerformance() {
      int j = 0;
      if (!useClassifier || selectionMethod == SELECT_AVERAGE || selectedAverageOld ) {   // if select average (also when we resorted to average because no rules were activated), update all rules
         for (int i = 0; i < numRules; i++) {
            ruleSet[i].updateFitness();
         }
      } else {
         while (oldRuleSet[j] > -1  ) {
            ruleSet[oldRuleSet[j++]].updateFitness();
         }
      }
   }  // updatePerformance


   protected void invokeGA() { // will be defined in subclasses
   }


   /**
    * Select the better parent from two via roulette wheel mechanism chosen parents,
    * provided that the parent had been activated in its lifetime.
    * This is the original stochastic tournament selection mechanism (Wetzel ranking)
    * as suggested by Wetzel (1983) uses roulette wheel
    * selection to chose the two candidates, from which the one with higher fitness is
    * selected
    * This procedure is overwritten in the SFIAgent-class.
    */
   protected int tournament() {
      int p1, p2, trial;
      double prob = 0;
      double totalFitness = 0;
      double cumFitness = 0 ;
      trial = 0;
      for(int i = 0 ; i < numRules ; i++ )  {
            totalFitness += ruleSet[i].fitness;
      }  // determining total Fitness of all rules
      prob = Random.uniform.nextDoubleFromTo(0f,totalFitness);
      p1 = -1;
      while (cumFitness <= prob) {
         cumFitness += ruleSet[++p1].fitness ;
      }
      p2 = -1 ;
      cumFitness = 0;
      do {
            prob = Random.uniform.nextDoubleFromTo(0f,totalFitness);
            while (cumFitness <= prob) {
               cumFitness += ruleSet[++p2].fitness ;
            }
         }  // do
         while ((p1==p2)  && trial++ < Specialist.MAXITERATIONS);
      if(ruleSet[p1].fitness > ruleSet[p2].fitness) {
         return p1;
      } else {
         return p2;
      }
   }  // tournament


   protected TradingRule copyRule(int whichRule) {
      /* doesn't work yet, needs to be a deep clone
      */
//      System.out.println("Erstelle neue Regel!");
      TradingRule offspring = new TradingRule(useClassifier, techTrader, checkRules);
      if(useClassifier) {
         for (int i = 0; i < 2 ; i++) {
            offspring.conditionWords[i] = ruleSet[whichRule].conditionWords[i];
         }
      }
      offspring.getSpecificity();
      offspring.forecastPart[0] = ruleSet[whichRule].forecastPart[0];
      offspring.forecastPart[1] = ruleSet[whichRule].forecastPart[1];
      offspring.forecastVar = ruleSet[whichRule].forecastVar;
      offspring.fitness = ruleSet[whichRule].fitness;
      offspring.birth = World.period;
      offspring.lastActive = World.period;
      offspring.lastUsed = World.period;
      offspring.usedCounter = 0; // ruleSet[whichRule].count; //?
      return offspring;
   }

   protected TradingRule crossover(int rule1, int rule2) {
      double prob, weight1, weight2, prob2;
      long bit;
      int words;
      TradingRule offspring;

      /* uniform crossover on the condition bits*/
      offspring = copyRule(rule1);
      words = (!offspring.technicalRule ? 1 : 2  );

//      System.out.println("Before Uniform Crossover:");
//      ruleSet[rule1].show_bits(0);
//      ruleSet[rule2].show_bits(0);

      for (int j = 0 ; j < words ; j++ ) {
         for (int i = 0; i<31 ; i++ ) {
            if (Random.uniform.nextIntFromTo(0,1)==0) {
               bit = (ruleSet[rule2].conditionWords[j] >> Asset.SHIFT[i] & 3l );
            } else {
               bit = (ruleSet[rule1].conditionWords[j] >> Asset.SHIFT[i] & 3l );
            }
            offspring.conditionWords[j] = offspring.conditionWords[j] & Asset.NMASK[i] | (bit << Asset.SHIFT[i]) ;
         }  // for all condition bits
      }  // for all condition words
      // end uniform crossover

//      System.out.println("Offspring after uniform crossover before check:");
//      offspring.show_bits(0);

      // now, do crossover with forecast-parameters
      prob = Random.uniform.nextDoubleFromTo(0d,1d);
      if (prob < probLinear) {
         // choos e a linear combination of parents parameters, weighted by strength
         if(ruleSet[rule1].forecastVar > 0d && ruleSet[rule2].forecastVar > 0d) {
            weight1 = ruleSet[rule2].forecastVar/(ruleSet[rule1].forecastVar+ruleSet[rule2].forecastVar);
         } else {
            weight1 = 0.5;
         }
         weight2 = 1d - weight1;
         offspring.forecastPart[0] = weight1*ruleSet[rule1].forecastPart[0]
            + weight2*ruleSet[rule2].forecastPart[0];
         offspring.forecastPart[1] = weight1*ruleSet[rule1].forecastPart[1]
            + weight2*ruleSet[rule2].forecastPart[1];
      } else if (prob < probLinear + probRandom) {
         // choose each parameter randomly from each parent
         for (int j=0 ; j<2 ; j++ ) {
            prob2 = Random.uniform.nextIntFromTo(0,1);
            if(prob2==0) {
               offspring.forecastPart[j] = ruleSet[rule1].forecastPart[j];
            } else {
               offspring.forecastPart[j] = ruleSet[rule2].forecastPart[j];
            }
         }  // for all 2 parameters
      } else {
         // parameters are taken solely from one randomly chosen parent
         prob2 = Random.uniform.nextIntFromTo(0,1);
         if(prob2==0) {
            offspring.forecastPart[0] = ruleSet[rule1].forecastPart[0];
            offspring.forecastPart[1] = ruleSet[rule1].forecastPart[1];
         } else {
            offspring.forecastPart[0] = ruleSet[rule2].forecastPart[0];
            offspring.forecastPart[1] = ruleSet[rule2].forecastPart[1];
         }
      }  // all crossover methods for forecast-parameters
      offspring.forecastVar = meanVariance;
      if (checkRules) {
         offspring.check_consistency();
      } else offspring.getSpecificity();
//      System.out.println("Offspring after uniform crossover after check:");
//      offspring.show_bits(0);
      offspring.detFitness();
      return offspring;
   }  // crossover


   /**
    * Returns the mean fitness of all trading rules for this agent
    */
   protected double getMeanFitness() {
      double fitness = 0;
      for (int j = 0; j < numRules ; j++ ) {
         fitness += ruleSet[j].fitness;
      }
      return fitness/numRules;
   }

   /**
    * Returns the minimum fitness of the rules in this agent's ruleSet
    */
   protected double getMinFitness() {
      double fitness = 200;
      for (int j = 0; j < numRules ; j++ ) {
         fitness = Math.min(fitness,ruleSet[j].fitness);
      }
      return fitness;
   }

   /**
    * Returns the minimum fitness of the rules in this agent's ruleSet
    */
   protected double getMaxFitness() {
      double fitness = 0;
      for (int j = 0; j < numRules ; j++ ) {
         fitness = Math.max(fitness,ruleSet[j].fitness);
      }
      return fitness;
   }  // getMaxFitness


//   public String[] getProbedProperties() {
//      if (!staticAgent) {
//            return new String[] {"ID","wealth","meanFitness","minFitness","maxFitness","fundamentalBitsSet","technicalBitsSet","cash","numberOfStock"};
//      } else { // set static properties for all properties with this actual instance of a trader
//          return new String[] {"initialCash","minCash","riskAversion","replaceXXRules","probCrossover","minActiveRules","SFITournament"};
//      }
//   }

   protected boolean getTechTrader() {
      return techTrader;
   }
   public double getMinCash() {
      return MINCASH;
   }
   public void setMinCash(double val) {
      this.MINCASH = val;
   }
   public int getNumRules() {
      return this.numRules;
   }
   public void setNumRules(int val) {
      this.numRules = val;
   }
   public double getRiskAversion() {
      return this.riskAversion;
   }
   public void setRiskAversion(double val) {
      this.riskAversion = val;
   }
   public double getProbCrossover() {
      return probCrossover;
   }
   public  void setProbCrossover(double val) {
      probCrossover = val;
   }

   protected int getTechnicalBits() {
      tBitsInUse = 0;
      if(techTrader && !zeroTechnicalBitAgent) {
         for (int j = 0; j<numRules ; j++ ) {
            tBitsInUse += ruleSet[j].tBits;
         }
         if(nesfiAgent && tBitsInUse==0) {
            zeroTechnicalBitAgent=true;
            World.numberOfZeroTechnicalBitAgents++;
            System.out.println("Period "+AsmModel.world.period+": New ZeroTechnicalBitAgent");
            // System.out.println("BitAnalyzer: "+AsmModel.world.getBitsSetString());
            if(zeroFundamentalBitAgent) {
               zeroBitAgent=true;
               World.increaseZeroBitAgents();
               // record this if whis is wanted
               if (AsmModel.recordData && AsmModel.recorderOptions.getNewZeroBitAgentAt() && World.period>0) AsmModel.recorder.record();
               // System.out.println("Period "+AsmModel.world.period+": New ZeroBitAgent, total :"+World.getZeroBitAgents());
            }
            if (AsmModel.recordData && AsmModel.recorderOptions.getNewZeroBitAgentAt()  && World.period>World.reInitialize)
               AsmModel.recorder.record();
         }
      }
      return tBitsInUse;
   }  // getTechnicalBits

   protected int getFundamentalBits() {
      fBitsInUse = 0;
      if (useClassifier && !zeroFundamentalBitAgent) {
         for (int j = 0; j<numRules ; j++ ) {
            fBitsInUse += ruleSet[j].fBits;
         }
         if(nesfiAgent && fBitsInUse==0) {
            World.numberOfZeroFundamentalBitAgents++;
            zeroFundamentalBitAgent=true;
            System.out.println("Period "+AsmModel.world.period+": New ZeroFundamentalBitAgent");
            // System.out.println("BitAnalyzer: "+AsmModel.world.getBitsSetString());
            if(zeroTechnicalBitAgent) {
               zeroBitAgent=true;
               World.increaseZeroBitAgents();
               // record this is this is wanted
               if (AsmModel.recordData && AsmModel.recorderOptions.getNewZeroBitAgentAt() && World.period>0) AsmModel.recorder.record();
               // System.out.println("Period "+AsmModel.world.period+": New ZeroBitAgent, total :"+World.getZeroBitAgents());
            }
            if (AsmModel.recordData && AsmModel.recorderOptions.getNewZeroBitAgentAt() && World.period>World.reInitialize)
               AsmModel.recorder.record();
         }
      }
      return fBitsInUse;
   }  // getFundamentalBits()

   protected void initializeXBits() { // is called just once when initializing this agent.
      if(useClassifier && checkRules) {
         maxFBits = 22; // the 2 group of 7 bits that belongs together are reduced at maximum 2 bits for each group
         maxTBits = 25; // there is only one group of seven bits that is reduced by the consistency check to 2 bits
      } else {
         maxFBits = 32; // there are all 32 bits in use for fundamental information
         maxTBits = 32;   // There are only 25 technical conditions coded
      }
   }  // initializeXBits

   protected double getAgentFBitFraction() {
      if(zeroBitAgent) {
         return 0d;
      } else {
         return getFundamentalBits()*1.0/(maxFBits*numRules*1.0);
      }
   }
   protected double getAgentTBitFraction() {
      if(zeroBitAgent) {
         return 0d;
      } else {
         return getTechnicalBits()*1.0/(maxTBits*numRules*1.0);
      }
   }
   protected void chooseRule() {}

   protected boolean isFastLearner() {
      return fastLearner;
   }
   /*
    * This function returns a string which contains the numbers of bits set for each
    * position. It is separated by ; to match the header in the recorder function with
    * which it is synchronized. The header looks liek this
    * "0  ;1  ;2  ;3  " etc, and the given to the DataRecorder to be written in the file is
    * "22 ;0  ;2  ;10 ;".
    * Thus, if properly synchronized, the data file can be still imported into EXCEL and
    * analyzed. I have choosen this way inorder to avoid 40 separate function calls to
    * write each bit. The Write function is called just once, and it gets its data to write
    * from this function.
    *
    */
   public String getAgentBitsSetString(){
      String dataString = "";
      int[][] BitsSet = new int[2][32];  // each position holds how often a particular bit is set in the rule set of this agent
      for (int k = 0; k < numRules ; k++ ) {
         for (int i = 0; i < 2 ; i++) {
            for (int j = 0; j < 32; j++) {
               BitsSet[i][j]=0;
            }
         }
      }
      for (int k = 0; k < numRules ; k++ ) {
         for (int i = 0; i < 2 ; i++) {
            for (int j = 0; j < 32; j++) {
               if ((ruleSet[k].conditionWords[i] >> Asset.SHIFT[j] & 3l ) != 0l ) {
                  BitsSet[i][j]++;
               }
            }
         }
      }
      for (int j = 0; j<2 ;j++ ) {
         for (int i = 0; i < 32 ; i++ ) {
            dataString += fillRight(String.valueOf(BitsSet[j][i]),3," ")+";";
            // System.out.println();
         }
      }
      dataString = dataString.substring(0,dataString.length()-1);
      // System.out.println(dataString);
      return dataString;
   }

      private String fillRight(String inputString, int length, String fillString) {
      if(inputString.length() >= length) {
         return inputString;
      } else {
         for (int i = inputString.length(); i <=length ; i++ ) {
            inputString += fillString;
         }
      }
      return inputString;
   }
}