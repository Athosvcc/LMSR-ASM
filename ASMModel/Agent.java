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
   protected boolean staticAgent = false;
   protected static double MAXHOLDINGS = 1000000000; // no one can own or owe more than the absolute number of stocks available in the economy
   protected static int traders = 0;   // needed for ID-initialization in constructor

   protected static double riskAversion = 0.3;

   // Instance-Variables
   protected int ID ;
   protected double wealth = 0;
   protected static double MINCASH = -2000;
   protected double cash;
   protected double initialCash = 100.0;

   protected boolean pos;
   protected double optimalDemand;
   protected double order ; // = new double[World.differentStocks];
   protected double numberOfPosStocks;
   protected double numberOfNegStocks;
  protected double fcVar ; // = new double[World.differentStocks];
   protected double divisor ; // = new double[World.differentStocks];
   protected LMSRStock stockLMSR;
   protected Specialist specialist;
   protected int x;  // for graphical display
   protected int y;  // for graphical display

   protected double pdCoeff, offset ;
   protected double forecast ;
   protected double medFitness, meanFitness, meanVariance ;

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

   public void executeOrder() { // gets cost of order and adds stocks to current holdings of agents and of the system as a whole, as well as sets revenue and subtracts cash paid
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
     stockLMSR = World.LMSRStocks;
     if (cash < MINCASH) {
        cash = MINCASH;
     }
     wealth = cash + numberOfPosStocks*stockLMSR.getPrice() + numberOfNegStocks*stockLMSR.getPriceNoStock();// update wealth
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
      stockLMSR = World.LMSRStocks;
      offset = AsmModel.LMSRNormal.nextDouble();
      divisor = stockLMSR.getProbability()*(1-stockLMSR.getProbability());
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
      constrainDemand(trialPrice);    // make sure that budget constraints are not violated
   }	 // setDemandAndSlope


   /**
    * Constrains demand such that neither MINCASH nor MAXBID are violated for one single stock
    * This applies only if there is one stock to check.
   */
   public void constrainDemand(double trialPrice) {
     specialist = AsmModel.specialist;
     while (specialist.getCostLMSR(order, pos) > (cash - MINCASH)) {
        order--;
     }
   }  // constrainDemand

   public double getDemand() {
      return order;
   }

   public double getRiskAversion() {
      return this.riskAversion;
   }
   public void setRiskAversion(double val) {
      this.riskAversion = val;
   }
}