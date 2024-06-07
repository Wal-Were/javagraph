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
import javax.swing.*;
import java.awt.*;

public class RFM {
    static final double EPSILON = 1e-6;
    static UnivariateFunction func;
    static JFrame currentChartFrame = null;

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
        if (currentChartFrame != null) {
            currentChartFrame.dispose();
        }
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
        currentChartFrame = new SwingWrapper<>(chart).displayChart();
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

    private static double fixedPoint(double guess, Expression g) {
        double x0 = guess;
        double error = 1e-3;
        double x1;
        int maxIterations = 1000;
        int i = 0;
    
        while (true) {
            x1 = g.setVariable("x", x0).evaluate();  
            if (Double.isNaN(x1) || Double.isInfinite(x1)) {
                System.out.println("Function value is not defined at x = " + x0);
                break;
            }
            if (Math.abs(x1 - x0) < error || i >= maxIterations) {
                break;
            }
            x0 = x1;
            i++;
        }
    
        return x1;
    }

    private static double regulaFalsi(double a, double b) {
        if (func.value(a) * func.value(b) >= 0) {
            System.out.println("You have not assumed right a and b\n");
            return 0;
        }
    
        double c = a;
        int maxIterations = 1000;
    
        for (int i = 0; i < maxIterations; i++) {
            c = (a * func.value(b) - b * func.value(a)) / (func.value(b) - func.value(a));
    
            if (Math.abs(func.value(c)) < EPSILON) {
                break;
            }
    
            if (func.value(c) * func.value(a) < 0)
                b = c;
            else
                a = c;
        }
        return c;
    }
    
    private static double secant(double x0, double x1, double E) {
        if (func.value(x0) * func.value(x1) >= 0) {
            System.out.println("Initial guesses do not bracket a root");
            return 0;
        }
    
        double xm = 0;
        int maxIterations = 1000;
    
        for (int i = 0; i < maxIterations; i++) {
            double denominator = func.value(x1) - func.value(x0);
            if (Math.abs(denominator) < EPSILON) {
                System.out.println("Denominator is zero. Change initial values");
                break;
            }
    
            xm = x1 - ((func.value(x1) * (x1 - x0)) / denominator);
            if (Math.abs(func.value(xm)) < EPSILON) {
                break;
            }
            x0 = x1;
            x1 = xm;
        }
    
        return xm;
    }


    private static double newtonRaphson(double x) {
        double h = func.value(x) / derivative(x);
        while (Math.abs(h) >= EPSILON) {
            h = func.value(x) / derivative(x);
            x = x - h;
        }
    
        return x;
    }
    
    private static double derivative(double x) {
        double h = 1e-15;
        return (func.value(x + h) - func.value(x)) / h;
    }

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final boolean[] runAgain = {true};
            JFrame mainFrame = new JFrame(); 
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            String method = null;
            while (runAgain[0]) {
                if (method == null) {
                    String[] methods = {"Bisection Method", "Graphical Method", "Incremental Method", "Fixed Point Method", "Regula Falsi Method", "Newton Raphson Method", "Secant Method"};
                    method = (String) JOptionPane.showInputDialog(null, "Choose a method:", "Method", JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);
                }
    
                if (method != null) {
                    JTextField functionField = new JTextField(20);
                    JTextField xlField = new JTextField(5);
                    JTextField xhField = new JTextField(5);
                    JTextField deltaXField = new JTextField(5);
                    JTextField gxField = new JTextField(20);
                    JPanel panel = new JPanel(new GridLayout(0, 1));
                    panel.add(new JLabel("Enter your function:"));
                    panel.add(functionField);
                    panel.add(new JLabel("Enter the value for xl:"));
                    panel.add(xlField);
    
                    switch (method) {
                        case "Bisection Method":
                        case "Regula Falsi Method":
                        case "Secant Method":
                            panel.add(new JLabel("Enter the value for xh:"));
                            panel.add(xhField);
                            break;
                        case "Graphical Method":
                        case "Incremental Method":
                        case "Newton Raphson Method":
                            panel.add(new JLabel("Enter the delta x value:"));
                            panel.add(deltaXField);
                            break;
                        case "Fixed Point Method":
                            panel.add(new JLabel("Enter the delta x value:"));
                            panel.add(deltaXField);
                            panel.add(new JLabel("Enter the function g(x):"));
                            panel.add(gxField);
                            break;
                    }
    
                    int result = JOptionPane.showConfirmDialog(null, panel, "Please Enter Your Function and Values", JOptionPane.OK_CANCEL_OPTION);
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
                        double xh, deltaX, root;
                        String gxStr;
                        Expression g;
    
                        switch (method) {
                            case "Bisection Method":
                            case "Regula Falsi Method":
                            case "Secant Method":
                                xh = Double.parseDouble(xhField.getText());
                                root = bisection(xl, xh);  
                                new Thread(() -> plotFunctionAndRoot(func, xl, xh, root)).start();
                                JOptionPane.showMessageDialog(null, "The method found a root: " + root);
                                break;
                            case "Graphical Method":
                            case "Incremental Method":
                            case "Newton Raphson Method":
                                deltaX = Double.parseDouble(deltaXField.getText());
                                root = graphicalMethod(xl, xl + 300 * deltaX);  
                                new Thread(() -> plotFunctionAndRoot(func, xl, xl + 300 * deltaX, root)).start();
                                JOptionPane.showMessageDialog(null, "The estimated root is: " + root);
                                break;
                            case "Fixed Point Method":
                                deltaX = Double.parseDouble(deltaXField.getText());
                                gxStr = gxField.getText();
                                g = new ExpressionBuilder(gxStr)
                                    .variable("x")
                                    .build();
                                root = fixedPoint(xl, g); 
                                new Thread(() -> plotFunctionAndRoot(func, xl - deltaX, xl + deltaX, root)).start();
                                JOptionPane.showMessageDialog(null, "The method found a root: " + root);
                                break;
                        }
                    }
                }
    
                String[] options = {"Change Method and Values", "Change Values", "Exit"};
                JOptionPane optionPane = new JOptionPane(
                    "Do you want to use the program again?",
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    null,
                    options,
                    options[0]
                );

                JDialog dialog = optionPane.createDialog("Run Again");
                dialog.setLocation(400, 550); 
                dialog.setVisible(true);

                Object selectedValue = optionPane.getValue();
                if (selectedValue == null) {
                    runAgain[0] = false;
                } else {
                    String selectedOption = (String) selectedValue;
                    switch (selectedOption) {
                        case "Change Method and Values":
                            method = null;
                            break;
                        case "Change Values":
                            break;
                        case "Exit":
                            System.exit(0);
                            break;
                        default:
                            runAgain[0] = false;
                            break;
                    }
                }
            }
            mainFrame.dispose();
    });
}
    
    private static double getDoubleInput(String message) {
        String valueStr = JOptionPane.showInputDialog(null, message);
        return Double.parseDouble(valueStr);
    }
}    
