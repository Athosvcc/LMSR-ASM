/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the World Class
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
 * to gather and report data that make only sense on a "world" level, e.g.,
 * total number or fractions of bits set, total wealth level, trading volume etc.
 */
class World {

   // static class variables
   public static double interestRate = 0.1;
   public static double interestRatep1 = interestRate + 1.0;
   public static int period = 0 ;

   public static int firstGATime = 250;      // should be bigger than TradingRule.theta
   public static int gaInterval = 100;        // average time between GA-runs, (Poisson-process ??)
   public static int gaIntervalFastLearner = 10 ; // some agents can have a different ga-interval
   protected static int firstPeriodForShowingLogs = 2500;
   protected static double gaProb = 1.0 / gaInterval;
   public static ASMModel.Agent[] Agents ;
   public static Stock Stocks ;
   public static LMSRStock LMSRStocks;

   public static int numberOfSFIAgents   = 0;  // have the NESFI-Agents run against the SFI-Agents
   public static int numberOfNESFIAgents = 0;  // have the NESFI-Agents run against the SFI-Agents
   public static int numberOfLMSRAgents = 25;
   private static int numberOfTechnicians = 0;  // must be equal or smaller numberOfSFIAgents; can only be SFI-Agents
   public static int numberOfAgents = numberOfSFIAgents + numberOfNESFIAgents + numberOfLMSRAgents;
   private static double fracFastLearner = 0.0;
   private static int numberOfFastLearner = 0;
   private static double fracClassifierAgents = 1.0;
   private static int numberOfClassifierAgents = 0;

   protected static int[][] BitsSet = new int[2][32];  // each position holds how often a particular bit is set in the economy

   public static int numberOfPeriods = 300000 ;
   protected static int reInitialize = firstGATime + (50*gaInterval) ;  // by then, the price should have reached its equilibrium; temporary effects due to warm up effects can then be accounted for in the re-initialize procedure
   public static int numRules = 0;
   private static double totalWealth = 0 ;
   private static double baseWealth = 0;
   private static double LongTermHREEBaseWealth = 0;
   protected static int numberOfZeroBitAgents = 0;
   protected static int numberOfZeroFundamentalBitAgents = 0;
   protected static int numberOfZeroTechnicalBitAgents = 0;


   public World() { // constructor
      for (int j = 0; j < 2 ; j++ ) {  // for the two words, i.e., the fundamental and technical word
         for (int i = 0 ; i <32 ; i++ ) {
            BitsSet[j][i] = 0;
            Double bit = new Double(0d);
         }
      }
      numberOfZeroBitAgents = 0;
      numberOfZeroFundamentalBitAgents = 0;
      numberOfZeroTechnicalBitAgents = 0;
   }  // constructor

   public static void createAgents() {
      if (AsmModel.LMSR) {
         Agents = new Agent[numberOfLMSRAgents];
         for(int i = 0; i< numberOfLMSRAgents; i++) {
            Agent newLMSRAgent = new LMSRAgent(0);
            Agents[i] = newLMSRAgent;
         }
      } else {
         Agents = new Agent[numberOfAgents] ;
         for(int i = 0; i< numberOfSFIAgents; i++) {
            Agent newSFIAgent = new SFIAgent(0);
            Agents[i] = newSFIAgent;
         }
         for(int i = numberOfSFIAgents; i < numberOfAgents ; i++) {
            if (fracFastLearner > 0.0) {
               if(Random.uniform.nextDoubleFromTo(0d,1d) < fracFastLearner) {
                  Agent newFastAgent = new FastAgent(0);
                  Agents[i] = newFastAgent;
               } else {
                  Agent newNESFIAgent = new NESFIAgent(0);
                  Agents[i] = newNESFIAgent;
               }
            } else {
               Agent newNESFIAgent = new NESFIAgent(0);
               Agents[i] = newNESFIAgent;
            }
         }  // all NESFI-Agents, either of type Normal- or FastLearner
      }

      baseWealth = Agents[0].cash; // assume that all agents have same endowments; then, baseWealth is the wealth due to inactivity, i.e., an agents does not trade and holds his initial endowment of one unit of stock.
      System.gc();
      if (AsmModel.recordData && AsmModel.recorderOptions.getNewZeroBitAgentAt() && World.period>0)
         AsmModel.recorder.record();
   }	// createAgents()

