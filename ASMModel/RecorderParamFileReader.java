/*
 * The NESFI-ASM 1.0 (Norman Ehrentreich's Santa Fe Institute Artificial Stock Market)
 * Copyright (C) Norman Ehrentreich 2002 & The Santa Fe Institute 1995
 *
 * No warranty implied;
 *
 * Implementation of the RecorderParamFileReader Class
*/

package ASMModel;


import java.io.*;
import java.util.*;
import java.lang.*;
/**
 * If a parameter file exists, it is read by this class and sets the recording option
 * in RecorderOptions. This is intended for batch runs, but a parameter file can also
 * preset the Boolean values for the GUI. The parameter file itself is a plain
 * ASCII-file in which each line can set a certain option on or off.
 * Please make sure that there are no empty lines or wrong variable names.
 * No sophisticated error checking has been implemented yet.
 */
public class RecorderParamFileReader {

   private static final int INVALID_WORD = -1;
   private static final int OTHER = 0;
   private static final int TECHNICALBITS = 1;
   private static final int FUNDAMENTALBITS = 2;
   private static final int PRICE = 3;
   private static final int HREEPRICE = 4;
   private static final int DIVIDEND = 5;
   private static final int AVERAGEWEALTH = 6;
   private static final int TRADINGVOLUME = 7;
   private static final int MEANTRADINGVOLUME = 8;
   private static final int PVCORR = 11;
   private static final int OUTPUTFILE = 14;  // name of outputFile, not implemented yet
   private static final int MEANFITNESS = 15;
   private static final int MINFITNESS = 16;
   private static final int MAXFITNESS = 17;
   private static final int CRUDEPRICE = 18;
   private static final int BITFRACTIONS = 19;
   private static final int ACTIVERULES = 20;
   private static final int WRITEFREQUENCY = 21;
   private static final int RECORDFREQUENCY = 22;
   private static final int STARTFROMPERIOD = 23;
   private static final int RECORDALLFROMPERIOD = 24;
   private static final int RECORDALLTOPERIOD = 25;
   private static final int BITANALYZER = 26;
   private static final int AVERAGEWEALTHOFCLASSIFIERAGENTS = 27;
   private static final int AVERAGEWEALTHOFNOCLASSIFIERAGENTS = 28;
   private static final int AVERAGEWEALTHOFFUNDAMENTALTRADERS = 29;
   private static final int AVERAGEWEALTHOFTECHNICALTRADERS = 30;
   private static final int AVERAGEWEALTHOFNORMALLEARNER = 31;
   private static final int AVERAGEWEALTHOFFASTLEARNER = 32;
   private static final int AVERAGEWEALTHOFSFIAGENTS = 33;
   private static final int AVERAGEWEALTHOFNESFIAGENTS = 34;
   private static final int NUMBEROFGENERALIZATIONS = 35;
   private static final int FORECASTPARAMETERA = 36;
   private static final int WRITERULEUSAGE = 37;
   private static final int ZEROBITAGENTS = 38;
   private static final int SELECTAVERAGECOUNTER = 39;
   private static final int WEALTHZEROBITAGENTS = 40;
   private static final int WEALTHNONZEROBITAGENTS = 41;
   private static final int BASEWEALTH = 42;
   private static final int HREEBASEWEALTH = 43;
   private static final int NEWZEROFUNDAMENTALBITAGENTAT = 44;
   private static final int NEWZEROTECHNICALBITAGENTAT = 45;
   private static final int NEWZEROBITAGENTAT = 46;
   private static final int WRITEAVERAGESTOCKHOLDINGS = 47;
   private static final int MARKETMAKERREVENUE = 48;
   private static final int MARKETMAKERLIABILITIES = 49;
   private static final int MARKETMAKERPROFIT = 50;

   private RandomAccessFile pFile;
   private String fileName;

