/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the TradingRule Class
*/

package ASMModel;

import java.lang.*;
import java.io.*;

import uchicago.src.sim.util.Random;
import uchicago.src.sim.engine.*;
import cern.jet.random.*;

/**
 * Every trading rule that an agent possesses is an actual instance of this class.
 * It has a lot of static variables for the GA and every rule handles its own
 * fitness calculation. The class TradingRule implements the Comparable interface
 * such that rules can compared and ranked according to their fitness.
 */
public class TradingRule implements Comparable, CustomProbeable {


   protected static double bitProb = 0.1;    // probability that a world bit is initialized to 0 or 1.
   protected static int maxSpecificity = 64 ;// there are at maximum 64 bits in a trading rule that can be set
   protected static double bitCost = 0.005;
   protected static double theta = 75.0;    // time healing parameter, parameter used by LeBaron et al.
   protected static double maxDev = 100.0;
   protected static double probMutation = .03; // LeBaron's et al. 1999 JEDC-paper: 0,03
   protected static double probLongJump = 0.2;
   protected static double probShortJump = 0.2;
   protected static double genFrac = 0.25;         // generalizing-probability for a bit
   protected static int maxNonActive = 4000;    // if longer than maxNonactive non activated replace it in the netx GA-run
   protected static int minCount = 5 ;           // minimum activations (and hence fitness updates) before a rule is actually used for a forecast
   protected static int generalizationCounter = 0;

   protected long[] conditionWords = new long[2];       // alternative implementation without the slow BitSet-concept
   /* a long Word (64 binary bits) can hold nBits=31 world bits. Two binary bits are needed to
      code a world bit. The last binary bit is used for sign, i.e. binary bits 62 and 63 are slack.
   */
   private static int ruleCounter = 0;    // how many rules are created in the economy as a whole
   private int ruleID = 0;
   protected static int nBits = 32 ;
   protected int fBits = 0;         // number of fundamental bits set
   protected int tBits = 0;         // number of technical bits set

   private double specificityCost ;       // added cost for specificity, i.e. the sum of bitCost*specificity
   private transient Stock stock;

   private static double subrange = .25;     // 0 = homogeneous agents
   private static double a_min = 0.7  ;      // 0.7
   private static double a_max = 1.2  ;
   private static double a_range = a_max - a_min ;
   private static double b_min = -10d;       // -10d
   private static double b_max = 19d ;       // 19d
   private static double b_range = b_max- b_min;
   private static double c_min = -1d ;
   private static double c_max = 1d  ;
   private static double c_range = c_max - c_min;
   private static double d_min = -1d ;
   private static double d_max = 1d  ;
   private static double d_range = d_max - d_min;
   private static double nhood = 0.05;
   protected static double initVar;

   protected int lastActive = 0;    // when was the rule activated for the last time? being activated means, that it matched the state-of-the-world, but it still had to be selected from the set of activated rules
   protected int lastUsed = 0;      // when was it used for the last time
   protected int usedCounter = 0;         // how often was this rule used, that i.e. activated and selected
   protected int activeCounter = 0;       // how often was this rule activated

   protected int birth = 0;         // when was this specific rule born, i.e. changed by the GA for the last-time?
   protected int specificity = 0;   // the number of bits that are set, i.e., not #
   protected int[][] BitsSet = new int[2][32];  // each position holds how often a particular bit is set in the rule set of this agent
   protected double fitness = 0;    // final fitness
   protected boolean technicalRule = true;
   private boolean useClassifier;
   private boolean checkRules;

   protected double[] forecastPart;
   protected double forecastVar;

   public TradingRule() {

   }