   protected static int getNumberOfTechnicians() {
      return numberOfTechnicians;
   }
   protected static void setNumberOfTechnicians(int val) {
      numberOfTechnicians = val;
   }
   protected static double getFracClassifierAgents() {
      return fracClassifierAgents;
   }
   protected static void setFracClassifierAgents(double val) {
      fracClassifierAgents = val;
   }
   protected static double getFracFastLearner() {
      return fracFastLearner;
   }
   protected static void setFracFastLearner(double val) {
      fracFastLearner = val;
   }
   protected static double getTotalWealth() {
      return totalWealth;
   }

   public static void setTotalWealth(double wealth) {
      totalWealth = wealth;
   }

   /**
    * Just a checking procedure to check whether the amount of stock stays constant
    * in the economy.
    */
   protected static double getAmountOfStock() {
      double amount = 0.0;
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         amount += Agents[i].getNumberOfStocks();
      }
      return amount;
   }

   /**
    * Determines correlations, for instance price-value-correlations for each stock (if wanted).
    * pdCorr has to be determined if market is in two-stock mode. It is the (p+d)-correlation
    * between the two stocks and is needed since agents condition their forecasts on it.
    */
   protected void detCorrelations() {
      if(AsmModel.observer.getShowPriceValueCorr() || AsmModel.recorderOptions.getPVCorr() ) {
         // determines wheter price tracks value, i.e. the correlation between the actual
         // price series and the risk-adjusted hree-price
         Stocks.pvCorr = Stats.correlation(Stocks.priceHistory,Stocks.hreePriceHistory);
         // System.out.println("PV-Corr: "+Stocks[i].pvCorr0);
      }  // if scheduled to show price-value-correlation
   }  // determine correlations

   protected void finalize() throws Throwable {
      period = 0 ;
      totalWealth = 0;
      super.finalize();
   }  // finalize

   protected static int getFundamentalBits() {
      int bits = 0;
      for (int i = 0 ; i <32 ; i++ ) {
         BitsSet[0][i] = 0;
      }
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         if (Agents[i].useClassifier) {
            for (int j = 0; j<Agents[i].numRules ; j++ ) {
               bits += Agents[i].ruleSet[j].fBits;
               for (int k = 0; k<32 ; k++ ) {
                  if ((Agents[i].ruleSet[j].conditionWords[0] >> Asset.SHIFT[k] & 3l ) > 0l ) {
                     BitsSet[0][k]++;
                  }
               }
            }
         }
      }
      return bits;
   }  // getNumberOfFundamentalBits

   protected static int getTechnicalBits() {
      int bits = 0;
      for (int i = 0 ; i <32 ; i++ ) {
         BitsSet[1][i] = 0;
      }
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         if(Agents[i].techTrader) {
            for (int j = 0; j<Agents[i].numRules ; j++ ) {
               bits += Agents[i].ruleSet[j].tBits;
               for (int k = 0; k<32 ; k++ ) {
                  if ((Agents[i].ruleSet[j].conditionWords[1] >> Asset.SHIFT[k] & 3l ) > 0l ) {
                     BitsSet[1][k]++;
                  }
               }
            }
         }
      }
      return bits;
   }  // getTechnicalBits

   /* If consistency_check is active, the bitfractions don't have to come up to 1.0 if
      bitProb=1.0; The checking procedure does not ensure that always the maximum number
      of bits is set after the check, sometimes only one may be set.
   */
   protected static double getWorldFBitFraction() {
      double bitFraction = 0;
      double classifierUser = 0d;
      for (int i = 0 ; i < World.numberOfAgents ; i++) {
         if (Agents[i].useClassifier) {
            bitFraction += Agents[i].fProb;
            classifierUser += 1d;
         }
      }
      return bitFraction/classifierUser;
   }

   protected static double getWorldTBitFraction() {
      double bitFraction = 0;
      double classifierUser = 0d;
      for (int i = 0 ; i < World.numberOfAgents ; i++) {
         if (Agents[i].useClassifier) {
            bitFraction += Agents[i].tProb;
            classifierUser += 1d;
         }
      }
      return bitFraction/classifierUser;
   }  // getWorldTBitFraction()

   protected static double getWorldBitFraction() {
      int bitsInUse = 0;
      int maxBitsInUse = 0;
      for (int i = 0 ; i < World.numberOfAgents ; i++) {
         if (Agents[i].useClassifier) {
            bitsInUse += Agents[i].fBitsInUse ;
            maxBitsInUse += Agents[i].maxFBits*Agents[i].numRules ;
            if (Agents[i].techTrader) {
               bitsInUse += Agents[i].tBitsInUse;
               maxBitsInUse += Agents[i].maxTBits*Agents[i].numRules ;
            }
         }
      }
      return (double)bitsInUse/maxBitsInUse;
   }  // getWorldBitFraction

   public static double getMeanFitness() {
      double meanFitness = 0;
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         meanFitness += Agents[i].getMeanFitness();
      }
      // System.out.println("Mean Fitness in Economy :"+meanFitness/World.numberOfAgents );
      return meanFitness/World.numberOfAgents;
   }  // getMeanFitness
   public static double getMinFitness() {
      double minFitness = TradingRule.maxDev;
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         minFitness = Math.min(Agents[i].getMinFitness(),minFitness);
      }
      // System.out.println("Minimum Fitness in Economy :"+minFitness );
      return minFitness;
   }  // getMinFitness
   public static double getMaxFitness() {
      double maxFitness = 0;
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         maxFitness = Math.max(Agents[i].getMaxFitness(),maxFitness);
      }
      // System.out.println("Maximum Fitness in Economy :"+maxFitness );
      return maxFitness;
   }  // getMinFitness

   public static double getAverageWealth() {
      int totalWealth = 0;
        for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
          totalWealth += Agents[i].getWealth() ;
        }
      // System.out.println("Average Wealth of agents in the economy :"+totalWealth/World.numberOfAgents );
      return totalWealth/World.numberOfAgents;
   }  // getAverageWealth




   /**
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
   public String getBitsSetString(){
      String dataString = "";
      getFundamentalBits();
      getTechnicalBits();
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

   protected static double getWealthOfClassifierAgents() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if(Agents[i].useClassifier) {
            traders++;
            wealth += Agents[i].getWealth();
         }
      }
      if (traders==0) return 0d; else return wealth/traders;
   }
   protected static double getWealthOfNoClassifierAgents() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if(!Agents[i].useClassifier) {
            traders++;
            wealth += Agents[i].getWealth();
         }
      }
      if(traders==0) {
         return 0d;
      } else {
         return wealth/traders;
      }
   }


   /** Even for periodic dividend data there were numerous zero bit agents, even though
    *  I had assumed that almost all agents use some bits. However, since still most of them
    *  discard bit usage and some of them keep it, I wanted to analyze whether those who keep
    *  using bits are more suceesful than the zero bit agents.
    *
    */

   protected static double getWealthOfZeroBitAgents() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if(Agents[i].useClassifier) {
            if(Agents[i].fBitsInUse==0 && Agents[i].tBitsInUse==0) {
               traders++;
               wealth += Agents[i].getWealth();
            }
         }
      }
      if(traders==0) {
         return 0d;
      } else {
         return wealth/traders;
      }
   }

   protected static double getWealthOfNonZeroBitAgents() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if(Agents[i].nesfiAgent && Agents[i].useClassifier ) {
            if(Agents[i].fBitsInUse!=0 || Agents[i].tBitsInUse!=0) {
               traders++;
               wealth += Agents[i].getWealth();
            }
         }
      }
      if(traders==0) {
         return 0d;
      } else {
         return wealth/traders;
      }
   }


   protected static double getAverageWealthTechnicalTraders() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if(Agents[i].useClassifier && Agents[i].techTrader) {
            traders++;
            wealth += Agents[i].getWealth();
         }
      }
      if (traders==0) return 0d; else return wealth/traders;
   }
   protected static double getAverageWealthFundamentalTraders() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if(Agents[i].useClassifier && !Agents[i].techTrader) {
            traders++;
            wealth += Agents[i].getWealth();
         }
      }
      if (traders==0) return 0d; else return wealth/traders;
   }

   protected static double getWealthOfNormalLearner() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if (!Agents[i].isFastLearner()) {
            traders++;
            wealth += Agents[i].getWealth();
         }
      }
      if (traders==0) return 0d; else return wealth/traders;
   }

   protected static double getWealthOfFastLearner() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0; i < numberOfAgents ; i++) {
         if (Agents[i].isFastLearner()) {
            traders++;
            wealth += Agents[i].getWealth();
         }
      }
      if (traders==0) return 0d; else return wealth/traders;
   }

   protected static double getWealthOfSFIAgents() {
      double wealth = 0d;
      int traders=0;
      for (int i = 0 ; i < numberOfSFIAgents ; i++) {
         traders++;
         wealth += Agents[i].getWealth();
      }
      if (traders==0) return 0d; else return wealth/traders;
   }

   protected static double getWealthOfNESFIAgents() {
      double wealth = 0d;
      int traders=0;
      for (int i = numberOfSFIAgents ; i < numberOfAgents ; i++) {
         traders++;
         wealth += Agents[i].getWealth();
      }
      if (traders==0) return 0d; else return wealth/traders;
   }

   protected static void detBaseWealth() {
      baseWealth -= (Stocks.getPrice()*interestRate - Stocks.getDividend());
   }
   protected static double getBaseWealth() {
      return baseWealth;
   }
   protected static void setBaseWealth(double val) {
      baseWealth = val;
   }

   protected static double getLongTermHreeBaseWealth() {
      double hreePriceAverage;
      double f = Stocks.rho/(1+interestRate-Stocks.rho);
      double g = ((1+f)*(1-Stocks.rho)*Stocks.getDividendMean()
                  - Agents[0].getRiskAversion()*(1+f)*(1+f)*Stocks.getNoiseVar() )
                  / interestRate;
      double wealth = 0;
      hreePriceAverage = f * Stocks.getDividendMean()+g;
      wealth = Agents[0].initialCash - period*(hreePriceAverage*interestRate - Stocks.getDividendMean());
      return wealth;
   }

