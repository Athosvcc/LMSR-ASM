/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
*/

package ASMModel;

import java.awt.*;
import java.awt.event.*;
import java.awt.Graphics;
import java.util.*;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;

// import java.util.ArrayList;
// import java.util.ListIterator;
// import java.util.Collections;

import uchicago.src.sim.engine.*;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.space.*;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.event.CheckBoxListener;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.reflector.BooleanPropertyDescriptor;
import uchicago.src.sim.analysis.*;
import uchicago.src.sim.util.Random;

import cern.jet.random.*;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import javax.swing.*;
import cern.jet.math.Functions.*;


/**
 * This is the main file of the NESFI-ASM. It has all the Repast specific stuff in it
 * and controls the model.
 */
public class AsmModel extends SimModelImpl {

   protected static World world;
   protected static Specialist specialist;
   protected static boolean showDisplays = true;
   protected static boolean recordData = false ;
   private static String recorderParamFile = "recorder.pf" ;
   protected static boolean stopAtZeroBit = true ;  // stops the simulation when the zero bit level is reached,
   private static boolean tBitsZero=false;   // recording only once the period that technical bits approached zero in conditionalStop()
   private static boolean fBitsZero=false;   // dito
   protected static boolean hree = false ;
   protected static boolean LMSR = true ; //mudar
   protected static OpenSequenceGraph logReturnGraph;
   protected static OpenSequenceGraph priceGraph;
   protected static OpenSequenceGraph hreePrice_PriceGraph;
   protected static OpenSequenceGraph correlationGraph;
   protected static OpenSequenceGraph bitUsageGraph;
   protected static OpenSequenceGraph volumeGraph;
   protected static OpenSequenceGraph wealthGraph;
   protected static OpenSequenceGraph MMGraph;
   protected static OpenSequenceGraph LMSRGraph;
   protected static BitDistributionPlot bitDistributionPlot;

   private Object2DTorus agentWorld;   // to display and probe the agents
   protected static DisplaySurface dsurf;
   protected static DataRecorder recorder;
   protected static RecorderOptions recorderOptions;
   protected static TradingRule tradingRule;
   protected SFIAgent staticSFIAgent;
   protected NESFIAgent staticNESFIAgent;
   protected LMSRAgent staticLMSRAgent;

   protected static ObserverOptions observer;
   private Schedule schedule;
   protected static Normal stockNormal;
   protected static Normal normalNormal;
   protected static Normal LMSRNormal;
   private static long rngSeed;
   private static RandomElement generator;
   private static final int DIGITS = 4;
   protected Stock stock;
   protected LMSRStock stockLMSR;
   protected static boolean stopNow = false;

   public AsmModel () {
      // adds boolean checkboxes to the NESFI-Settings windows
      BooleanPropertyDescriptor bd2 = new BooleanPropertyDescriptor("RecordData", true);
      descriptors.put("RecordData", bd2);
      BooleanPropertyDescriptor bd3 = new BooleanPropertyDescriptor("Hree", true);
      descriptors.put("Hree", bd3);
//      BooleanPropertyDescriptor bd5 = new BooleanPropertyDescriptor("CheckRules", true);
//      descriptors.put("CheckRules", bd5);
      BooleanPropertyDescriptor bd4 = new BooleanPropertyDescriptor("StopAtZeroBit", true);
      descriptors.put("stopAtZeroBit", bd4);
      BooleanPropertyDescriptor bd5 = new BooleanPropertyDescriptor("LMSR", true);
      descriptors.put("LMSR", bd5);

      // adds a List to choose an option
      Hashtable h3 = new Hashtable();
      h3.put(new Integer(Agent.SELECT_BEST),    "Select Best Rule");
      h3.put(new Integer(Agent.SELECT_AVERAGE), "Select Average");
      h3.put(new Integer(Agent.SELECT_ROULETTE),"Roulette-Wheel");
      ListPropertyDescriptor pd3 = new ListPropertyDescriptor("SelectionMethod", h3);
      descriptors.put("SelectionMethod", pd3);

      // create objects only for the sake of getting these options out of the main
      // NESFI-Settings windows. Repast requires actual objects that can be probed.
      observer = new ObserverOptions();
      recorderOptions = new RecorderOptions();
      try {
         new RecorderParamFileReader(recorderParamFile);
      }
      catch (FileNotFoundException ex) {
      }



      // create objects to set parameters that are common to all other
      // instances of these classes.
      stock = new Stock();
      stockLMSR = new LMSRStock();
      tradingRule = new TradingRule();   // create tradingrule to set static parameters
      staticSFIAgent = new SFIAgent(-1);     // create "pseudo-static" agent to set static parameters for SFI-Agents
      staticNESFIAgent = new NESFIAgent(-1);     // create "pseudo-static" agent to set static parameters for NESFI-Agents
      staticLMSRAgent = new LMSRAgent(-1);
   }  // constructor AsmModel

