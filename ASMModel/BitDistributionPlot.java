package ASMModel;

import java.awt.Font;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.data.DatasetUtilities;

public class BitDistributionPlot extends ApplicationFrame {

   protected double fundamentalBits[][] = new double[1][32];
   protected double technicalBits[][] = new double[1][32];
   protected CategoryDataset fundamentalDataset;
   protected CategoryDataset technicalDataset;


   public BitDistributionPlot(String title) {
        super(title);
        World.getFundamentalBits();
        World.getTechnicalBits();
        // add the chart to a panel...
        ChartPanel chartPanel = new ChartPanel(createChart());
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    public CategoryDataset createFDataset() {
      for (int i = 0; i < 32; i++) {
        fundamentalBits[0][i] = World.BitsSet[0][i];
      }
      fundamentalDataset = DatasetUtilities.createCategoryDataset(
         "Series ",
         "",
         fundamentalBits
      );
      return fundamentalDataset;
    }

    public CategoryDataset createTDataset() {
      for (int i = 0; i < 32; i++) {
        technicalBits[0][i] = World.BitsSet[1][i];
      }
      technicalDataset = DatasetUtilities.createCategoryDataset(
         "Series ",
         "",
         technicalBits
      );
      return technicalDataset;


    }

    private JFreeChart createChart() {

        CategoryDataset dataset1 = createFDataset();
        NumberAxis rangeAxis1 = new NumberAxis("# of set F-bits");
        rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer renderer1 = new BarRenderer();
        renderer1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
        subplot1.setDomainGridlinesVisible(true);

        CategoryDataset dataset2 = createTDataset();
        NumberAxis rangeAxis2 = new NumberAxis("# of set T-Bits");
        rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer renderer2 = new BarRenderer();
        renderer2.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
        subplot2.setDomainGridlinesVisible(true);

        CategoryAxis domainAxis = new CategoryAxis("Bit Number");
        CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
        plot.add(subplot1, 1);
        plot.add(subplot2, 1);

        JFreeChart result = new JFreeChart(
            "Fundamental and Technical Bit Distribution",
            new Font("SansSerif", Font.BOLD, 12),
            plot,
            false
        );
        return result;

    }


}
