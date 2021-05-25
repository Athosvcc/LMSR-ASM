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
   private static final int PRICE = 3;
   private static final int AVERAGEWEALTH = 6;
   private static final int TRADINGVOLUME = 7;
   private static final int OUTPUTFILE = 14;  // name of outputFile, not implemented yet
   private static final int WRITEFREQUENCY = 21;
   private static final int RECORDFREQUENCY = 22;
   private static final int STARTFROMPERIOD = 23;
   private static final int RECORDALLFROMPERIOD = 24;
   private static final int RECORDALLTOPERIOD = 25;
   private static final int BASEWEALTH = 42;
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
                     case OUTPUTFILE:   // filename for output file
                        AsmModel.recorderOptions.setRecorderOutputFile(st.nextToken().toString());
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
                     case MARKETMAKERREVENUE:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMarketMakerRevenue(false);
                           } else {
                              AsmModel.recorderOptions.setMarketMakerRevenue(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MARKETMAKERLIABILITIES:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMarketMakerLiabilities(false);
                           } else {
                              AsmModel.recorderOptions.setMarketMakerLiabilities(true);
                           }
                        } else {
                           System.out.println("Error while parsing line number "+lineNumber);
                        }
                        break;
                     case MARKETMAKERPROFIT:   // filename for output file
                        option = BooleanLexer(st.nextToken().toString());
                        if(option>-1) {
                           if(option==0) {
                              AsmModel.recorderOptions.setMarketMakerProfit(false);
                           } else {
                              AsmModel.recorderOptions.setMarketMakerProfit(true);
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
      if(word.equalsIgnoreCase("price:")) {
         token = PRICE;
         return token;
      }
      if(word.equalsIgnoreCase("price:")) {
         token = PRICE;
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
      if(word.equalsIgnoreCase("outputfile:")) {
         token = OUTPUTFILE;
         return token;
      }
      if(word.equalsIgnoreCase("true") || word.equalsIgnoreCase("false")) {
         token = OTHER;
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
      if(word.equalsIgnoreCase("recordalltoperiod:")) {
         token = RECORDALLTOPERIOD;
         return token;
      }
//      if(word.equalsIgnoreCase("averagewealthofsfiagents:")) { // mudar
//         token = AVERAGEWEALTHOFSFIAGENTS;
//         return token;
//      }
//      if(word.equalsIgnoreCase("averagewealthofnesfiagents:")) {
//         token = AVERAGEWEALTHOFNESFIAGENTS;
//         return token;
//      }
      if(word.equalsIgnoreCase("outputfile:")) {
         token = OUTPUTFILE;
         return token;
      }
      if(word.equalsIgnoreCase("baseWealth:")) {
         token = BASEWEALTH;
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