   private void buildModel() {

      // now create 3 Random-Generators,
      // Uniform is heavily used in the GA
      // stockNormal creates the noise for the dividend-process
      // normalNormal is used when trading rules are created
      // rngSeed is set by the gui
      Random.createUniform();
      normalNormal = new Normal(0.0, 1.0, new MersenneTwister((int)getRngSeed()) );
      LMSRNormal = new Normal(0.0, 0.05, new MersenneTwister((int)getRngSeed()) ); // as set in Prediction Market Liquidity
      stockNormal =  new Normal(0.0, Math.sqrt(stock.noiseVar), new MersenneTwister((int)getRngSeed()) );
      stock.initialize();
      stockLMSR.initialize();

      world = new World();
      World.Stocks = stock;
      World.LMSRStocks = stockLMSR;
      specialist = new Specialist();
      World.createAgents();
      if (!LMSR) {
         Agent.initMinMaxs();
      }
      if (recordData) {	// writes data to an ascii-file
         recorder = new DataRecorder(recorderOptions.getRecorderOutputFile(), this, "Data Recording of NEFSI-ASM" );
         recorder.setDelimiter(";");
         if (LMSR) {
            if (recorderOptions.getPrice()) {
               class WriteYesPrice implements NumericDataSource {
                  public double execute() {
                     return stockLMSR.getPrice();
                  }
               }
               class WriteNoPrice implements NumericDataSource {
                  public double execute() { return stockLMSR.getPriceNoStock(); }
               }
               recorder.addNumericDataSource("Price of Yes Stock", new WriteYesPrice(), 3, 2);
               recorder.addNumericDataSource("Price of No Stock", new WriteNoPrice(), 3, 2);
            } // if scheduled to record stock price(s)
            if (recorderOptions.getQuantityLMSR()) {
               class WriteYesQuantity implements NumericDataSource {
                  public double execute() {
                     return stockLMSR.getQPosLMSR();
                  }
               }
               class WriteNoQuantity implements NumericDataSource {
                  public double execute() { return stockLMSR.getQNegLMSR(); }
               }
               recorder.addNumericDataSource("Quantity of Yes Stock", new WriteYesQuantity(), 3, 2);
               recorder.addNumericDataSource("Quantity of No Stock", new WriteNoQuantity(), 3, 2);
            } // if scheduled to record stock quantity
            if (recorderOptions.getMarketMakerRevenue()) {
               class WriteMarketMakerRevenue implements NumericDataSource {
                  public double execute() {
                     return specialist.getSpecialistRevenue(); }
               }
               recorder.addNumericDataSource("Market Maker Revenue", new WriteMarketMakerRevenue(), 3, 2);
            } // if scheduled to record MM Revenue
            if (recorderOptions.getMarketMakerLiabilities()) {
               class WriteProbability implements NumericDataSource {
                  public double execute() {
                     return stockLMSR.getProbability();
                  }
               }
               recorder.addNumericDataSource("Probability", new WriteProbability(), 3, 2);
            } // if scheduled to record Probability
            if (recorderOptions.getMarketMakerLiabilities()) {
               class WriteMarketMakerLiabilities implements NumericDataSource {
                  public double execute() {
                     return specialist.getSpecialistLiabilities();
                  }
               }
               recorder.addNumericDataSource("Market Maker Liabilities", new WriteMarketMakerLiabilities(), 3, 2);
            } // if scheduled to record MM Liabilities
            if (recorderOptions.getMarketMakerProfit()) {
               class WriteMarketMakerProfit implements NumericDataSource {
                  public double execute() {
                     return specialist.getSpecialistProfit();
                  }
               }
               recorder.addNumericDataSource("Market Maker Profit", new WriteMarketMakerProfit(), 3, 2);
            } // if scheduled to record MM Profit

         } else { // end LMSR
            if (recorderOptions.getDividend()) {
               class WriteDividend implements NumericDataSource {
                  public double execute() {
                     return stock.getDividend();
                  }
               }
               recorder.addNumericDataSource("Dividend", new WriteDividend(), 2, 2);
            } // if scheduled to record stock dividend(s)

            if (recorderOptions.getNumberOfGeneralizations()) {
               class WriteGeneralizations implements NumericDataSource {
                  public double execute() {
                     return TradingRule.generalizationCounter;
                  }
               }
               recorder.addNumericDataSource("Generalizations", new WriteGeneralizations(), 7, 0);
            } // if scheduled to record stock price(s)

            if (recorderOptions.getPrice()) {
               class WritePrice implements NumericDataSource {
                  public double execute() {
                     return stock.getPrice();
                  }
               }
               recorder.addNumericDataSource("Price", new WritePrice(), 3, 2);
            } // if scheduled to record stock price(s)
            if (recorderOptions.getHreePrice()) {
               class WriteHreePrice implements NumericDataSource {
                  public double execute() {
                     return stock.getHreePrice();
                  }
               }
               recorder.addNumericDataSource("Hree price", new WriteHreePrice(), 3, 2);
            } // if scheduled to record Hree-price of stock
            if (recorderOptions.getCrudePrice()) {
               class WriteCrudePrice implements NumericDataSource {
                  public double execute() {
                     return stock.getDividend() / World.interestRate;
                  }
               }
               recorder.addNumericDataSource("risk neutral price", new WriteCrudePrice(), 3, 2);
            } // if scheduled to record Hree-price of stock
            if (recorderOptions.getTradingVolume()) {
               class WriteTradingVolume implements NumericDataSource {
                  public double execute() {
                     return stock.getTradingVolume();
                  }
               }
               recorder.addNumericDataSource("Trading volume", new WriteTradingVolume(), 3, DIGITS);
            } // if scheduled to record trading volume of each stocks
            if (recorderOptions.getMeanTradingVolume()) {
               class WriteMeanTradingVolume implements NumericDataSource {
                  public double execute() {
                     return stock.getMeanTradingVolume();
                  }
               }
               recorder.addNumericDataSource("Mean trading volume", new WriteMeanTradingVolume(), 3, DIGITS);
            } // if scheduled to record mean trading volume of each stocks

            if (recorderOptions.getFundamentalBits()) {
               final double fBitsUsed = (double) World.getFundamentalBits();
               class WriteFundamentalBits implements NumericDataSource {
                  public double execute() {
                     if (recorderOptions.getBitFractions()) {
                        return World.getWorldFBitFraction();
                     } else {
                        if (World.period < World.firstGATime) {
                           return fBitsUsed;
                        } else {
                           return (double) World.getFundamentalBits();
                        }
                     }
                  }
               }
               if (recorderOptions.getBitFractions()) {
                  recorder.addNumericDataSource("Fundamental Bits", new WriteFundamentalBits(), 10, 9);
               } else {
                  recorder.addNumericDataSource("Fundamental Bits", new WriteFundamentalBits(), 7, 1);
               }
            } // if scheduled to record the number of fundamental bits in the economy
            if (recorderOptions.getTechnicalBits()) {
               final double tBitsUsed = (double) World.getTechnicalBits();
               class WriteTechnicalBits implements NumericDataSource {
                  public double execute() {
                     if (recorderOptions.getBitFractions()) {
                        return World.getWorldTBitFraction();
                     } else {
                        if (World.period < World.firstGATime) {
                           return tBitsUsed;
                        } else {
                           return (double) World.getTechnicalBits();
                        }
                     }
                  }
               }
               if (recorderOptions.getBitFractions()) {
                  recorder.addNumericDataSource("Technical Bits", new WriteTechnicalBits(), 10, 9);
               } else {
                  recorder.addNumericDataSource("Technical Bits", new WriteTechnicalBits(), 7, 1);
               }
            } // if scheduled to record the number of technical bits in the economy
            if (recorderOptions.getBitFractions()) {
               class WriteBitFraction implements NumericDataSource {
                  public double execute() {
                     return World.getWorldBitFraction();
                  }
               }
               recorder.addNumericDataSource("Total Bit Fraction", new WriteBitFraction(), 10, 9);
            } // if scheduled to record the number of technical bits in the economy
            if (recorderOptions.getAverageWealth()) {
               class WriteAverageWealth implements NumericDataSource {
                  public double execute() {
                     return World.getAverageWealth();
                  }
               }
               recorder.addNumericDataSource("Average Wealth", new WriteAverageWealth(), 7, DIGITS);
            } // if scheduled to record the average wealth of agents in the economy
            if (recorderOptions.getPVCorr()) {
               class WritePVCorr implements NumericDataSource {
                  public double execute() {
                     return Stock.pvCorr;
                  }
               }
               recorder.addNumericDataSource("Price-Value-Corr.", new WritePVCorr(), 4, DIGITS);
            } // if scheduled to record price-value-correlation
            if (recorderOptions.getMeanFitness()) {
               class WriteMeanFitness implements NumericDataSource {
                  public double execute() {
                     return World.getMeanFitness();
                  }
               }
               recorder.addNumericDataSource("Mean Fitness", new WriteMeanFitness(), 3, DIGITS);
            } // if scheduled
            if (recorderOptions.getMinFitness()) {
               class WriteMinFitness implements NumericDataSource {
                  public double execute() {
                     return World.getMinFitness();
                  }
               }
               recorder.addNumericDataSource("Min Fitness", new WriteMinFitness(), 3, DIGITS);
            } // if scheduled
            if (recorderOptions.getMaxFitness()) {
               class WriteMaxFitness implements NumericDataSource {
                  public double execute() {
                     return World.getMaxFitness();
                  }
               }
               recorder.addNumericDataSource("Max Fitness", new WriteMaxFitness(), 3, DIGITS);
            } // if scheduled
            if (recorderOptions.getActiveRules()) {
               class WriteActiveRules implements NumericDataSource {
                  public double execute() {
                     return Agent.activatedRules;
                  }
               }
               recorder.addNumericDataSource("Activated Rules", new WriteActiveRules(), 4, DIGITS);
            } // if scheduled
            if (recorderOptions.getBitAnalyzer()) {
               class BitDataSource implements DataSource {
                  public Object execute() {
                     return world.getBitsSetString();
                  }
               }
               recorder.addObjectDataSource("F0 ;F1  ;F2 ;F3 ;F4 ;F5 ;F6 ;F7 ;F8 ;F9 ;F10;F11;F12;F13;F14;F15;F16;F17;F18;F19;F20;F21;F22;F23;F24;F25;F26;F27;F28;F29;F30;F31;T0 ;T1  ;T2 ;T3 ;T4 ;T5 ;T6 ;T7 ;T8 ;T9 ;T10;T11;T12;T13;T14;T15;T16;T17;T18;T19;T20;T21;T22;T23;T24;T25;T26;T27;T28;T29;T30;T31", new BitDataSource());
            }  // bit Analyzer
            if (recorderOptions.getAverageWealthOfClassifierAgents()) {
               class WealthOfClassifierAgents implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfClassifierAgents();
                  }
               }
               recorder.addNumericDataSource("Wealth Class.Ag.", new WealthOfClassifierAgents(), 7, 0);
            } // if scheduled
            if (recorderOptions.getAverageWealthOfNoClassifierAgents()) {
               class WealthOfNoClassifierAgents implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfNoClassifierAgents();
                  }
               }
               recorder.addNumericDataSource("Wealth NoClass.Ag.", new WealthOfNoClassifierAgents(), 7, 0);
            } // if scheduled

            if (recorderOptions.getAverageWealthOfFundamentalTraders()) {
               class WealthOfFundamentalTraders implements NumericDataSource {
                  public double execute() {
                     return World.getAverageWealthFundamentalTraders();
                  }
               }
               recorder.addNumericDataSource("Av. Wealth Fund. Traders", new WealthOfFundamentalTraders(), 7, 0);
            } // if scheduled
            if (recorderOptions.getAverageWealthOfTechnicalTraders()) {
               class WealthOfTechnicalTraders implements NumericDataSource {
                  public double execute() {
                     return World.getAverageWealthTechnicalTraders();
                  }
               }
               recorder.addNumericDataSource("Av. Wealth Techn. Traders", new WealthOfTechnicalTraders(), 7, 0);
            } // if scheduled
            if (recorderOptions.getAverageWealthOfFastLearner()) {
               class WealthOfFastLearner implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfFastLearner();
                  }
               }
               recorder.addNumericDataSource("Av. Wealth Fast Learner", new WealthOfFastLearner(), 7, 0);
            } // if scheduled
            if (recorderOptions.getAverageWealthOfNormalLearner()) {
               class WealthOfNormalLearner implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfNormalLearner();
                  }
               }
               recorder.addNumericDataSource("Av. Wealth Normal Learner", new WealthOfNormalLearner(), 7, 0);
            } // if scheduled
            if (recorderOptions.getAverageWealthOfSFIAgents()) {
               class WealthOfSFIAgents implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfSFIAgents();
                  }
               }
               recorder.addNumericDataSource("Av. Wealth SFI-Agents", new WealthOfSFIAgents(), 7, 0);
            } // if scheduled
            if (recorderOptions.getAverageWealthOfNESFIAgents()) {
               class WealthOfNESFIAgents implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfNESFIAgents();
                  }
               }
               recorder.addNumericDataSource("Av. Wealth NESFI-Agents", new WealthOfNESFIAgents(), 7, 0);
            } // if scheduled

            if (recorderOptions.getSelectAverageCounter()) {
               class SelectAverageCounter implements NumericDataSource {
                  public double execute() {
                     return Agent.selectAverageCounter;
                  }
               }
               class SFISelectAverageCounter implements NumericDataSource {
                  public double execute() {
                     return SFIAgent.SFISelectAverageCounter;
                  }
               }
               class NESFISelectAverageCounter implements NumericDataSource {
                  public double execute() {
                     return NESFIAgent.NESFISelectAverageCounter;
                  }
               }
               recorder.addNumericDataSource("SelectAverage", new SelectAverageCounter(), 7, 0);
               recorder.addNumericDataSource("SFISelectAverage", new SFISelectAverageCounter(), 7, 0);
               recorder.addNumericDataSource("NESFISelectAverage", new NESFISelectAverageCounter(), 7, 0);
            } // if scheduled

            if (recorderOptions.getZeroBitAgents()) {
               class ZeroBitAgents implements NumericDataSource {
                  public double execute() {
                     return World.getZeroBitAgents();
                  }
               }
               class ZeroFundamentalBitAgents implements NumericDataSource {
                  public double execute() {
                     return World.numberOfZeroFundamentalBitAgents;
                  }
               }
               class ZeroTechnicalBitAgents implements NumericDataSource {
                  public double execute() {
                     return World.numberOfZeroTechnicalBitAgents;
                  }
               }
               recorder.addNumericDataSource("ZBA", new ZeroBitAgents(), 4, 0);
               recorder.addNumericDataSource("ZFBA", new ZeroFundamentalBitAgents(), 4, 0);
               recorder.addNumericDataSource("ZTBA", new ZeroTechnicalBitAgents(), 4, 0);
            } // if scheduled
            if (recorderOptions.getWealthZeroBitAgents()) {
               class WealthZeroBitAgents implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfZeroBitAgents();
                  }
               }
               recorder.addNumericDataSource("Wealth ZBA", new WealthZeroBitAgents(), 7, 0);
            } // if scheduled
            if (recorderOptions.getWealthNonZeroBitAgents()) {
               class WealthNonZeroBitAgents implements NumericDataSource {
                  public double execute() {
                     return World.getWealthOfNonZeroBitAgents();
                  }
               }
               recorder.addNumericDataSource("Wealth NZBA", new WealthNonZeroBitAgents(), 7, 0);
            } // if scheduled
            if (recorderOptions.getBaseWealth()) {
               class BaseWealth implements NumericDataSource {
                  public double execute() {
                     return World.getBaseWealth();
                  }
               }
               recorder.addNumericDataSource("Base Wealth", new BaseWealth(), 7, 0);
            } // if scheduled
            if (recorderOptions.getHreeBaseWealth()) {
               class HreeBaseWealth implements NumericDataSource {
                  public double execute() {
                     return World.getLongTermHreeBaseWealth();
                  }
               }
               recorder.addNumericDataSource("Hree Base wealth", new HreeBaseWealth(), 7, 0);
            } // if scheduled

            if (recorderOptions.getForecastParameterA()) {
               class forecastParameterA implements NumericDataSource {
                  public double execute() {
                     return World.getForecastMeanA();
                  }
               }
               recorder.addNumericDataSource("ForecastMean", new forecastParameterA(), 7, 6);

               class MeanVarianceEstimate implements NumericDataSource {
                  public double execute() {
                     return World.getVarianceMean();
                  }
               }
               recorder.addNumericDataSource("VarEstimateMean", new MeanVarianceEstimate(), 6, 4);

            } // if scheduled
         } // end not LMSR
      }  // if record ??
   }  // buildModel

   private void buildDisplay() {
      if(showDisplays) {
         final Stock stock = World.Stocks;
         final LMSRStock stockLMSR = World.LMSRStocks;
         if(observer.showStocks) {
            priceGraph = new OpenSequenceGraph("Prices and MA's", this);
            priceGraph.setYRange(0 , 1.2);
            priceGraph.setYIncrement(2.5);
            if(observer.getShowHreePrice_Price()) {
               hreePrice_PriceGraph = new OpenSequenceGraph("HreePrice - Price", this);
               hreePrice_PriceGraph.setYRange(-5 , 5);
               hreePrice_PriceGraph.setYIncrement(5);
            }
            if(observer.getShowHreePrice_Price()) {
               hreePrice_PriceGraph.addSequence("HreePrice-Price", new Sequence() {
                  public double getSValue() {
                     return stock.getHreePrice()-stock.getPrice();
                  }
               });
            }  // if showHreePrice_Price
            if(observer.showPrice) {
               if (LMSR) {
                  priceGraph.addSequence("Price of Yes Stocks", new Sequence() {
                     public double getSValue() {
                        return stockLMSR.getPrice();
                     }
                  });
                  priceGraph.addSequence("Price of No Stocks", new Sequence() {
                     public double getSValue() {
                        return stockLMSR.getPriceNoStock();
                     }
                  });
               } else { // end LMSR showPrice
                  priceGraph.addSequence("Price", new Sequence() {
                     public double getSValue() {
                        return stock.getPrice();
                     }
                  });
               }
            }  // if showPrice
            if(observer.showProbability) {
               priceGraph.addSequence("Probability", new Sequence() {
                  public double getSValue() {
                     return stockLMSR.getProbability();
                  }
               });
            }  // showPriceMean
            if(observer.showPriceMean) {
               priceGraph.addSequence("Mean of Stockprice", new Sequence() {
                  public double getSValue() {
                       return stock.getPriceMean();
                  }
               });
            }  // showPriceMean
            if(observer.showPriceMA5) {
               priceGraph.addSequence("MA5 of Stock-Price", new Sequence() {
                  public double getSValue() {
                       return stock.getPriceMA(0);
                  }
               });
            }  // showPriceMA5
            if(observer.showPriceMA25) {
               priceGraph.addSequence("MA25 of Stock-Price", new Sequence() {
                  public double getSValue() {
                       return stock.getPriceMA(1);
                  }
               });
            }  // showPriceMA25
            if(observer.showPriceMA50) {
               priceGraph.addSequence("MA50 of Stock-Price", new Sequence() {
                  public double getSValue() {
                       return stock.getPriceMA(2);
                  }
               });
            }  // showPriceMA50
            if(observer.showPriceMA100) {
               priceGraph.addSequence("MA100 of Stock-Price", new Sequence() {
                  public double getSValue() {
                       return stock.getPriceMA(3);
                  }
               });
            }  // showPriceMA100
            if(observer.showDividend) {
               priceGraph.addSequence("Dividend", new Sequence() {
                  public double getSValue() {
                       return stock.getDividend();
                  }
               });
            }  // showDividend
            if(observer.showDivMean) {
               priceGraph.addSequence("Dividend-Mean/r", new Sequence() {
                  public double getSValue() {
                       return stock.getDivMean()/World.interestRate;
                  }
               });
            }  // showDividend
            if(observer.showHreePrice) {
               priceGraph.addSequence("Risk adjusted hree-Price", new Sequence() {
                  public double getSValue() {
                       return stock.hreePrice;
                  }
               });
            }  // showHreePrice
            if(observer.showHreeMean) {
               priceGraph.addSequence("Hree-Price Mean", new Sequence() {
                  public double getSValue() {
                       return stock.hreePriceMean;
                  }
               });
            }  // showHreePriceMean
            if(observer.showCrudePrice) {
               priceGraph.addSequence("Crude Risk neutral Price=Div/r", new Sequence() {
                  public double getSValue() {
                       return stock.oldDividend/World.interestRate;
                  }
               });
            }  // showHreePrice
         }  // if(observer.showStocks)
         if(observer.showLogReturns) {
            logReturnGraph = new OpenSequenceGraph("Log-Return", this);
            logReturnGraph.setYRange(-0.1, .10);
            logReturnGraph.setYIncrement(0.01);
            logReturnGraph.addSequence("Log Price", new Sequence() {
               public double getSValue() {
                  return stock.logReturn;
               }
            });
            logReturnGraph.addSequence("Log-Value", new Sequence() {
               public double getSValue() {
                  return (stock.logHreePrice +0.075);
               }
            });
         }  // if showLogReturns
         if(observer.showVolume) {
            volumeGraph = new OpenSequenceGraph("Volume over time", this);
            this.registerMediaProducer("Volume over Time ", volumeGraph );
            volumeGraph.setYRange(0 , 45);
            volumeGraph.setYIncrement(1);
            if (LMSR) {
               volumeGraph.addSequence("Volume of Yes Stocks", new Sequence() {
                  public double getSValue() {
                     return stockLMSR.getTradingVolumeYes();
                  }
               });
               volumeGraph.addSequence("Volume of No Stocks", new Sequence() {
                  public double getSValue() {
                     return stockLMSR.getTradingVolumeNo();
                  }
               });
            } else {
               volumeGraph.addSequence("Volume", new Sequence() {
                  public double getSValue() {
                     return stock.getTradingVolume();
                  }
               });
            }
         }  // showVolume
         if(observer.getShowMarketMakerRevenue() || observer.getShowMarketMakerLiabilities() || observer.getShowMarketMakerProfit()) {
            MMGraph = new OpenSequenceGraph("Market Maker Cash Flow", this);
            this.registerMediaProducer("Market Maker Cash Flow ", MMGraph );
            MMGraph.setYRange(0 , 20);
            MMGraph.setYIncrement(1);
            if (observer.getShowMarketMakerRevenue()) {
               MMGraph.addSequence("Market Maker Revenue", new Sequence() {
                  public double getSValue() {
                     return specialist.getSpecialistRevenue();
                  }
               });
            }
            if (observer.getShowMarketMakerLiabilities()) {
               MMGraph.addSequence("Market Maker Liabilities", new Sequence() {
                  public double getSValue() {
                     return specialist.getSpecialistLiabilities();
                  }
               });
            }
            if (observer.getShowMarketMakerProfit()) {
               MMGraph.addSequence("Market Maker Profit", new Sequence() {
                  public double getSValue() {
                     return specialist.getSpecialistProfit();
                  }
               });
            }
         }  // showMarketMaker
         if(observer.getShowBLiq()) {
            LMSRGraph = new OpenSequenceGraph("LMSR Settings", this);
            this.registerMediaProducer("LMSR Settings ", LMSRGraph );
            LMSRGraph.setYRange(0 , 10);
            LMSRGraph.setYIncrement(1);
            LMSRGraph.addSequence("bLiq", new Sequence() {
               public double getSValue() {
                  return stockLMSR.getBLiq();
               }
            });
         }  // showLMSRSettings
         if(observer.getShowWealthClassifierAgents() || observer.getShowWealthFundamentalTraders() || observer.getShowWealthNoClassifierAgents() || observer.getShowWealthTechnicalTraders() || observer.getShowWealthNormalLearner() || observer.getShowWealthFastLearner() || observer.getShowWealthSFIAgents() || observer.getShowWealthNESFIAgents() || observer.getShowWealthNonZeroBitAgents() || observer.getShowWealthZeroBitAgents() || observer.getShowBaseWealth() || observer.getShowLongTermHreeBaseWealth()) {
            wealthGraph = new OpenSequenceGraph("Average Wealth", this);
            wealthGraph.setYRange(20000 , 25000);
            wealthGraph.setYIncrement(500);
            if(observer.getShowWealthClassifierAgents() && World.getFracClassifierAgents() > 0d) {
               wealthGraph.addSequence("Classifier Agents", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfClassifierAgents();
                  }
               });
            }
            if(observer.getShowWealthNoClassifierAgents() && World.getFracClassifierAgents() < 1d) {
               wealthGraph.addSequence("No-Classifier Agents", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfNoClassifierAgents();
                  }
               });
            }
            if(observer.getShowWealthFundamentalTraders() && World.getNumberOfTechnicians() > 0) {
               wealthGraph.addSequence("Fundamental Traders", new Sequence() {
                  public double getSValue() {
                     return World.getAverageWealthFundamentalTraders();
                  }
               });
            }
            if(observer.getShowWealthTechnicalTraders() && World.getNumberOfTechnicians() > 0) {
               wealthGraph.addSequence("Technical Traders", new Sequence() {
                  public double getSValue() {
                     return World.getAverageWealthTechnicalTraders();
                  }
               });
            }
            if(observer.getShowWealthFastLearner() && World.getFracFastLearner() > 0d) {
               wealthGraph.addSequence("Fast Learner", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfFastLearner();
                  }
               });
            }
            if(observer.getShowWealthNormalLearner() && World.getFracFastLearner() > 0d) {
               wealthGraph.addSequence("Normal Learner", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfNormalLearner();
                  }
               });
            }
            if(observer.getShowWealthSFIAgents() && World.numberOfSFIAgents > 0 && World.numberOfNESFIAgents > 0) {
               wealthGraph.addSequence("SFI-Agents", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfSFIAgents();
                  }
               });
            }
            if(observer.getShowWealthNESFIAgents() && World.numberOfNESFIAgents > 0 && World.numberOfSFIAgents > 0) {
               wealthGraph.addSequence("NESFI-Agents", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfNESFIAgents();
                  }
               });
            }
            if(observer.getShowWealthZeroBitAgents() && World.getFracClassifierAgents() > 0d) {
               wealthGraph.addSequence("Zero-Bit Agents", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfZeroBitAgents();
                  }
               });
            }
            if(observer.getShowWealthNonZeroBitAgents() && World.getFracClassifierAgents() > 0d) {
               wealthGraph.addSequence("Non Zero-Bit Agents", new Sequence() {
                  public double getSValue() {
                     return World.getWealthOfNonZeroBitAgents();
                  }
               });
            }
            if(observer.getShowBaseWealth() ) {
               wealthGraph.addSequence("Base Wealth", new Sequence() {
                  public double getSValue() {
                     return World.getBaseWealth();
                  }
               });
            }
            if(observer.getShowLongTermHreeBaseWealth() ) {
               wealthGraph.addSequence("HREE Base Wealth", new Sequence() {
                  public double getSValue() {
                     return World.getLongTermHreeBaseWealth();
                  }
               });
            }



         }  // showWealth

         if(observer.showAgentGrid) {
            dsurf = new DisplaySurface(this, "Probable Agent Display");
            registerDisplaySurface("Main", dsurf);
            agentWorld = new Object2DTorus(World.numberOfAgents/3, World.numberOfAgents/3 );
            for (int i=0 ; i< World.numberOfAgents; i++) {
               int x, y;
               do {
                  x = Uniform.staticNextIntFromTo(0, World.numberOfAgents/3);
                  y = Uniform.staticNextIntFromTo(0, World.numberOfAgents/3);
               } while (agentWorld.getObjectAt(x, y) != null);
               Agent agent = World.Agents[i];
               agent.setX(x);
               agent.setY(y);
               agentWorld.putObjectAt(x, y, agent);
            }
            Object2DDisplay agentDisplay = new Object2DDisplay(agentWorld);
            // agentDisplay.setObjectList(World.Agents);
            dsurf.addDisplayableProbeable(agentDisplay, "Agents");
            addSimEventListener(dsurf);
         }  // showAgentGrid in order to probe agent's wealth, stock holdings etc.
         if(observer.getShowPriceValueCorr()) {
            correlationGraph = new OpenSequenceGraph("Correlation-Chart", this);
            correlationGraph.setYRange(-0.1 , 1);
            correlationGraph.setYIncrement(0.1);
            if(observer.getShowPriceValueCorr()) {
               correlationGraph.addSequence("Price-Value Correlation", new Sequence() {
                  public double getSValue() {
                     return stock.pvCorr;
                  }
               });
            }  // if price-value correlation
         }  // of any correlation to be shown
         if(observer.getShowTechnicalBits() || observer.getShowFundamentalBits() || observer.getShowBitFractions() ) {
            if(observer.getShowBitFractions()) {
               final double fBitsUsed = World.getWorldFBitFraction();
               final double tBitsUsed = World.getWorldTBitFraction();
               final double BitsUsed = (fBitsUsed+tBitsUsed)/2;
               bitUsageGraph = new OpenSequenceGraph("Bit-Usage in Economy", this);
               this.registerMediaProducer("Bit-Usage in Economy", bitUsageGraph );
               bitUsageGraph.setYRange(0.975*TradingRule.bitProb,1.025*TradingRule.bitProb);
               bitUsageGraph.setYIncrement(TradingRule.bitProb);
               bitUsageGraph.addSequence("Total Fraction of bits used ", new Sequence() {
                  public double getSValue() {
                     if(World.period<World.firstGATime) {
                        return BitsUsed;
                     } else {
                        return World.getWorldBitFraction();
                     }
                  }
               });
               if(observer.getShowTechnicalBits()) {
                  bitUsageGraph.addSequence("Fraction of technical bits used ", new Sequence() {
                     public double getSValue() {
                        if(World.period<World.firstGATime) {
                           return tBitsUsed;
                        } else {
                           return World.getWorldTBitFraction();
                        }
                     }
                  });
               }
               if(observer.getShowFundamentalBits()) {
                  bitUsageGraph.addSequence("Fraction fundamental bits used ", new Sequence() {
                     public double getSValue() {
                        if(World.period<World.firstGATime) {
                           return fBitsUsed;
                        } else {
                           return World.getWorldFBitFraction();
                        }
                     }
                  });
               }
            } else { // fractions or absolute numbers
               final int fBitsUsed = World.getFundamentalBits();
               final int tBitsUsed = World.getTechnicalBits();
               bitUsageGraph = new OpenSequenceGraph("Total Bit-Usage in Economy", this);
               this.registerMediaProducer("Total Bit-Usage in Economy", bitUsageGraph );
               bitUsageGraph.setYRange(Math.min(fBitsUsed,tBitsUsed)-25 ,Math.max(fBitsUsed,tBitsUsed)+25 );
               bitUsageGraph.setYIncrement(500);
               if(observer.getShowTechnicalBits()) {
                  bitUsageGraph.addSequence("Number of technical bits used ", new Sequence() {
                     public double getSValue() {
                        if(World.period<World.firstGATime) {
                           return tBitsUsed;
                        } else {
                           return World.getTechnicalBits();
                        }
                     }
                  });
               }
               if(observer.getShowFundamentalBits()) {
                  bitUsageGraph.addSequence("Number of fundamental bits used ", new Sequence() {
                     public double getSValue() {
                        if(World.period<World.firstGATime) {
                           return fBitsUsed;
                        } else {
                           return World.getFundamentalBits();
                        }
                     }
                  });
               }
            }  // show Bit fractions or absolute number of bits
         }  // if any bits are to be shown
      }  // if showDisplays
   }   // buildDisplay()

      private void buildSchedule() {
//         try {
            class PerformPeriod extends BasicAction {
               public void execute() {
                  ExecutePeriod.execute();
               }
            } // PerformPeriod
            // schedule.scheduleActionAtInterval(100, new SnapshotRunner(), Schedule.LAST);
            ActionGroup group = new ActionGroup(ActionGroup.SEQUENTIAL);
            group.addAction(new PerformPeriod());
            schedule.scheduleActionBeginning(0, group);
            schedule.scheduleActionAt(world.numberOfPeriods, this, "stop");
            // schedule.scheduleActionAt(world.reInitialize, this, "reInitialize");
            if (recordData) {
                schedule.scheduleActionAtInterval(recorderOptions.getWriteFrequency(), recorder, "writeToFile");
                schedule.scheduleActionAtPause(recorder, "writeToFile");
                schedule.scheduleActionAtEnd(recorder, "writeToFile");
            }
            if (stopAtZeroBit) {
               schedule.scheduleActionAtInterval(1, this, "conditionalStop");
            }
//               if(World.gaInterval  > 250) {
//                  schedule.scheduleActionAtInterval(50, this, "conditionalStop");
//               } else {
//                  if(World.gaInterval >25) {
//                     schedule.scheduleActionAtInterval(25, this, "conditionalStop");
//                  } else {
//                     if(World.gaInterval > 10) {
//                        schedule.scheduleActionAtInterval(5, this, "conditionalStop");
//                     } else {
//                        schedule.scheduleActionAtInterval(1, this, "conditionalStop");
//                     }
//                  }
//               }
//            }
           schedule.scheduleActionAtInterval(2500000, this , "checkVariables");
//         } catch (NoSuchMethodException ex) {
//            SimUtilities.showError("Unable to build schedule - can't find method", ex);
//            ex.printStackTrace();
//            System.exit(0);
//         }
      } 	// buildSchedule

      /**
       * It is possible that bit usage has vanished in the warm up period. Thus, reinitialize
       * the bits over the whole population when prices seem to have reached their equilibrium
       * level. Then see, whether bit usage vanishes again.
       */
      public void reInitialize() {
         System.out.println(NESFIAgent.numRulesNESFIAgent+" "+NESFIAgent.newTR);
         double averageWealth = World.getLongTermHreeBaseWealth() ;
         World.setBaseWealth(averageWealth);
         for (int i = 0; i < World.numberOfAgents ; i++) {
            World.Agents[i].wealth = averageWealth;
            World.Agents[i].numberOfStocks=1d;
            World.Agents[i].cash = averageWealth - stock.price;
         }

         for (int i = 0; i < World.numberOfAgents ; i++) {
            if(World.Agents[i].useClassifier) {
               if(World.Agents[i].nesfiAgent) {
                  World.Agents[i].zeroBitAgent = false;
                  World.Agents[i].zeroFundamentalBitAgent = false;
                  World.Agents[i].zeroTechnicalBitAgent = false;
               }
               for (int j = 0; j<World.Agents[i].numRules ; j++ ) {
                  for (int k = 0; k < (!World.Agents[i].ruleSet[j].technicalRule? 1 : 2 ); k++) {
                     for (int l = 0; l < TradingRule.nBits; l++) {
                        if (Random.uniform.nextDoubleFromTo(0d,1d) < TradingRule.bitProb) {
                           /* Store "value" for bit n with aState[WORD(n)] |= value << SHIFT[n];
                              if the stored value was previously 0
                           */
                           if (Random.uniform.nextDoubleFromTo(0d,1d) < 0.5) {
                              World.Agents[i].ruleSet[j].conditionWords[k] |= 1l << Asset.SHIFT[l];
                           } else {
                              World.Agents[i].ruleSet[j].conditionWords[k] |= 2l << Asset.SHIFT[l];
                           }
                           // now update bit world bit statistics, i.e. how often a particular bit is set in the economy
                           //World.BitsSet[i][j]++;
                        }  // bit set with probability bitProb
                     }  // for all nBits
                     if (World.Agents[i].useClassifier && World.Agents[i].checkRules) {
                        World.Agents[i].ruleSet[j].specificity = World.Agents[i].ruleSet[j].check_consistency();
                     } else {
                        if(World.Agents[i].useClassifier) {
                           World.Agents[i].ruleSet[j].specificity = World.Agents[i].ruleSet[j].getSpecificity();
                        }
                     }
                     World.Agents[i].ruleSet[j].forecastVar = World.Agents[i].ruleSet[j].initVar ;
                     World.Agents[i].ruleSet[j].fitness = World.Agents[i].ruleSet[j].detFitness();
             for (int l = 25 ; l <32 ; l++ ) {  // set unused bits in the technical word to zero
                        World.Agents[i].ruleSet[l].conditionWords[1] = (World.Agents[i].ruleSet[l].conditionWords[1] & Asset.NMASK[l]) | (0l << Asset.SHIFT[l] );
                     }
                  }
               }
            }
         }
         System.out.println("Re-Initialized Trading Bits at period "+World.period);
         World.numberOfZeroBitAgents = 0;
         World.numberOfZeroFundamentalBitAgents = 0;
         World.numberOfZeroTechnicalBitAgents = 0;
         SFIAgent.SFISelectAverageCounter = 0;
         Agent.selectAverageCounter = 0;
         NESFIAgent.NESFISelectAverageCounter = 0;
//         if (recordData && recorderOptions.getNewZeroBitAgentAt())
//            recorder.record();
      }

      public void conditionalStop() {
//         if ( World.getWorldTBitFraction()== 0.0 && !tBitsZero) {
//            tBitsZero = true;
//            if (recordData) {
//               recorder.record();
//            }
//         }
//         if ( World.getWorldBitFraction()==0.0  && !fBitsZero) {
//            fBitsZero = true;
//            if (recordData) {
//               recorder.record();
//            }
//         }
//         if (fBitsZero && tBitsZero) {
//            tBitsZero = false;
//            fBitsZero = false;
            // schedule.scheduleActionAt(world.period, this, "stop");
            if(stopNow) stop();
//         }
      }  // conditionalStop()


   public void begin() {

      buildModel();
      if(showDisplays) {
          buildDisplay();
/*        // I had this in the RePast 1.4 version which caused a run time error with RePast 2.0
          // If I remember correctly this was just for a correct display of the first period and is not
          // necessarily needed.
          if (observer.showPrice && observer.showStocks ) {
            priceGraph.step();
          }
*/
      }
      buildSchedule();
      if (showDisplays) {
         if (observer.showPrice ) {
            priceGraph.display();
         }
         if(observer.getShowHreePrice_Price()) {
            hreePrice_PriceGraph.display();
         }
         if(observer.showLogReturns) {
            logReturnGraph.display();
         }
         if(observer.getShowVolume()) {
            volumeGraph.display();
         }
         if(observer.showAgentGrid) {
            dsurf.display();
         }
         if(observer.getShowPriceValueCorr()) {
            correlationGraph.display();
         }
         if(observer.getShowTechnicalBits() || observer.getShowFundamentalBits() || observer.getShowBitFractions() ) {
            bitUsageGraph.display();
         }
         if(observer.getShowMarketMakerRevenue() || observer.getShowMarketMakerLiabilities() || observer.getShowMarketMakerProfit() ) {
            MMGraph.display();
         }
         if(observer.getShowBLiq()) {
            LMSRGraph.display();
         }
         if(observer.getShowWealthClassifierAgents() || observer.getShowWealthFundamentalTraders() || observer.getShowWealthNoClassifierAgents() || observer.getShowWealthTechnicalTraders() || observer.getShowWealthNormalLearner() || observer.getShowWealthFastLearner() || observer.getShowWealthSFIAgents() || observer.getShowWealthNESFIAgents() || observer.getShowWealthNonZeroBitAgents() || observer.getShowWealthZeroBitAgents() || observer.getShowBaseWealth() || observer.getShowLongTermHreeBaseWealth() ) {
            wealthGraph.display();
         }
         if(bitDistributionPlot != null) bitDistributionPlot.dispose();
      }
   }  // begin


   public static void main(String[] args) {
      SimInit init = new SimInit();
      AsmModel model = new AsmModel();
      if (args.length > 0) {  // if there is a parameter file given, then the run is in batch-mode
         init.loadModel(model, args[0], true);
      } else {
         init.loadModel(model, null, false);
      }
   }  // public static void main( )

   public String getName() {
          return "LMSR-NESFI-ASM";
   }
   public Schedule getSchedule() {
          return schedule;
   }
   public void stop() {
      if(this.getController().isBatch()) {
         if(recorderOptions.getWriteRuleUsage()) {
            writeRuleUsage();
         }
         if(recorderOptions.getWriteAverageStockHoldings()) {
            writeAverageStockHoldings();
         }
      }
      this.fireStopSim();
      if(showDisplays  && observer.showBitDistribution) {
         String title = "Combined Bit Distribution";
         if(bitDistributionPlot != null) bitDistributionPlot.dispose();
         bitDistributionPlot = new BitDistributionPlot(title);
         bitDistributionPlot.pack();
         bitDistributionPlot.setSize(1024,730);
         bitDistributionPlot.setVisible(true);
      }
      if (recordData && AsmModel.recorderOptions.getNewZeroBitAgentAt() && World.period==World.numberOfPeriods)
         recorder.record();

   }

   public void setup() {
      if (World.period > 0) {	// setup called after a simulation run
         tBitsZero = false;
         stopNow = false;
         TradingRule.generalizationCounter=0;
         TradingRule.setRuleCounter(0);
         if(showDisplays) {
            if(observer.showPrice) {
               priceGraph.dispose();
               if(observer.getShowHreePrice_Price()) {
                  hreePrice_PriceGraph.dispose();
               }
            }
            if(observer.showAgentGrid) {
               dsurf.dispose();
            }
            if(observer.getShowPriceValueCorr()) {
               correlationGraph.dispose();
            }
            if(observer.getShowFundamentalBits() || observer.getShowTechnicalBits()) {
               bitUsageGraph.dispose();
            }
            if(observer.getShowLogReturns() ) {
               logReturnGraph.dispose();
            }
            if(observer.getShowVolume()) {
               volumeGraph.dispose();
            }
         }  // showDisplays
         for (int i = 0 ; i < world.numberOfAgents ; i++) {
            World.Agents[i] = null;
         }
         specialist = null;
         normalNormal = null;
         stockNormal = null;
         Agent.instancesOfTechnicians = 0;
         World.numberOfZeroBitAgents = 0;
         World.numberOfZeroFundamentalBitAgents = 0;
         World.numberOfZeroTechnicalBitAgents = 0;
         World.period=0;
         NESFIAgent.NESFISelectAverageCounter = 0;
         SFIAgent.SFISelectAverageCounter = 0;
      }  // reRun
      schedule = null;
      schedule = new Schedule();
      System.gc();
      if(showDisplays) {
         if(observer.showAgentGrid) {
            dsurf = new DisplaySurface(this, "Probable Agent Display");
            registerDisplaySurface("Main", dsurf);
         }
      }
   }  // setup

   /* Now the public get- and set-methods for the GUI are included. Please refer to the
      RePast manual if you don't understand how they are constructed.
   */
   public int getNumberOfSFIAgents() {
      return world.numberOfSFIAgents;
   }
   public void setNumberOfSFIAgents(int val) {
      world.numberOfSFIAgents = val;
      world.numberOfAgents = world.numberOfSFIAgents + world.numberOfNESFIAgents + world.numberOfLMSRAgents;
   }
   public int getNumberOfNESFIAgents() {
      return world.numberOfNESFIAgents;
   }
   public void setNumberOfNESFIAgents(int val) {
      world.numberOfNESFIAgents = val;
      world.numberOfAgents = world.numberOfSFIAgents + world.numberOfNESFIAgents + world.numberOfLMSRAgents;
   }
   public int getNumberOfLMSRAgents() { //mudar
      return world.numberOfLMSRAgents;
   }
   public void setNumberOfLMSRAgents(int val) { //mudar
      world.numberOfLMSRAgents = val;
      world.numberOfAgents = world.numberOfSFIAgents + world.numberOfNESFIAgents + world.numberOfLMSRAgents;
   }
   public boolean getShowDisplays() { return showDisplays; }
   public void setShowDisplays(boolean showDisplays) {
      this.showDisplays = showDisplays;
   }
   public boolean getRecordData() { return recordData; }
   public void setRecordData(boolean recordData) {
      this.recordData = recordData;
   }
   public boolean getHree() { return hree; }
   public void setHree(boolean hree) {
      this.hree = hree;
      if (hree) Specialist.type = Specialist.RESPECIALIST;
   }
   public boolean getLMSR() { return LMSR; }
   public void setLMSR(boolean LMSR) {
      this.LMSR = LMSR;
      if (LMSR) { Specialist.type = Specialist.LMSRSPECIALIST; }
   }

   public int getNumberOfPeriods() { return world.numberOfPeriods; }
   public void setNumberOfPeriods(int numberOfPeriods) { world.numberOfPeriods = numberOfPeriods;   }
   public double getInterestRate() { return World.interestRate; }
   public void setInterestRate(double interestRate) { World.interestRate = interestRate; }
   public int getFirstGATime() { return World.firstGATime; }
   public void setFirstGATime(int val ) { World.firstGATime = val; }
   public void setSelectionMethod(int selectionMethod) { Agent.selectionMethod = selectionMethod; }
   public int getSelectionMethod() { return Agent.selectionMethod ; }
   public void setMemory(int memory) { Asset.setMemory(memory); }
   public int getMemory() { return Asset.getMemory() ; }
   public void setGaInterval(int interval) {
       World.gaInterval = interval ;
       World.gaProb = 1.0 / interval ;
   }
   public void setNumberOfRules(int rules) {
      NESFIAgent.numRulesNESFIAgent = rules;
      NESFIAgent.newTR = rules / 5;
      SFIAgent.numRulesSFIAgent = rules;
      SFIAgent.newTR = rules / 5;
   }
   public int getNumberOfRules() {return NESFIAgent.numRulesNESFIAgent; }
   public void setReInitializeAt(int period) {World.reInitialize=period ; }
   public int getReInitializeAt() {return World.reInitialize;}
   public int getGaInterval() { return World.gaInterval ; }
   public int getNumberOfTechnicians() { return World.getNumberOfTechnicians() ; }
   public void setNumberOfTechnicians(int val ) {
      World.setNumberOfTechnicians(val);
   }
   public double getFracClassifierAgents() { return World.getFracClassifierAgents(); }
   public void setFracClassifierAgents(double val) {
      World.setFracClassifierAgents(val);
   }
   public double getFracFastAgents() { return World.getFracFastLearner() ; }
   public void setFracFastAgents(double val) { World.setFracFastLearner(val); }

   public int getGaIntervalFastAgents() { return World.gaIntervalFastLearner ; }
   public void setGaIntervalFastAgents(int val) {World.gaIntervalFastLearner = val; }
   public boolean getStopAtZeroBit() { return stopAtZeroBit; }
   public void setStopAtZeroBit(boolean val) { this.stopAtZeroBit = val; }


   public double getInitBitProb() { return TradingRule.bitProb ; }
   public void setInitBitProb(double val ) {
      TradingRule.bitProb=val;
   }


   /* The following functions put complete objects into the GUI. Even though I wanted to
      set only static parameters for these objects, I have to create an actual instance
      that is called from the get-methods. Those instances are created in the constructor
      of AsmModel. The agent-objects are 'virtual' in the sense that their constructor
      realizes through a parameter that the agent counter shouldn't be increased.
   */
   public void setObserver(ObserverOptions val) {}
   public ObserverOptions getObserver() { return observer ; }
   public void setRecorderOptions(RecorderOptions val) {}
   public RecorderOptions getRecorderOptions() { return recorderOptions ; }
   public void setTradingRule(TradingRule val) {}
   public TradingRule getTradingRule() { return tradingRule; }
   public void setSFIAgent(SFIAgent val) {}
   public SFIAgent getSFIAgent() { return staticSFIAgent ; }
   public void setNESFIAgent(NESFIAgent val) { }
   public NESFIAgent getNESFIAgent() { return staticNESFIAgent ; }
   public void setLMSRAgent(LMSRAgent val) { } //mudar para dentro de lmsragent
   public LMSRAgent getLMSRAgent() { return staticLMSRAgent ; } //mudar para dentro de lmsragent