//   protected static void increaseNumberOfClassifierAgents() {
//      numberOfClassifierAgents++;
//   }
   protected static double getForecastMeanA() {
      double meanA = 0d;
      double totalrules = 0d;
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         for (int j = 0 ; j < Agents[i].numRules ; j++ ) {
            meanA += Agents[i].ruleSet[j].getAij();
            totalrules += 1;
            ;
         }  // for all rules of an agent
      }  // for all agents
      return meanA/totalrules;
   }  // getForecastMeanA()

   protected static void increaseNumberOfClassifierAgents() {
      numberOfClassifierAgents++;
   }
   protected static double getVarianceMean() {
      double meanVariance = 0d;
      double totalrules = 0d;
      for (int i = 0 ; i < World.numberOfAgents ; i++ ) {
         for (int j = 0 ; j < Agents[i].numRules ; j++ ) {
            meanVariance += Agents[i].ruleSet[j].getForecastVar();
            totalrules += 1;
            ;
         }  // for all rules of an agent
      }  // for all agents
      return meanVariance/totalrules;
   }  // getForecastMeanA()




   protected static void increaseZeroBitAgents() {
      numberOfZeroBitAgents++;
      if(numberOfZeroBitAgents==numberOfNESFIAgents && AsmModel.stopAtZeroBit) {
         // stop the simulation here, if I only could!!
         // if (AsmModel.recordData) AsmModel.recorder.record();
         if (AsmModel.stopAtZeroBit) AsmModel.stopNow = true;  // not nice, but it works
         // System.out.println("Stop");
      }
   }

   /**
    * Returns the number of classifier-agents that have discovered the zero bit solution
    */
   protected static int getZeroBitAgents() {
//      int agents = 0;
//      for (int i = 0 ; i < World.numberOfAgents ; i++) {
//         if (Agents[i].useClassifier) {
//            if (Agents[i].fBitsInUse==0 && Agents[i].tBitsInUse==0) {
//               agents++;
//            }
//         }
//      }
//      return agents;
   return numberOfZeroBitAgents;
   }  // zeroBitAgents


}