   public TradingRule(boolean Classifier, boolean technical, boolean ruleCheck) {
      technicalRule = technical;
      this.useClassifier = Classifier;
      this.checkRules = ruleCheck;
      ruleCounter++;
      ruleID = ruleCounter;
      forecastPart = new double[2];   // holds forecast-parameters a, b, c, and d
      forecastPart[0] = Specialist.reA + subrange*(a_max-a_min)*AsmModel.normalNormal.nextDouble();
      forecastPart[1] = Specialist.reB + subrange*(b_max-b_min)*AsmModel.normalNormal.nextDouble();
      initVar = (1 + Specialist.reF)*(1 + Specialist.reF)*World.Stocks.getNoiseVar();
      /* Initialize all conditions to don't care (#, coded as binary "00") */
      conditionWords[0] = 0l;
      conditionWords[1] = 0l;

      /* Now, initilize the individual rule bits with probability bitProb to either 0 or 1.
      */
      if(useClassifier) {
         for (int i = 0; i < (!technicalRule? 1 : 2 ); i++) {
            for (int j = 0; j < nBits; j++) {
               if (Random.uniform.nextDoubleFromTo(0d,1d) < bitProb) {
                  /* Store "value" for bit n with aState[WORD(n)] |= value << SHIFT[n];
                     if the stored value was previously 0
                  */
                  if (Random.uniform.nextDoubleFromTo(0d,1d) < 0.5) {
                     conditionWords[i] |= 1l << Asset.SHIFT[j];
                  } else {
                     conditionWords[i] |= 2l << Asset.SHIFT[j];
                  }
                  // now update bit world bit statistics, i.e. how often a particular bit is set in the economy
                  //World.BitsSet[i][j]++;
               }  // bit set with probability bitProb
            }  // for all nBits
         }  // end of initializing rule bits
//         for (int j = 25 ; j <32 ; j++ ) {  // set unused bits in the technical word to zero
//            conditionWords[1] = (conditionWords[1] & Asset.NMASK[j]) | (0l << Asset.SHIFT[j] );
//         }
//         // set the last unused bit in the technical word to zero
//         conditionWords[1] = (conditionWords[1] & Asset.NMASK[31]) | (0l << Asset.SHIFT[j] );
         if (useClassifier && checkRules) {
//            System.out.println("Initiated Rule before check:");
//            show_bits(0);
            specificity = check_consistency();
//            System.out.println("Initiated Rule after check:");
//            show_bits(0);
         } else {
            if(useClassifier) {
               specificity = getSpecificity();
            }
         }
      }  // initialize only when in classifier mode

      forecastVar = initVar ;
      fitness = detFitness();
   }  // constructor

   protected double getAij() {
      return forecastPart[0];
   }
   protected double getBij() {
      return forecastPart[1];
   }
   protected double getForecastVar() {
      return forecastVar;
   }

   /**
    * Calculates Fitness of this rule when activated. It is based on ForecastVariance.
    */

   protected void updateFitness() {
      double forecast ;
      double deviation ;
      double target ;
      double a, b, c ;

      a = 1.0/theta;
      b = 1f - a;

      if(World.period > 1) {
         if(World.period < theta) {
            forecastVar = initVar;
         } else {
            stock = World.Stocks;
            target = stock.price + stock.dividend; // stock.pricePlusDivHistory[stock.priceHistoryTop];
            forecast = (stock.oldPrice + stock.oldDividend)*forecastPart[0] + forecastPart[1];
            // System.out.println("Forecast : "+forecast + "  realized : "+target);
            deviation = (target - forecast)*(target - forecast);
            if (deviation > maxDev) deviation = maxDev;
//            if (activeCounter>theta) {
               forecastVar  = b * forecastVar + a * deviation ;
//            } else {
//               c = 1.0/(1+activeCounter);
//               forecastVar  = (1.0 - c) * forecastVar + c * deviation ;
//            }
         }
         fitness = detFitness();
      }  // no Fitness value for first period
   }  // updateFitness


   public void use() {
      usedCounter++;
      lastUsed = World.period;
      // System.out.println("Rule activated for the "+usedCounter+"th time");
   }

