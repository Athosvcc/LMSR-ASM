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
 * This is the main file of the LMSR-ASM. It has all the Repast specific stuff in it
 * and controls the model.
 */
public class AsmModel extends SimModelImpl {

   protected static World world;
   protected static Specialist specialist;
   protected static boolean showDisplays = true;
   protected static boolean recordData = false ;
   private static String recorderParamFile = "recorder.pf" ;
   protected static boolean LS_LMSR = false ;
   protected static OpenSequenceGraph priceGraph;
   protected static OpenSequenceGraph volumeGraph;
   protected static OpenSequenceGraph wealthGraph;
   protected static OpenSequenceGraph MMGraph;
   protected static OpenSequenceGraph LMSRGraph;

   private Object2DTorus agentWorld;   // to display and probe the agents
   protected static DisplaySurface dsurf;
   protected static DataRecorder recorder;
   protected static RecorderOptions recorderOptions;
   protected LMSRAgent staticLMSRAgent;

   protected static ObserverOptions observer;
   private Schedule schedule;
   protected static Normal LMSRNormal;
   private static long rngSeed;
   private static RandomElement generator;
   private static final int DIGITS = 4;
   protected LMSRStock stockLMSR;
   protected static boolean stopNow = false;

   public AsmModel () {
      // adds boolean checkboxes to the NESFI-Settings windows
      BooleanPropertyDescriptor bd2 = new BooleanPropertyDescriptor("RecordData", true);
      descriptors.put("RecordData", bd2);
      BooleanPropertyDescriptor bd5 = new BooleanPropertyDescriptor("LMSR", true);
      descriptors.put("LMSR", bd5); // mudar

      // adds a List to choose an option
      Hashtable h3 = new Hashtable();
      h3.put(new Integer(Specialist.SELECT_2DIGITS),    "Round 2 Digits");
      h3.put(new Integer(Specialist.SELECT_6DIGITS), "Round 6 Digits");
      h3.put(new Integer(Specialist.SELECT_FLOAT),"Use Float");
      ListPropertyDescriptor pd3 = new ListPropertyDescriptor("MarketMakerMethod", h3);
      descriptors.put("MarketMakerMethod", pd3);

      Hashtable h4 = new Hashtable();
      h4.put(new Integer(Agent.IDEAL), "Ideal");
      h4.put(new Integer(Agent.LOGIT), "Logit");
      h4.put(new Integer(Agent.RANDOMWALK),"Random Walk");
      ListPropertyDescriptor pd4 = new ListPropertyDescriptor("AgentType", h4);
      descriptors.put("AgentType", pd4);


      // create objects only for the sake of getting these options out of the main
      // settings windows. Repast requires actual objects that can be probed.
      observer = new ObserverOptions();
      recorderOptions = new RecorderOptions();
      try {
         new RecorderParamFileReader(recorderParamFile);
      }
      catch (FileNotFoundException ex) {
      }



      // create objects to set parameters that are common to all other
      // instances of these classes.
      stockLMSR = new LMSRStock();
      staticLMSRAgent = new LMSRAgent(-1);
   }  // constructor AsmModel

