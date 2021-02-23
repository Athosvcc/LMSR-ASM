JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        Agent.java \
        AsmModel.java \
        Asset.java \
        BitDistributionPlot.java \
        ExecutePeriod.java \
        NESFIAgent.java \
        ObserverOptions.java \
        RecorderOptions.java \
        RecorderParamFileReader.java \
        SFIAgent.java \
        Specialist.java \
        Stats.java \
        Stock.java \
        TradingRule.java \
        World.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class