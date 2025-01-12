import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


class TangentLineCalculator extends JFrame {
    private JTextField xInputField; // Text field for entering x-coordinate
    private DrawingPanel drawingPanel; // Reference to the drawing panel
    private JButton zoomInButton, zoomOutButton; // Buttons for zooming
    private JLabel equationLabel; // Label to display tangent line equation

    public TangentLineCalculator() {
        setTitle("Tangent Line Calculator");
        setSize(800, 668); // Increased height for text field

        // Panel for text field and drawing panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel for text field, buttons, and equation label
        JPanel inputPanel = new JPanel();
        JLabel xLabel = new JLabel("Enter x-coordinate:");
        xInputField = new JTextField(5);
        xInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update tangent point when Enter is pressed
                double newTangentPoint = Double.parseDouble(xInputField.getText());
                drawingPanel.setTangentPoint(newTangentPoint);
            }
        });

        // Zoom buttons
        zoomInButton = new JButton("Zoom In");
        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.zoomIn();
            }
        });

        zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.zoomOut();
            }
        });

        
        inputPanel.add(xLabel);
        inputPanel.add(xInputField);
        inputPanel.add(zoomInButton);
        inputPanel.add(zoomOutButton);

        // Add text field panel and drawing panel to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        drawingPanel = new DrawingPanel();
        mainPanel.add(drawingPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    public static void main(String[] args) {
        new TangentLineCalculator().setVisible(true);
    }
}


class Function {
    public static double f(double x) {
        return x * x;  // Function: x^2
    }

    public static double fPrime(double x) {
        return 2 * x;  // Derivative: 2x for function x^2
    }
}

class DrawingPanel extends JPanel {
    private double tangentPoint = 0; // Selected point for tangent calculation
    private double scale = 40; // Initial scale for zooming (600 pixels / 10 units = 60 pixels per unit)
    private double offsetX = 0; // Offset for panning
    private double offsetY = 0; // Offset for panning
    private Point lastMousePosition; // Last recorded mouse position for dragging
    private Timer animationTimer; // Timer for animation
    private double animationStep = 0.02; // Step size for animation
    private double currentTangentPoint; // Current point of tangent line during animation
    private double fx; // Value of function at tangent point
    private double fxPrime; // Value of derivative at tangent point
    
    
    //constructor
    public DrawingPanel() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePosition = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMousePosition.x;
                int dy = e.getY() - lastMousePosition.y;
                offsetX += dx / scale;
                offsetY -= dy / scale;
                lastMousePosition = e.getPoint();
                repaint();
            }
        });

        // Initialize animation timer for moving tangent point
        animationTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentTangentPoint > tangentPoint) {
                    currentTangentPoint -= animationStep;
                    if (currentTangentPoint <= tangentPoint) {
                        currentTangentPoint = tangentPoint;
                        animationTimer.stop();
                    }
                } else if (currentTangentPoint < tangentPoint) {
                    currentTangentPoint += animationStep;
                    if (currentTangentPoint >= tangentPoint) {
                        currentTangentPoint = tangentPoint;
                        animationTimer.stop();
                    }
                }
                repaint();
            }
        });
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawAxes(g);
        plotFunction(g);
        plotTangentLine(g);
    }

    private void drawAxes(Graphics g) {
        g.setColor(Color.BLACK);
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2 - (int) (offsetX * scale);
        int centerY = height / 2 + (int) (offsetY * scale);
        g.drawLine(0, centerY, width, centerY); // x-axis
        g.drawLine(centerX, 0, centerX, height); // y-axis

        // Draw x-axis values
        for (int i = -width ; i <= width ; i += scale) {
            int x = centerX + i;
            g.drawLine(x, centerY - 5, x, centerY + 5);
            g.drawString(String.format("%.1f", (i / (double) scale)), x - 10, centerY + 20);
        }

        // Draw y-axis values
        for (int i = -height ; i <= height ; i += scale) {
            int y = centerY + i;
            g.drawLine(centerX - 5, y, centerX + 5, y);
            g.drawString(String.format("%.1f", (-i / (double) scale)), centerX + 10, y + 5);
        }
    }

    private void plotFunction(Graphics g) {
        g.setColor(Color.RED); // Red for the function line
        int width = getWidth();
        int height = getHeight();
        
        // Set the range for x values in the visible area
        double startX = -width / (2 * scale);
        double endX = width / (2 * scale);
        double step = 0.01; // Small step for smooth curve

        for (double x = startX; x <= endX; x += step) {
            // Convert function coordinates to panel coordinates
            int x1 = (int) (width / 2 + (x - offsetX) * scale);
            int y1 = (int) (height / 2 - (Function.f(x) - offsetY) * scale);
            int x2 = (int) (width / 2 + (x + step - offsetX) * scale);
            int y2 = (int) (height / 2 - (Function.f(x + step) - offsetY) * scale);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void plotTangentLine(Graphics g) {
        g.setColor(Color.BLUE);
        int width = getWidth();
        int height = getHeight();

        // Set the range for x values in the visible area
        double startX = -width / (2 * scale);
        double endX = width / (2 * scale);

        // Draw tangent line based on currentTangentPoint
        for (double x = startX; x <= endX; x += 0.01) {
            double tangentY = fx + fxPrime * (x - currentTangentPoint);

            int x1 = (int) (width / 2 + (x - offsetX) * scale);
            int y1 = (int) (height / 2 - (tangentY - offsetY) * scale);
            double xNext = x + 0.01;
            double tangentYNext = fx + fxPrime * (xNext - currentTangentPoint);
            int x2 = (int) (width / 2 + (xNext - offsetX) * scale);
            int y2 = (int) (height / 2 - (tangentYNext - offsetY) * scale);

            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void setTangentPoint(double newTangentPoint) {
        // Stop the current animation if it's running
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        // Set the new tangent point and reset currentTangentPoint to start animation
        this.tangentPoint = newTangentPoint;
        
        if (tangentPoint <= 0){
            this.currentTangentPoint = tangentPoint -2;
        }
        if (tangentPoint >= 0){
            this.currentTangentPoint = tangentPoint + 2;
        }

        this.fx = Function.f(tangentPoint); // Calculate once
        this.fxPrime = Function.fPrime(tangentPoint); // Calculate once
        
        animationTimer.start();
        repaint();
    }

    public void zoomIn() {
        scale *= 1.2; // Increase the scale factor
        repaint();
    }

    public void zoomOut() {
        scale /= 1.2; // Decrease the scale factor
        repaint();
    }
}