   private void buildModel() {

      // LMSRNormal creates the noise for the probability process
      // rngSeed is set by the gui
      Random.createUniform();
      LMSRNormal = new Normal(0.0, 0.05, new MersenneTwister((int)getRngSeed()) ); // as set in Prediction Market Liquidity
      stockLMSR.initialize();

      world = new World();
      World.LMSRStocks = stockLMSR;
      specialist = new Specialist();
      World.createAgents();
      if (recordData) {	// writes data to an ascii-file
         recorder = new DataRecorder(recorderOptions.getRecorderOutputFile(), this, "Data Recording of LMSR-ASM" );
         recorder.setDelimiter(";"); // mudar?

         if (recorderOptions.getPrice()) {
            class WriteYesPrice implements NumericDataSource {
               public double execute() {
                  return stockLMSR.getPrice();
               }
            }
            class WriteNoPrice implements NumericDataSource {
               public double execute() { return stockLMSR.getPriceNoStock(); }
            }
            class WritePriceSum implements NumericDataSource {
               public double execute() { return stockLMSR.getPriceSum(); }
            }   
            recorder.addNumericDataSource("Price of Yes Stock", new WriteYesPrice(), 3, 2);
            recorder.addNumericDataSource("Price of No Stock", new WriteNoPrice(), 3, 2);
            recorder.addNumericDataSource("Sum of All Prices", new WritePriceSum(), 3, 2);
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
         if (recorderOptions.getBLiq()) {
            class WriteBLiq implements NumericDataSource {
               public double execute() {
                  return stockLMSR.getBLiq();
               }
            }
            recorder.addNumericDataSource("bLiq", new WriteBLiq(), 3, 2);
         } // if scheduled to record bLiq


         if (recorderOptions.getAverageWealth()) {
            class WriteAverageWealth implements NumericDataSource {
               public double execute() {
                  return World.getAverageWealth();
               }
            }
            recorder.addNumericDataSource("Average Wealth", new WriteAverageWealth(), 7, DIGITS);
         } // if scheduled to record the average wealth of agents in the economy
      }  // if record
   }  // buildModel

   private void buildDisplay() {
      if(showDisplays) {
         final LMSRStock stockLMSR = World.LMSRStocks;
         if(observer.showStocks) {
            priceGraph = new OpenSequenceGraph("Prices", this);
            priceGraph.setYRange(0 , 1.2);
            priceGraph.setYIncrement(2.5);
            priceGraph.setXRange(0 , 1.0);
            if(observer.showPrice) {
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
               priceGraph.addSequence("Sum of All Prices", new Sequence() {
                  public double getSValue() {
                     return stockLMSR.getPriceSum();
                  }
               });
            }  // if showPrice
            if(observer.showProbability) {
               priceGraph.addSequence("Probability", new Sequence() {
                  public double getSValue() {
                     return stockLMSR.getProbability();
                  }
               });
            }  // showProbability
         }  // if(observer.showStocks)
         if(observer.showVolume) {
            volumeGraph = new OpenSequenceGraph("Volume over time", this);
            this.registerMediaProducer("Volume over Time ", volumeGraph );
            volumeGraph.setYRange(0 , 45);
            volumeGraph.setYIncrement(1);
            volumeGraph.setXRange(0 , 1.0);
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
         }  // showVolume
         if(observer.getShowMarketMakerRevenue() || observer.getShowMarketMakerLiabilities() || observer.getShowMarketMakerProfit()) {
            MMGraph = new OpenSequenceGraph("Market Maker Cash Flow", this);
            this.registerMediaProducer("Market Maker Cash Flow ", MMGraph );
            MMGraph.setYRange(0 , 20);
            MMGraph.setYIncrement(1);
            MMGraph.setXRange(0 , 1.0);
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
            LMSRGraph.setXRange(0 , 1.0);
            LMSRGraph.addSequence("bLiq", new Sequence() {
               public double getSValue() {
                  return stockLMSR.getBLiq();
               }
            });
         }  // showLMSRSettings
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
      } 	// buildSchedule

      public void reInitialize() {
      }

      public void conditionalStop() {
            if(stopNow) stop();
      }  // conditionalStop()


   public void begin() {
      buildModel();
      if(showDisplays) {
          buildDisplay();
      }
      buildSchedule();
      if (showDisplays) {
         if (observer.showPrice ) {
            priceGraph.display();
         }
         if(observer.getShowVolume()) {
            volumeGraph.display();
         }
         if(observer.showAgentGrid) {
            dsurf.display();
         }
         if(observer.getShowMarketMakerRevenue() || observer.getShowMarketMakerLiabilities() || observer.getShowMarketMakerProfit() ) {
            MMGraph.display();
         }
         if(observer.getShowBLiq()) {
            LMSRGraph.display();
         }
//         if(observer.getShowWealthSFIAgents() || observer.getShowWealthNESFIAgents()) {
//            wealthGraph.display(); // mudar
//         }
      }
   }  // begin


   public static void main(String[] args) {
      Locale.setDefault(new Locale("en", "US"));
      SimInit init = new SimInit();
      AsmModel model = new AsmModel();
      if (args.length > 0) {  // if there is a parameter file given, then the run is in batch-mode
         init.loadModel(model, args[0], true);
      } else {
         init.loadModel(model, null, false);
      }
   }  // public static void main( )

   public String getName() {
          return "LMSR-ASM";
   }
   public Schedule getSchedule() {
          return schedule;
   }
   public void stop() {
      if(this.getController().isBatch()) {
      }
      this.fireStopSim();
   }

   public void setup() { // restart settings for batch runs
      if (World.period > 0) {	// setup called after a simulation run
         stopNow = false;
         if(showDisplays) {
            if(observer.showPrice) {
               priceGraph.dispose();
            }
            if(observer.showAgentGrid) {
               dsurf.dispose();
            }
            if(observer.getShowVolume()) {
               volumeGraph.dispose();
            }
            if(observer.getShowBLiq()) {
               LMSRGraph.dispose();
            }
            if(observer.getShowMarketMakerRevenue() || observer.getShowMarketMakerLiabilities() || observer.getShowMarketMakerProfit() ) {
               MMGraph.dispose();
            }
         }  // showDisplays
         for (int i = 0 ; i < world.numberOfAgents ; i++) {
            World.Agents[i] = null;
         }
         specialist.setResetSpecialist();
         specialist = null;
         World.period=0;
         stockLMSR.setResetLMSRStocks();
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
   public int getNumberOfLMSRAgents() {
      return world.numberOfLMSRAgents;
   }
   public void setNumberOfLMSRAgents(int val) {
      world.numberOfLMSRAgents = val;
      world.numberOfAgents = world.numberOfLMSRAgents;
   }
   public boolean getShowDisplays() { return showDisplays; }
   public void setShowDisplays(boolean showDisplays) {
      this.showDisplays = showDisplays;
   }
   public boolean getRecordData() { return recordData; }
   public void setRecordData(boolean recordData) {
      this.recordData = recordData;
   }
   public boolean getLS_LMSR() { return LS_LMSR; }
   public void setLS_LMSR(boolean LS_LMSR) {
      this.LS_LMSR = LS_LMSR;
   }

   public int getNumberOfPeriods() { return world.numberOfPeriods; }
   public void setNumberOfPeriods(int numberOfPeriods) { world.numberOfPeriods = numberOfPeriods;   }
   public double getInterestRate() { return World.interestRate; }
   public void setInterestRate(double interestRate) { World.interestRate = interestRate; }
   public void setMarketMakerMethod(int MMMethod) { Specialist.selectionMethod = MMMethod; }
   public int getMarketMakerMethod() { return Specialist.selectionMethod ; }
   public void setAgentType(int agentType) { LMSRAgent.agentType = agentType; }
   public int getAgentType() { return LMSRAgent.agentType ; }
   public void setMemory(int memory) { Asset.setMemory(memory); }
   public int getMemory() { return Asset.getMemory() ; }

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

   public void setLMSRAgent(LMSRAgent val) { }
   public LMSRAgent getLMSRAgent() { return staticLMSRAgent ; }
//   public void setAgent(Agent val) {}
//   public Agent getAgent() { return staticAgent ; }
   public LMSRStock getStockLMSR() { return stockLMSR; }
   public void setStockLMSR(LMSRStock val) { this.stockLMSR = val; }


   /* The next parameters don't show up in the GUI,
      yet the get- and set procedure are included here to be able to set them from the
      parameter file. It would be nice to extend the model such that there are individual
      parameter files for the object parameter settings. Right now, these parameters are handled from the
      single parameter file, and the respective get- and set-methods have to be public in the
      top level AsmModel-Class.
   */
   public void setRiskAversion(double val) { staticLMSRAgent.riskAversion = val; }   // need it for Batch-runs
   public double getRiskAversion() { return staticLMSRAgent.riskAversion ; }
   public void setProbabilityProcess(int probabilityProcess) { LMSRStock.probabilityProcess = probabilityProcess; }
   public int getProbabilityProcess() { return LMSRStock.probabilityProcess; }
   public void setAlphaLS(double val) { LMSRStock.alphaLS = val; }
   public double getAlphaLS() { return LMSRStock.alphaLS ; }
   public void setBLiq(double val) { LMSRStock.bLiq = val; }
   public double getBLiq() { return LMSRStock.bLiq ; }
   public void setInitialPrice(double val) { LMSRStock.initialPrice = val;}
   public double getInitialPrice() {return LMSRStock.initialPrice ;}
   public double getInitialQuantity() { return LMSRStock.initialQuantity; }
   public void setInitialQuantity(double val) { LMSRStock.initialQuantity = val; }
   public void setPeriodShock(double val) { LMSRStock.periodShock = val; }
   public double getPeriodShock() { return LMSRStock.periodShock ; }
   public void setProbAfterShock(double val) { LMSRStock.probAfterShock = val;}
   public double getProbAfterShock() {return LMSRStock.probAfterShock ;}
   public double getProbability() { return LMSRStock.probability; }
   public void setProbability(double val) { LMSRStock.probability = val; }
   public void setRecorderParamFile(String val) {recorderParamFile = val; }
   public String getRecorderParamFile() { return recorderParamFile; }
   public void setRecorderOutputFile(String val) {recorderOptions.setRecorderOutputFile(val); }
   public String getRecorderOutputFile() { return recorderOptions.getRecorderOutputFile(); }

   public String[] getInitParam() {
      if(this.getController().isBatch()) {
         String[] params = {"numberOfLMSRAgents","numberOfPeriods","recordData","interestRate","memory",
         "showDisplays","riskAversion","recorderParamFile","recorderOutputFile",
         "MarketMakerMethod","AgentType","reInitializeAt",
         "LS_LMSR","alphaLS","bLiq","initialPrice","initialQuantity",
         "periodShock","probAfterShock","probability", "probabilityProcess"
         };
         return params;
      } else {
         Controller.ALPHA_ORDER= false;   // show the variable not in alphabetical order but in the order as they are in the string array.
         String[] params = {"LMSRAgent","numberOfLMSRAgents","MarketMakerMethod","AgentType",
                 "numberOfPeriods","interestRate","memory","LS_LMSR",
                 "stockLMSR","showDisplays","observer","recordData","recorderOptions","reInitializeAt"};
         return params;
      }
   }  // getInitParam()

} 	// AsmModel

