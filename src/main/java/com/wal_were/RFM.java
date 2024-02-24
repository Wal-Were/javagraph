package com.wal_were;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class RFM {
    static final double EPSILON = 0.01;
    static UnivariateFunction func;

    private static double bisection(double a, double b) {
        double c = a;
        int iteration = 0;
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Iteration", "a", "b", "Root"}, 0);
        while ((b - a) >= EPSILON) {
            c = (a + b) / 2;
            model.addRow(new Object[]{iteration, a, b, c});
            if (func.value(c) == 0.0)
                break;
            else if (func.value(c) * func.value(a) < 0)
                b = c;
            else
                a = c;
            iteration++;
        }
        System.out.printf("The value of root is : %.4f\n", c);

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        JFrame frame = new JFrame("Bisection Method Results");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scrollPane);
        frame.pack();
        frame.setVisible(true);

        return c;
    }

    private static void plotFunctionAndRoot(UnivariateFunction function, double start, double end, double root) {
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        double xStart = Math.min(start, root) - 1;  
        double xEnd = Math.max(end, root) + 1;  
    
        for (double x = xStart; x <= xEnd; x += 0.01) {
            double y = function.value(x);
            System.out.println("x: " + x + ", y: " + y);  // Add this line
            xData.add(x);
            yData.add(y);
        }
        List<Double> rootXData = new ArrayList<>();
        List<Double> rootYData = new ArrayList<>();
        rootXData.add(root);
        rootYData.add(0.0);
        XYChart chart = QuickChart.getChart("Function and Root", "X", "Y", "y(x)", xData, yData);
        chart.addSeries("Root", rootXData, rootYData);
        chart.getStyler().setYAxisMin(Math.min(0, function.value(root)) - 1);  
        chart.getStyler().setYAxisMax(Math.max(0, function.value(root)) + 1);  
        new SwingWrapper<>(chart).displayChart();
    }

    private static double graphicalMethod(double start, double end) {
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        double prevX = start;
        double prevY = func.value(prevX);
        double root = Double.NaN;
    
        for (double x = start; x <= end; x += 0.01) {
            double y = func.value(x);
            xData.add(x);
            yData.add(y);
            if (Math.signum(y) != Math.signum(prevY)) {
                root = (x + prevX) / 2;  
                break;
            }
            prevX = x;
            prevY = y;
        }
    
        XYChart chart = QuickChart.getChart("Function", "X", "Y", "y(x)", xData, yData);
        new Thread(() -> new SwingWrapper<>(chart).displayChart()).start();
    
        return root;
    }

    private static double incrementalMethod(double a, double deltaX) {
        double x = a;
        double prevY = func.value(x);
        while (true) {
            x += deltaX;
            double y = func.value(x);
            if (Math.signum(y) != Math.signum(prevY)) {
                System.out.println("Root found between " + (x - deltaX) + " and " + x);
                return x;
            }
            prevY = y;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField functionField = new JTextField(20);
            JTextField xlField = new JTextField(5);
            JTextField deltaXField = new JTextField(5);
            JPanel myPanel = new JPanel();
            myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS)); 
            myPanel.add(new JLabel("Enter your function:"));
            myPanel.add(functionField);
            myPanel.add(Box.createVerticalStrut(15)); 
            myPanel.add(new JLabel("Enter the value for xl:"));
            myPanel.add(xlField);
            myPanel.add(Box.createVerticalStrut(15)); 
            myPanel.add(new JLabel("Enter the value for delta x:"));
            myPanel.add(deltaXField);
            int result = JOptionPane.showConfirmDialog(null, myPanel, 
                   "Please Enter Your Function and Values for xl and delta x", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String functionStr = functionField.getText();
                Expression e = new ExpressionBuilder(functionStr)
                    .variable("x")
                    .build();
    
                func = new UnivariateFunction() {
                    @Override
                    public double value(double x) {
                        e.setVariable("x", x);
                        return e.evaluate();
                    }
                };
    
                double xl = Double.parseDouble(xlField.getText());
                double deltaX = Double.parseDouble(deltaXField.getText());
                String[] methods = {"Bisection Method", "Graphical Method", "Incremental Method"};
                String method = (String) JOptionPane.showInputDialog(null, "Choose a method:", "Method", JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);
    
                switch (method) {
                    case "Bisection Method":
                        double root = bisection(xl, 2);  
                        new Thread(() -> plotFunctionAndRoot(func, xl, xl + deltaX, root)).start();
                        JOptionPane.showMessageDialog(null, "The method found a root: " + root);
                    break;
                    case "Graphical Method":
                        root = graphicalMethod(xl, xl + 300 * deltaX);  
                        new Thread(() -> plotFunctionAndRoot(func, xl, xl + 300 * deltaX, root)).start();
                        JOptionPane.showMessageDialog(null, "The estimated root is: " + root);
                        break;
                    case "Incremental Method":
                        root = incrementalMethod(xl, deltaX);  
                        new Thread(() -> plotFunctionAndRoot(func, xl, xl + deltaX, root)).start();
                        JOptionPane.showMessageDialog(null, "The root found is: " + root);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid choice");
                }
            }
        });
    }
}