   public RecorderParamFileReader(String arg) throws FileNotFoundException {
      String line;
      int lineNumber = 0;
      int option = 0;
      pFile = new RandomAccessFile(arg,"r");
      try {
         line = pFile.readLine();
         while (line!= null) {
            // line = line.replace(':',' ');
            lineNumber++;
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
               option = Lexer(st.nextToken().toString());
               if(option >-1) {
                  switch(option) {
                     case TECHNICALBITS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setTechnicalBits(false);
                           } else {
                              AsmModel.recorderOptions.setTechnicalBits(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case FUNDAMENTALBITS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setFundamentalBits(false);
                           } else {
                              AsmModel.recorderOptions.setFundamentalBits(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case PRICE:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setPrice(false);
                           } else {
                              AsmModel.recorderOptions.setPrice(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case HREEPRICE:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setHreePrice(false);
                           } else {
                              AsmModel.recorderOptions.setHreePrice(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case DIVIDEND:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setDividend(false);
                           } else {
                              AsmModel.recorderOptions.setDividend(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTH:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealth(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealth(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case TRADINGVOLUME:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setTradingVolume(false);
                           } else {
                              AsmModel.recorderOptions.setTradingVolume(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MEANTRADINGVOLUME:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMeanTradingVolume(false);
                           } else {
                              AsmModel.recorderOptions.setMeanTradingVolume(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case PVCORR:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setPVCorr(false);
                           } else {
                              AsmModel.recorderOptions.setPVCorr(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MEANFITNESS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMeanFitness(false);
                           } else {
                              AsmModel.recorderOptions.setMeanFitness(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MINFITNESS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMinFitness(false);
                           } else {
                              AsmModel.recorderOptions.setMinFitness(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MAXFITNESS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMaxFitness(false);
                           } else {
                              AsmModel.recorderOptions.setMaxFitness(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case CRUDEPRICE:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setCrudePrice(false);
                           } else {
                              AsmModel.recorderOptions.setCrudePrice(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case BITFRACTIONS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setBitFractions(false);
                           } else {
                              AsmModel.recorderOptions.setBitFractions(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case ACTIVERULES:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setActiveRules(false);
                           } else {
                              AsmModel.recorderOptions.setActiveRules(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case BITANALYZER:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setBitAnalyzer(false);
                           } else {
                              AsmModel.recorderOptions.setBitAnalyzer(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case WRITEFREQUENCY:
                        option = NumericLexer(st.nextToken().toString());
                        if(option!=0) {
                           AsmModel.recorderOptions.setWriteFrequency(option);
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case RECORDFREQUENCY:
                        option = NumericLexer(st.nextToken().toString());
                        if(option!=0) {
                           AsmModel.recorderOptions.setRecordFrequency(option);
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case STARTFROMPERIOD:
                        option = NumericLexer(st.nextToken().toString());
                        if(option!=0) {
                           AsmModel.recorderOptions.setStartFromPeriod(option);
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case RECORDALLFROMPERIOD:
                        option = NumericLexer(st.nextToken().toString());
                        if(option!=0) {
                           AsmModel.recorderOptions.setRecordAllFromPeriod(option);
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case RECORDALLTOPERIOD:
                        option = NumericLexer(st.nextToken().toString());
                        if(option!=0) {
                           AsmModel.recorderOptions.setRecordAllToPeriod(option);
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFCLASSIFIERAGENTS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfClassifierAgents(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfClassifierAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFNOCLASSIFIERAGENTS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfNoClassifierAgents(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfNoClassifierAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;


                     case AVERAGEWEALTHOFFUNDAMENTALTRADERS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfFundamentalTraders(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfFundamentalTraders(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFTECHNICALTRADERS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfTechnicalTraders(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfTechnicalTraders(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFFASTLEARNER:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfFastLearner(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfFastLearner(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFNORMALLEARNER:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfNormalLearner(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfNormalLearner(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFSFIAGENTS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfSFIAgents(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfSFIAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case AVERAGEWEALTHOFNESFIAGENTS:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setAverageWealthOfNESFIAgents(false);
                           } else {
                              AsmModel.recorderOptions.setAverageWealthOfNESFIAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case OUTPUTFILE:   // filename for output file
                        AsmModel.recorderOptions.setRecorderOutputFile(st.nextToken().toString());
                        break;
                     case NUMBEROFGENERALIZATIONS:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNumberOfGeneralizations(false);
                           } else {
                              AsmModel.recorderOptions.setNumberOfGeneralizations(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case FORECASTPARAMETERA:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setForecastParameterA(false);
                           } else {
                              AsmModel.recorderOptions.setForecastParameterA(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case WRITERULEUSAGE:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setWriteRuleUsage(false);
                           } else {
                              AsmModel.recorderOptions.setWriteRuleUsage(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case WRITEAVERAGESTOCKHOLDINGS:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setWriteAverageStockHoldings(false);
                           } else {
                              AsmModel.recorderOptions.setWriteAverageStockHoldings(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;

                     case SELECTAVERAGECOUNTER:
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setSelectAverageCounter(false);
                           } else {
                              AsmModel.recorderOptions.setSelectAverageCounter(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;

                     case ZEROBITAGENTS:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setZeroBitAgents(false);
                           } else {
                              AsmModel.recorderOptions.setZeroBitAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case WEALTHZEROBITAGENTS:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setWealthZeroBitAgents(false);
                           } else {
                              AsmModel.recorderOptions.setWealthZeroBitAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case WEALTHNONZEROBITAGENTS:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setWealthNonZeroBitAgents(false);
                           } else {
                              AsmModel.recorderOptions.setWealthNonZeroBitAgents(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case BASEWEALTH:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setBaseWealth(false);
                           } else {
                              AsmModel.recorderOptions.setBaseWealth(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case HREEBASEWEALTH:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setHreeBaseWealth(false);
                           } else {
                              AsmModel.recorderOptions.setHreeBaseWealth(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case NEWZEROFUNDAMENTALBITAGENTAT:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNewZeroFundamentalBitAgentAt(false);
                           } else {
                              AsmModel.recorderOptions.setNewZeroFundamentalBitAgentAt(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case NEWZEROTECHNICALBITAGENTAT:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNewZeroTechnicalBitAgentAt(false);
                           } else {
                              AsmModel.recorderOptions.setNewZeroTechnicalBitAgentAt(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case NEWZEROBITAGENTAT:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNewZeroBitAgentAt(false);
                           } else {
                              AsmModel.recorderOptions.setNewZeroBitAgentAt(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MARKETMAKERREVENUE:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNewZeroFundamentalBitAgentAt(false);
                           } else {
                              AsmModel.recorderOptions.setNewZeroFundamentalBitAgentAt(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MARKETMAKERLIABILITIES:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNewZeroTechnicalBitAgentAt(false);
                           } else {
                              AsmModel.recorderOptions.setNewZeroTechnicalBitAgentAt(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MARKETMAKERPROFIT:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setNewZeroBitAgentAt(false);
                           } else {
                              AsmModel.recorderOptions.setNewZeroBitAgentAt(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;

                  }
               } else {
                  System.out.println("Error while parsing line number "+lineNumber);
                  System.exit(0);
               }

               // System.out.println(st.nextToken());
            }

            line = pFile.readLine();
         }  // while there is a non-empty line
         pFile.close();
      } catch (IOException e) {
         System.out.println(e.toString());
      }

   }  // RecorderParamFileReader()

   private int Lexer(String word) {
      int token = -1;
      if(word.equalsIgnoreCase("technicalbits:")) {
         token = TECHNICALBITS;
         return token;
      }
      if(word.equalsIgnoreCase("fundamentalbits:")) {
         token = FUNDAMENTALBITS;
         return token;
      }
      if(word.equalsIgnoreCase("bitfractions:")) {
         token = BITFRACTIONS;
         return token;
      }
      if(word.equalsIgnoreCase("price:")) {
         token = PRICE;
         return token;
      }
      if(word.equalsIgnoreCase("price:")) {
         token = PRICE;
         return token;
      }
      if(word.equalsIgnoreCase("hreeprice:")) {
         token = HREEPRICE;
         return token;
      }
      if(word.equalsIgnoreCase("crudeprice:")) {
         token = CRUDEPRICE;
         return token;
      }
      if(word.equalsIgnoreCase("dividend:")) {
         token = DIVIDEND;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealth:")) {
         token = AVERAGEWEALTH;
         return token;
      }
      if(word.equalsIgnoreCase("tradingvolume:")) {
         token = TRADINGVOLUME;
         return token;
      }
      if(word.equalsIgnoreCase("meantradingvolume:")) {
         token = MEANTRADINGVOLUME;
         return token;
      }
      if(word.equalsIgnoreCase("pvcorr:")) {
         token = PVCORR;
         return token;
      }
      if(word.equalsIgnoreCase("outputfile:")) {
         token = OUTPUTFILE;
         return token;
      }
      if(word.equalsIgnoreCase("true") || word.equalsIgnoreCase("false")) {
         token = OTHER;
         return token;
      }
      if(word.equalsIgnoreCase("meanfitness:")) {
         token = MEANFITNESS;
         return token;
      }
      if(word.equalsIgnoreCase("minfitness:")) {
         token = MINFITNESS;
         return token;
      }
      if(word.equalsIgnoreCase("maxfitness:")) {
         token = MAXFITNESS;
         return token;
      }
      if(word.equalsIgnoreCase("activerules:")) {
         token = ACTIVERULES;
         return token;
      }
      if(word.equalsIgnoreCase("writefrequency:")) {
         token = WRITEFREQUENCY;
         return token;
      }
      if(word.equalsIgnoreCase("recordfrequency:")) {
         token = RECORDFREQUENCY;
         return token;
      }
      if(word.equalsIgnoreCase("startfromperiod:")) {
         token = STARTFROMPERIOD;
         return token;
      }
      if(word.equalsIgnoreCase("recordallfromperiod:")) {
         token = RECORDALLFROMPERIOD;
         return token;
      }
      if(word.equalsIgnoreCase("bitanalyzer:")) {
         token = BITANALYZER;
         return token;
      }
      if(word.equalsIgnoreCase("recordalltoperiod:")) {
         token = RECORDALLTOPERIOD;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthofclassifieragents:")) {
         token = AVERAGEWEALTHOFCLASSIFIERAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthofnoclassifieragents:")) {
         token = AVERAGEWEALTHOFNOCLASSIFIERAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthoffundamentaltraders:")) {
         token = AVERAGEWEALTHOFFUNDAMENTALTRADERS;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthoftechnicaltraders:")) {
         token = AVERAGEWEALTHOFTECHNICALTRADERS;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthoffastlearner:")) {
         token = AVERAGEWEALTHOFFASTLEARNER;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthofnormallearner:")) {
         token = AVERAGEWEALTHOFNORMALLEARNER;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthofsfiagents:")) {
         token = AVERAGEWEALTHOFSFIAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("averagewealthofnesfiagents:")) {
         token = AVERAGEWEALTHOFNESFIAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("outputfile:")) {
         token = OUTPUTFILE;
         return token;
      }
      if(word.equalsIgnoreCase("numberofgeneralizations:")) {
         token = NUMBEROFGENERALIZATIONS;
         return token;
      }
      if(word.equalsIgnoreCase("forecastparametera:")) {
         token = FORECASTPARAMETERA;
         return token;
      }
      if(word.equalsIgnoreCase("writeruleusage:")) {
         token = WRITERULEUSAGE;
         return token;
      }
      if(word.equalsIgnoreCase("writeaveragestockholdings:")) {
         token = WRITEAVERAGESTOCKHOLDINGS;
         return token;
      }
      if(word.equalsIgnoreCase("zerobitagents:")) {
         token = ZEROBITAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("selectAverageCounter:")) {
         token = SELECTAVERAGECOUNTER;
         return token;
      }
      if(word.equalsIgnoreCase("averageWealthOfZeroBitAgents:")) {
         token = WEALTHZEROBITAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("averageWealthOfNonZeroBitAgents:")) {
         token = WEALTHNONZEROBITAGENTS;
         return token;
      }
      if(word.equalsIgnoreCase("baseWealth:")) {
         token = BASEWEALTH;
         return token;
      }
      if(word.equalsIgnoreCase("hreeBaseWealth:")) {
         token = HREEBASEWEALTH;
         return token;
      }
      if(word.equalsIgnoreCase("newZeroFundamentalBitAgentAt:")) {
         token = NEWZEROFUNDAMENTALBITAGENTAT;
         return token;
      }
      if(word.equalsIgnoreCase("newZeroTechnicalBitAgentAt:")) {
         token = NEWZEROTECHNICALBITAGENTAT;
         return token;
      }
      if(word.equalsIgnoreCase("newZeroBitAgentAt:")) {
         token = NEWZEROBITAGENTAT;
         return token;
      }
      if(word.equalsIgnoreCase("marketMakerRevenue:")) {
         token = MARKETMAKERREVENUE;
         return token;
      }
      if(word.equalsIgnoreCase("marketMakerLiabilities:")) {
         token = MARKETMAKERLIABILITIES;
         return token;
      }
      if(word.equalsIgnoreCase("marketMakerProfit:")) {
         token = MARKETMAKERPROFIT;
         return token;
      }



      return token;
   }  // Lexer

    private int BooleanLexer(String word) {
      int token = -1;
      if(word.equalsIgnoreCase("false") ) {
         return 0;
      }
      if(word.equalsIgnoreCase("true") ) {
         return 1;
      }
      return token;
   }

   private int NumericLexer(String word) {
      return Integer.valueOf(word).intValue();
   }


}