//   public void setAgent(Agent val) {}
//   public Agent getAgent() { return staticAgent ; }
   public Stock getStock() { return stock; }
   public void setStock(Stock val) { this.stock = val; }
   public LMSRStock getStockLMSR() { return stockLMSR; }
   public void setStockLMSR(LMSRStock val) { this.stockLMSR = val; }




   /* The next parameters (crossoverProb. riskAversion, bitCost, and maxNonActive) don't show up in the GUI,
      yet the get- and set procedure are included here to be able to set them from the
      parameter file. It would be nice to extend the model such that there are individual
      parameter files for the object (Trading Rule, Agent, SFI-Agent, NESFI-Agent, FastAgent,
      Specialist etc) parameter settings. Right now, these parameters are handled from the
      single parameter file, and the respective get- and set-methods have to be public in the
      top level AsmModel-Class.
   */
   public void setAlphaLS(double val) { LMSRStock.alphaLS = val; }
   public double getAlphaLS() { return LMSRStock.alphaLS ; }
   public void setBLiq(double val) { LMSRStock.bLiq = val; }
   public double getBLiq() { return LMSRStock.bLiq ; }
   public void setInitialPrice(double val) { LMSRStock.initialPrice = val;}
   public double getInitialPrice() {return LMSRStock.initialPrice ;}
   public double getInitialQuantity() { return LMSRStock.initialQuantity; }
   public void setInitialQuantity(double val) { LMSRStock.initialQuantity = val; }
   public void setLiquiditySensitive(boolean val) { LMSRStock.liquiditySensitive = val; }
   public boolean getLiquiditySensitive() { return LMSRStock.liquiditySensitive ; }
   public void setPeriodShock(double val) { LMSRStock.periodShock = val; }
   public double getPeriodShock() { return LMSRStock.periodShock ; }
   public void setProbAfterShock(double val) { LMSRStock.probAfterShock = val;}
   public double getProbAfterShock() {return LMSRStock.probAfterShock ;}
   public double getProbability() { return LMSRStock.probability; }
   public void setProbability(double val) { LMSRStock.probability = val; }
   public void setCrossoverProbability(double val) { Agent.probCrossover = val; }
   public double getCrossoverProbability() { return Agent.probCrossover ; }
   public void setRiskAversion(double val) { staticSFIAgent.riskAversion = val; }   // need it for Batch-runs
   public double getRiskAversion() { return staticSFIAgent.riskAversion ; }
   public void setBitCost(double val) { TradingRule.bitCost = val;}
   public double getBitCost() {return TradingRule.bitCost ;}
   public int getMaxNonActive() { return TradingRule.maxNonActive; }
   public void setMaxNonActive(int val) { TradingRule.maxNonActive = val; }
   public void setRecorderParamFile(String val) {recorderParamFile = val; }
   public String getRecorderParamFile() { return recorderParamFile; }
   public void setRecorderOutputFile(String val) {recorderOptions.setRecorderOutputFile(val); }
   public String getRecorderOutputFile() { return recorderOptions.getRecorderOutputFile(); }

   public String[] getInitParam() {
      if(this.getController().isBatch()) {
         String[] params = {"numberOfSFIAgents","numberOfNESFIAgents","numberOfLMSRAgents",
         "stopAtZeroBit","numberOfPeriods","recordData","interestRate","hree","memory",
         "numberOfTechnicians","fracFastAgents","gaInterval","showDisplays",
         "firstGATime","gaIntervalFastAgents","bitCost","crossoverProbability",
         "riskAversion","maxNonActive","recorderParamFile","recorderOutputFile","fracClassifierAgents",
         "selectionMethod","reInitializeAt","numberOfRules","initBitProb",
         "LMSR","alphaLS","bLiq","initialPrice","initialQuantity","liquiditySensitive",
         "periodShock","probAfterShock","probability"
         };
         return params;
      } else {
         Controller.ALPHA_ORDER= false;   // show the variable not in alphabetical order but in the order as they are in the string array.
         String[] params = {"SFIAgent","numberOfSFIAgents","NESFIAgent","numberOfNESFIAgents","LMSRAgent","numberOfLMSRAgents","selectionMethod","fracClassifierAgents","numberOfTechnicians","fracFastAgents","gaIntervalFastAgents","gaInterval","firstGATime","numberOfPeriods","stopAtZeroBit","interestRate","memory","hree","LMSR","stock","stockLMSR","tradingRule","showDisplays","observer","recordData","recorderOptions","reInitializeAt"};
         return params;
      }
   }  // getInitParam()


   /**
    * This procedure just updates the console such that the model doesn't look like stuck if
    * it is in batch-mode. It is scheduled in buildSchedule and can easily be deleted.
   */
   public void checkVariables() {
      double wealth =0;
      double money =0;
      System.out.println("GA-Int.: "+World.gaInterval+"Period: "+World.period+": "+World.getZeroBitAgents()+" Zero-Bit Agents");
      System.out.println("BitAnalyzer: "+AsmModel.world.getBitsSetString());
      System.out.println(World.Agents[0].checkRules);
      if(showDisplays && observer.showBitDistribution) {
         if(bitDistributionPlot != null) bitDistributionPlot.dispose();
         String title = "Combined Bit Distribution";
         bitDistributionPlot = new BitDistributionPlot(title);
         bitDistributionPlot.pack();
         bitDistributionPlot.setSize(1024,730);
         bitDistributionPlot.setVisible(true);
      }
         for (int i = 0  ; i < World.numberOfAgents ; i++) {
            System.out.println("Agent "+World.Agents[i].ID+": "+World.Agents[i].getAgentBitsSetString());
         }
   }  // checking routine

   private void writeRuleUsage() {
      double cumFitness = 0d;
      BatchController control = (BatchController)this.getController();
      try  {
         FileWriter ausgabestrom = new FileWriter("I:/SimulationRuns/FinalDissertation/BitsOnSpeed/25SFIRuleFitnessesMore"+control.getBatchCount());
         PrintWriter ausgabe = new PrintWriter(ausgabestrom);
         ausgabe.println("AgentID;RuleID;Age;numberOfActivations;NumberOfUses;Specificity;absFitness;relFitness");
         for (int i = 0  ; i < World.numberOfAgents ; i++) {
            for (int j = 0 ; j < World.Agents[i].numRules ; j++) {
               cumFitness += World.Agents[i].ruleSet[j].fitness;
            }
            for (int j = 0 ; j < World.Agents[i].numRules ; j++) {
               ausgabe.println(World.Agents[i].ID+";"+World.Agents[i].ruleSet[j].getRuleID()+";"+(World.period-World.Agents[i].ruleSet[j].birth)+";"+World.Agents[i].ruleSet[j].activeCounter+";"+World.Agents[i].ruleSet[j].usedCounter+";"+World.Agents[i].ruleSet[j].getSpecificity()+";"+World.Agents[i].ruleSet[j].fitness+";"+Math.rint(World.Agents[i].ruleSet[j].fitness/cumFitness/0.00001)*0.001);
            }
            cumFitness = 0d;
         }
         ausgabe.close();
      } catch (IOException e) {
            System.err.println(e.toString());
      }
   }  // writeRuleUsage

   private void writeAverageStockHoldings() {
      BatchController control = (BatchController)this.getController();
      try  {
         FileWriter ausgabestrom = new FileWriter("I:/SimulationRuns/FinalDissertation/BitsOnSpeed/25SFIBitUsageMore"+control.getBatchCount());
         PrintWriter ausgabe = new PrintWriter(ausgabestrom);
         ausgabe.println("AgentID;ruleID;tBits;fBits;F0 ;F1  ;F2 ;F3 ;F4 ;F5 ;F6 ;F7 ;F8 ;F9 ;F10;F11;F12;F13;F14;F15;F16;F17;F18;F19;F20;F21;F22;F23;F24;F25;F26;F27;F28;F29;F30;F31;T0 ;T1  ;T2 ;T3 ;T4 ;T5 ;T6 ;T7 ;T8 ;T9 ;T10;T11;T12;T13;T14;T15;T16;T17;T18;T19;T20;T21;T22;T23;T24;T25;T26;T27;T28;T29;T30;T31");
         for (int i = 0  ; i < World.numberOfAgents ; i++) {
            ausgabe.println(World.Agents[i].ID+";-;"+World.Agents[i].getTechnicalBits()+";"+World.Agents[i].getFundamentalBits()+";"+World.Agents[i].getAgentBitsSetString() );
            for(int j = 0; j < World.Agents[i].numRules; j++  ) {
               ausgabe.println(World.Agents[i].ID+";"+World.Agents[i].ruleSet[j].getRuleID()+";"+World.Agents[i].ruleSet[j].tBits+";"+World.Agents[i].ruleSet[j].fBits+";"+World.Agents[i].ruleSet[j].getTradingRuleBitsSetString()  );
            }
         }
         ausgabe.close();
      } catch (IOException e) {
         System.err.println(e.toString());
      }
   }  // writeAverageStockHoldings

} 	// AsmModel