   protected int getSpecificity() {
      int spec = 0;
      fBits = 0;
      tBits = 0;
      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < nBits; j++) {
            if ((conditionWords[i] >> Asset.SHIFT[j] & 3l ) != 0l ) {
               spec++;
               if (i==0) {
                  fBits++;
                  BitsSet[i][j] = 1;
               } else {
                  tBits++;
                  BitsSet[i][j] = 1;
               }
            } else {
               BitsSet[i][j] = 0;
            }
         }
      }
      specificityCost = (spec - maxSpecificity ) * bitCost ; // thus, costs are negative
      specificity = spec;
      return spec;
   }  // getSpecificity()


   public boolean match() {
      stock = World.Stocks;
      if (( conditionWords[0] & stock.aState[0]) > 0) { return false; }
      if (technicalRule) {
         if (( conditionWords[1] & stock.aState[1]) > 0) { return false; }
      }
      lastActive = World.period ;
      activeCounter++;
      return true;
   }  // match


   /** A common problem in classifier systems is that some requirements on the condition
     *  bitstrings are not satisfied. For instance, take the 6-bit condition sequence
    *
    *  price*interestRate/dividend > 1/4, 1/2, 3/4, 1, 3/2, 2
    *
    *  If, for instance, bit 5 is set, then logically, bits 1 to 4 should be set, too.
    *  Until now, this problem has been neglected or circumvented by using larger rule sets.
    *  Those rules, which haven't been activated for a given period of time (because they
    *  had these logical inconsistencies), were send to a generalizing procedure which
    *  produced simply more don't cares # in the condition string. This increased the
    *  probability that the rule was logically consistent and could be activated.
    *
    * This check_consistency-procedure takes a different approach. Each time a rule has been
    * initialized or created by the GA, it is send to this procedure which ensures logical
    * consistency. It does so by sequentially checking the bit sequence from the beginning
    * (left or right) and then set the following bits such that these bits are in accordance
    * with the first bit that was set.
    *
    * However, it depends whether we start analyzing the bit sequence from the right or from
    * the left. In this model, we need to check, for instance the sequences bit 0-5
    * (div/div.-Mean > pdRatios[i]) and bits 6-11 (price*interestRate/dividend>pdRatios[i]).
    * If we start checking from bit 0 up to bit 5, the result is different than if we start
    * from bit 5 down to 0. How that? Take for instance the rule sequence
    *
    * {true, false, false, false, true, false}.
    *
    * I use the boolean values in order to avoid confusion with the different coding schemes.
    * This means that the considered value is bigger than 1/4, smaller than 1/2, 3/4, 1, and
    * then bigger than 3/2 (which is obviously not consistent with the other three bits
    * before) and then smaller than 2.
    *
    * Procedure starting from bit 0 up to 5:
    * It may well be that the value is bigger than 1/4, but smaller than 1/2. But then, the
    * value is also smaller than 3/4, 1, 3/2, and 2. Hence, the resulting condition-String is
    * {true, false, false, false, false, false}
    *
    * Procedure starting from bit 5 down to 0:
    * If the value is smaller than 2, but bigger than 3/2, it is clear that a logical bit
    * sequence should be changed to
    * {true, true, true, true, true, false}
    *
    * In order to avoid systematic favoring of one or the other method (which is equal of
    * favoring either high or low ratios), both ways are chosen with a probability of 1/2.
    *
    * Additionally, within these sequences we need only one significant bit, either the
    * highest or lowest non-zero bit. All other bits are set to don't cares (=binary zeros).
    * The same information content of that rule can be coded without increasing the
    * specificity of that rule.
   */
   protected int check_consistency () { //coding: rule:value 0:#    1:0(false)   2:1(true)
      // System.out.println("Do Consistency Check!");
      int bitPos = -1;
      // System.out.print("After Mutation / Before Consistency Check: "); show_bits(0);
      if (Random.uniform.nextDoubleFromTo(0f,1f) <= 0.5) {  // check with equal prob. either from higher bits down or vice versa
         /* Check consistency starting from the higher bits down to zero */
         for (int j = 6 ; j >= 0 ; j-- ) {  // check (dividend/dividendMean > pdRatio)-sequence
            if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 2l && j>0) {  // if true, all lower bits to #
               for (int i = j-1; i > -1; i--) { // then set all other lower bits to don't care
                  conditionWords[0] = (conditionWords[0] & Asset.NMASK[i]) | (0l << Asset.SHIFT[i] );
               }
               break;
            } else { // if not true set
               if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 1l ) {  // if false, keep pos. in mind; if more false are coming on the lower pos., change this pos. to #
                  if(bitPos>0) { // there is a false higher up which is not necessary,
                     // hence, change that unnecessary false to #
                     conditionWords[0] = (conditionWords[0] & Asset.NMASK[bitPos]) | (0l << Asset.SHIFT[bitPos] );
                  }
                  bitPos = j; // now keep that position in mind
               }
            }
         }
      } else {
         /* Now check consistency starting from the lower bits */
         for (int j = 0 ; j < 7 ; j++ ) {  // check (dividend/dividendMean > pdRatio)-sequence
            if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 1l && j<6) { // if false, set all higher bits to #
               for (int i = j+1; i < 7 ; i++) { // then set all higher bits to don't care
                  conditionWords[0] = (conditionWords[0] & Asset.NMASK[i]) | (0l << Asset.SHIFT[i] );
               }
               break;
            } else { // if not false
               if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 2l ) {  // if true, keep that pos. in mind; if more trues are comin up, set this true to #
                  if(bitPos>=0) { // there is a true lower down which is not necessary,
                     // hence, change that unnecessary true to #
                     conditionWords[0] = (conditionWords[0] & Asset.NMASK[bitPos]) | (0l << Asset.SHIFT[bitPos] );
                  }
                  bitPos = j; // now keep that position in mind
               }
            }
         }
      }  // end with checking the first bit sequence (bits 0 to 6)
      bitPos = -1;
      if (Random.uniform.nextDoubleFromTo(0f,1f) <= 0.5) {  // now check the f. bit sequence (bits 7 to 13)
         for (int j = 13 ; j >= 7 ; j-- ) {  // check (price*interestRate/dividend>pdRatio)-sequence
            if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 2l && j>7 ) {   // if true, all lower bits to #
               for (int i = j-1; i > 6; i--) {
                  conditionWords[0] = (conditionWords[0] & Asset.NMASK[i]) | (0l << Asset.SHIFT[i] );
               }
               break;
            } else { // if not true
               if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 1l ) {   // if false, keep pso. in mind; if more falses are coming up on the lower pos., change this bit to #
                  if(bitPos>0) { // there was a false higher up which is not neccessary,
                     // hence, change that unnecessary false to #
                     conditionWords[0] = (conditionWords[0] & Asset.NMASK[bitPos]) | (0l << Asset.SHIFT[bitPos] );
                  }
                  bitPos = j; // now keep that pos. in mind
               }
            }
         }
      } else { // noch check that sequence starting from the lower bits
         for (int j = 7 ; j <= 13 ; j++ ) {  // check (price*interestRate/dividend>pdRatio)-sequence
            if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 1l && j<13) {  // if false, set all higher bits to #
               for (int i = j+1; i <= 13; i++) {
                  conditionWords[0] = (conditionWords[0] & Asset.NMASK[i]) | (0l << Asset.SHIFT[i] );
               }
               break;
            } else { // if not false
               if ((conditionWords[0] >> Asset.SHIFT[j] & 3l ) == 2l ) {  // if true, keep that pos. in mind; if more trues are comin up, set this true to #
                  if(bitPos>=0) { // there is a true lower down which is not necessary,
                     // hence, change that unnecessary true to #
                     conditionWords[0] = (conditionWords[0] & Asset.NMASK[bitPos]) | (0l << Asset.SHIFT[bitPos] );
                  }
                  bitPos = j; // now keep that position in mind
               }
            }
         }
      }
      if (technicalRule) {
         bitPos = -1;
         if (Random.uniform.nextDoubleFromTo(0f,1f) <= 0.5) {
            /* Check consistency starting from the higher bits down to zero */
            for (int j = 6 ; j >= 0 ; j-- ) {  // check (dividend/dividendMean > pdRatio)-sequence
               if ((conditionWords[1] >> Asset.SHIFT[j] & 3l ) == 2l && j>0) {  // if true, all lower bits to #
                  for (int i = j-1; i > -1; i--) { // then set all other lower bits to don't care
                     conditionWords[1] = (conditionWords[1] & Asset.NMASK[i]) | (0l << Asset.SHIFT[i] );
                  }
                  break;
               } else { // if not true set
                  if ((conditionWords[1] >> Asset.SHIFT[j] & 3l ) == 1l ) {  // if false, keep pos. in mind; if more false are coming on the lower pos., change this pos. to #
                     if(bitPos>0) { // there is a false higher up which is not necessary,
                        // hence, change that unnecessary false to #
                        conditionWords[1] = (conditionWords[1] & Asset.NMASK[bitPos]) | (0l << Asset.SHIFT[bitPos] );
                     }
                     bitPos = j; // now keep that position in mind
                  }
               }
            }
         } else {
            /* Now check consistency starting from the lower bits */
            for (int j = 0 ; j < 7 ; j++ ) {  // check (dividend/dividendMean > pdRatio)-sequence
               if ((conditionWords[1] >> Asset.SHIFT[j] & 3l ) == 1l && j<6) { // if false, set all higher bits to #
                  for (int i = j+1; i < 7 ; i++) { // then set all higher bits to don't care
                     conditionWords[1] = (conditionWords[1] & Asset.NMASK[i]) | (0l << Asset.SHIFT[i] );
                  }
                  break;
               } else { // if not false
                  if ((conditionWords[1] >> Asset.SHIFT[j] & 3l ) == 2l ) {  // if true, keep that pos. in mind; if more trues are comin up, set this true to #
                     if(bitPos>=0) { // there is a true lower down which is not necessary,
                        // hence, change that unnecessary true to #
                        conditionWords[1] = (conditionWords[1] & Asset.NMASK[bitPos]) | (0l << Asset.SHIFT[bitPos] );
                     }
                     bitPos = j; // now keep that position in mind
                  }
               }
            }
         }  // end with checking the bit sequence (bits 0 to 6) in conditionWords[1]

//         for (int j = 25 ; j <32 ; j++ ) {  // set unused bits to zero
//            conditionWords[1] = (conditionWords[1] & Asset.NMASK[j]) | (0l << Asset.SHIFT[j] );
//         }

      }  // checking for technical bits
      return getSpecificity();
   }  // check_consistency_new



   /**
    * Even though we made sure by check_consistency that there are no illogical rules,
    * it turned out that in an simulation run there is an increasing fraction of rules
    * which are not activated. Those are generalized in this procedure , i.e. bits set
    * are changed to zero with a certain probability.
    */
   protected void generalize( double newFitness) {
//      System.out.println("Offspring before generalization");
//      show_bits(0);
      generalizationCounter++;
      for (int i = 0; i < (!technicalRule ? 1 : 2 ); i++) {
         for (int j = 0; j < nBits; j++) {
            if ((conditionWords[i] >> Asset.SHIFT[j] & 3l ) != 0l ) {
               if(Random.uniform.nextDoubleFromTo(0d,1d) < genFrac) {
                  conditionWords[i] &= Asset.NMASK[j];
               }
            }
         }
      }
//      System.out.println("Offspring after generalization");
//      show_bits(0);
      lastActive = World.period;
      lastUsed = World.period;
      usedCounter = 0;
      birth = World.period;
      specificity = getSpecificity();
      fitness = newFitness;
   }  // generalize


   /**
    * Does a lexicographic ordering. First order criterion is whether the rules have not been
    * activated during the last maxNonActive periods. If not activated, a rule is considered
    * to be smaller. This ensures that rules which haven't beeen activated for the last
    * maxNonActive periods are sorted at the end of the array and are repalced by the
    * genetic algorithm first.
    *
    * Second order criterion is fitness.
    */
   public int compareTo(Object o) {
      int ret = 0;
      if ((World.period - lastActive > maxNonActive) && ( World.period - lastActive > ((TradingRule)o).maxNonActive  )) {
         if (fitness < ((TradingRule)o).getFitness()) {
            ret = -1;
         } else {
            ret = 1;
         }
      } else if ((World.period - lastActive > maxNonActive) && ( World.period - lastActive < ((TradingRule)o).maxNonActive ) ) {
          ret = 1;
      } else if ((World.period - lastActive < maxNonActive) && ( World.period - lastActive > ((TradingRule)o).maxNonActive ) ) {
         ret = -1;
      } else {
         if (fitness < ((TradingRule)o).getFitness()) {
            ret = -1;
         } else {
            ret = 1;
         }
      }
      return ret;
   }  // CompareTo

   protected void mutateSFI(double medianVariance) {
      long bit;
      double prob, temp;
      boolean changed = false;
      if(useClassifier) {
         for (int i = 0; i < (technicalRule? 2 : 1 ); i++) {
            for (int j = 0; j < nBits; j++) {
               if(Random.uniform.nextDoubleFromTo(0d,1d) < probMutation) { // just change the bit with probablity proMutation
                  bit = (conditionWords[i] >> Asset.SHIFT[j]) & 3l ;   // extract bit
                  switch ((int)bit) {
                     case 0:  // bit = 0 zero means in conditionWords "don't care"
                        if(Random.uniform.nextIntFromTo(0,2) == 0 ) { // with prob 1/3 set # to 1
                           conditionWords[i] |= 1l << Asset.SHIFT[j];
                           changed = true;
                        } else if(Random.uniform.nextIntFromTo(0,2) == 1 ) { // with prob. 1/3 set # to 2
                           conditionWords[i] |= 2l << Asset.SHIFT[j];
                           changed = true;
                        }
                        break;
                     case 1:
                        if(Random.uniform.nextIntFromTo(0,2) > 0 ) { // with prob 2/3 set 1 to #
                           conditionWords[i] &= Asset.NMASK[j];
                           changed = true;
                        } else { // with prob. 1/3 set 1 to 2
                           conditionWords[i] = (conditionWords[i] & Asset.NMASK[j]) | (2l << Asset.SHIFT[j]);
                           changed = true;
                        }
                        break;
                     case 2:
                        if(Random.uniform.nextIntFromTo(0,2) > 0 ) { // with prob 2/3 set 2 to #
                           conditionWords[i] &= Asset.NMASK[j];
                           changed = true;
                        } else { // with prob. 1/3 set 2 to 1
                           conditionWords[i] = (conditionWords[i] & Asset.NMASK[j]) | (1l << Asset.SHIFT[j]);
                           changed = true;
                        }
                     break;
                  }  // switch bit
               }  // actually change the bit with certain prob.
            }  // for all bits
         }  // mutate all conditionWords
//         for (int j = 25 ; j <32 ; j++ ) {  // set unused bits in the technical word to zero
//            conditionWords[1] = (conditionWords[1] & Asset.NMASK[j]) | (0l << Asset.SHIFT[j] );
//         }
      }  // do bit mutation only when in classifier mode
      // now, after we've changed the condition-bits, we change the forecast-parameters
      prob = Random.uniform.nextDoubleFromTo(0d,1d);
      if(prob < probLongJump) {  // change the a-parameter
         forecastPart[0] = a_min + a_range*Random.uniform.nextDoubleFromTo(0d,1d);
         changed = true;
      } else if (prob < probLongJump + probShortJump) {
         temp = forecastPart[0] + a_range*nhood*Random.uniform.nextDoubleFromTo(0d,1d);
         if(temp<a_min) {
            temp = a_min;
         } else if (temp > a_max) {
            temp = a_max;
         }
         forecastPart[0] = temp;
         changed = true;
      }  // else leave unchanged
      prob = Random.uniform.nextDoubleFromTo(0d,1d);
      if(prob < probLongJump) {  // now change the b-parameter
         forecastPart[1] = b_min + b_range*Random.uniform.nextDoubleFromTo(0d,1d);
         changed = true;
      } else if (prob < probLongJump + probShortJump) {
         temp = forecastPart[1] + b_range*nhood*Random.uniform.nextDoubleFromTo(0d,1d);
         if(temp<b_min) {
            temp = b_min;
         } else if (temp > b_max) {
            temp = b_max;
         }
         forecastPart[1] = temp;
         changed = true;
      }  // else leave unchanged
      if (changed) { // set rule strength to median-strength, should be given as a parameter
         forecastVar = medianVariance;
         if(useClassifier) {
            if (checkRules) {
               specificity = check_consistency();
            } else {
               specificity = getSpecificity();
            }
         }else {
            specificity = 0;
         }
         fitness = detFitness();
      }  // changed ?
   }  // mutateSFI()

   /**
   *  This is the updated mutation operator that works with dynamically adjusting bit
   *  transition probabilities. The original SFI-mutator had fixed transition probabalities
   *  that usually introduced an upward bias in the level of bits set. It was not neutral
   *  to the initial level of bits set. In fact, this old SFI-mutator twisted most results
   *  to the ones described in the papers. For instance, the more often the old mutation
   *  operator was called, the higher was the equilibrium level of bits set. Thus,
   *  increasing the learning speed by shortening the GA invocation interval ended up in
   *  a higher level of bits set. So far, this has been interpreted as emergent technical
   *  trading, yet it is nothing else than a purely technical influence, i.e., not fitness
   *  driven. For a thorough discussion, see my working paper.
   *
   *  To allow for divergent probabilities for technical and fundamental bits, the updated
   *  NESFI-mutation operator works with two transition probabilities which are given as
   *  parameters fProb and tProb. They are determined for each agent individually over his
   *  complete rule set. If he owns 100 rules, I count the total number of bits set and
   *  divide it by the maximum number of bits (maxTBits, maxFBits) that can be set. This
   *  updating happens right after a GA has been invoked on the agents rule set, for all
   *  other periods those agent variables do not change.
   */
   protected void mutateNESFI(double medianVariance, double fProb, double tProb) {
      long bit;
      double prob, temp;
      double cProb;    // dynamic transition probabilities
      boolean changed = false;
//      System.out.print("NESFI-M.");
      if (useClassifier) {
         for (int i = 0; i < (technicalRule? 2 : 1 ); i++) {   // technical rules have two words that hold condition bits
            if(i==0) {cProb = fProb; } else {cProb = tProb; }  // word 0 holds fundamental bits, word 1 holds technical bits
            for (int j = 0; j < nBits; j++) {                  // nBits = number of bits (trits) that can be set = 31)
               if(Random.uniform.nextDoubleFromTo(0d,1d) < probMutation) { // just change the bit with probablity proMutation
                  bit = (conditionWords[i] >> Asset.SHIFT[j]) & 3l ;   // extract bit
                  temp = Random.uniform.nextDoubleFromTo(0d,1d);
                  switch ((int)bit) {
                     case 0:  // bit = 0 zero means in conditionWords "don't care"
                        if( temp < cProb/2 ) { // with prob ?Prob/2 set # to 1
                           conditionWords[i] |= 1l << Asset.SHIFT[j];
                           changed = true;
                        } else if(temp <= cProb ) { // with ?Prob. set # to 2
                           conditionWords[i] |= 2l << Asset.SHIFT[j];
                           changed = true;
                        }  // else leave unchanged with 1-?Prob
                        break;
                     case 1:
                        if( temp <= (1-cProb) ) { // with prob (1-cProb) set 1 to #
                           conditionWords[i] &= Asset.NMASK[j];
                           changed = true;
                        } else { // with prob. cProb set 1 to 2
                           conditionWords[i] = (conditionWords[i] & Asset.NMASK[j]) | (2l << Asset.SHIFT[j]);
                           changed = true;
                        }
                        break;
                     case 2:
                        if( temp < (1-cProb) ) { // with prob (1-cProb) set 2 to #
                           conditionWords[i] &= Asset.NMASK[j];
                           changed = true;
                        } else { // with prob. cProb set 2 to 1
                           conditionWords[i] = (conditionWords[i] & Asset.NMASK[j]) | (1l << Asset.SHIFT[j]);
                           changed = true;
                        }
                     break;
                  }  // switch bit
               }
            }  // for all bits
         }  // mutate all conditionWords
//         for (int j = 25 ; j <32 ; j++ ) {  // set unused bits in the technical word to zero
//            conditionWords[1] = (conditionWords[1] & Asset.NMASK[j]) | (0l << Asset.SHIFT[j] );
//         }   // not in 64Bit-mode
         // now, after we've changed the condition-bits, we change the forecast-parameters
      }  // do mutation only when in classifier mode
      // the rest is as in the original SFI-ASM
      prob = Random.uniform.nextDoubleFromTo(0d,1d);
      if(prob < probLongJump) {  // change the a-parameter
         forecastPart[0] = a_min + a_range*Random.uniform.nextDoubleFromTo(0d,1d);
         changed = true;
      } else if (prob < probLongJump + probShortJump) {
         temp = forecastPart[0] + a_range*nhood*Random.uniform.nextDoubleFromTo(0d,1d);
         if(temp<a_min) {
            temp = a_min;
         } else if (temp > a_max) {
            temp = a_max;
         }
         forecastPart[0] = temp;
         changed = true;
      }  // else leave unchanged
      prob = Random.uniform.nextDoubleFromTo(0d,1d);
      if(prob < probLongJump) {  // now change the b-parameter
         forecastPart[1] = b_min + b_range*Random.uniform.nextDoubleFromTo(0d,1d);
         changed = true;
      } else if (prob < probLongJump + probShortJump) {
         temp = forecastPart[1] + b_range*nhood*Random.uniform.nextDoubleFromTo(0d,1d);
         if(temp<b_min) {
            temp = b_min;
         } else if (temp > b_max) {
            temp = b_max;
         }
         forecastPart[1] = temp;
         changed = true;
      }  // else leave unchanged
      if (changed) { // set rule strength to median-strength, should be given as a parameter
         forecastVar = medianVariance;
         if(useClassifier) {
            if (checkRules) {
               specificity = check_consistency();
            } else {
               specificity = getSpecificity();
            }
         } else {
            specificity = 0;
         }
         fitness = detFitness();
      }  // changed ?
   }  // mutateNESFI()


   /**
    * Just a function to visualize the content of the condition words. There are certain
    * substrings of the condition words which have to fulfill certain logical conditions,
    * so these substring are surrounded by |.
    */
   protected void show_bits(int mode) {
      String bitString0, bitString1, temp;
      long val;
      if (mode==0) {
         bitString0 = "Word 0: ";
         bitString1 = "Word 1: ";
         for (int i = 0; i < (!technicalRule ? 1 : 2 ); i++) {
            temp="";
            for (int j = 0; j < nBits; j++) {
               val = ((conditionWords[i] >> Asset.SHIFT[j]) & 3l);
               if (val==0l) {
                  temp += "#";
               } else if(val == 1l) {
                  temp += "0";
               } else {
                  temp += "1";
               }
               // if(j==6 || j==13 || j==24) {temp += "|";}  // group the bit sequences, differs in the 64 and 57 bit version
            }
            if(i==0) {bitString0 += temp;} else {bitString1 += temp;}
         }
         System.out.print(bitString0+"  ");
         if (technicalRule) {
            System.out.println(bitString1);
         }
      } else {

      }
   }  // show_bits

   protected double detFitness() {
      // System.out.println("specificity: "+getSpecificity()+"Bitcost "+bitCost+"  Spec.-cost: "+specificityCost + "  fitness: "+(maxDev - forecastVar - specificityCost));
      return maxDev - forecastVar - specificityCost;

   }

   protected double getFitness() {
      return fitness;
   }

   protected static int getNBits() {
      return nBits-1;
   }

   public double getBitProb() { return bitProb; }
   public void setBitProb(double val) { bitProb = val; }
   public int getMaxSpecificity() { return maxSpecificity; }
   public void setMaxSpecificity(int val) { maxSpecificity = val; }
   public double getBitCost() { return bitCost; }
   public void setBitCost(double val) { bitCost = val; }
   public double getTheta() { return theta; }
   public void setTheta(double val) { theta = val; }
   public double getMaxDev() { return maxDev; }
   public void setmaxDev(double val) { maxDev = val; }
   public double getProbMutation() { return probMutation; }
   public void setProbMutation(double val) { probMutation = val; }
   public double getProbLongJump() { return probLongJump; }
   public void setProbLongJump(double val) { probLongJump = val; }
   public double getProbShortJump() { return probShortJump; }
   public void setProbShortJump(double val) { probShortJump = val; }
   public double getGenFrac() { return genFrac; }
   public void setGenFrac(double val) { genFrac = val; }
   public double getSubrange() { return subrange; }
   public void setSubrange(double val) { subrange = val; }
   public double getNhood() { return nhood; }
   public void setNhood(double val) { nhood = val; }
   public int getMaxNonActive() { return maxNonActive; }
   public void setMaxNonActive(int val) { maxNonActive = val; }
   public int getMinCount() { return minCount; }
   public void setMinCount(int val) { minCount = val; }

   public String[] getProbedProperties() {
      return new String[] {"bitProb","maxSpecificity","bitCost","theta",
         "maxDev","probMutation","probLongJump","probShortJump","genFrac",
         "subRange","nhood","maxNonActive","minCount"};
   }

   protected int getRuleID() {
      return ruleID;
   }
   protected static int getRuleCounter() {
      return ruleCounter;
   }
   protected static void setRuleCounter(int val) {
      ruleCounter = val;
   }
//
//
//   protected void check_SFIRules() {
//
//   }

   /*
    * This function returns a string which contains the numbers of bits set for each
    * position. It is separated by ; to match the header in the recorder function with
    * which it is synchronized. The header looks like this
    * "F0  ;F1 ;...;T0  ;T31" etc, and the given to the DataRecorder to be written in the file is
    * "0;0;1;1;".
    * Thus, if properly synchronized, the data file can be still imported into EXCEL and
    * analyzed. I have choosen this way inorder to avoid 40 separate function calls to
    * write each bit. The Write function is called just once, and it gets its data to write
    * from this function.
    *
    */
   public String getTradingRuleBitsSetString(){
      String dataString = "";
      for (int i = 0; i < 2 ;i++ ) {
         for (int j = 0; j < 32 ; j++ ) {
            dataString += String.valueOf(BitsSet[i][j])+";";
         }
      }
      return dataString.substring(0,dataString.length()-1);
   }

}