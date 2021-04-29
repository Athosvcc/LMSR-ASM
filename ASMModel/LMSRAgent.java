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

   public LMSRAgent(int traderType) {
      if (traderType > -1 ) { // real trader, not the static one to adjust some parameters
         traders++;
         this.ID = traders;
         cash = initialCash;
         stockLMSR = World.LMSRStocks;
         wealth = cash + numberOfPosStocks*stockLMSR.getPrice() + numberOfNegStocks*stockLMSR.getPriceNoStock();
         World.setTotalWealth(World.getTotalWealth()+wealth);
      } else { // actual instance of a trader which is only used to set the static parameters through the gui
         staticAgent = true;
         // no endowment with stocks
      }
   }  // constructor


   public String[] getProbedProperties() {
      if (!staticAgent) {
               return new String[] {"ID","wealth","meanFitness","minFitness","maxFitness","fundamentalBitsSet","technicalBitsSet","cash","numberOfStock"};
         } else { // set static properties for all properties with this actual instance of a trader
             return new String[] {"numRules","initialCash","minCash","riskAversion","replaceXXRules","probCrossover","minActiveRules","checkRules"};
         }
   }


//   public String getName() {
//      return "NESFI-Agent";
//   }


}