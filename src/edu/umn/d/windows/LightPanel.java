package edu.umn.d.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import javax.vecmath.Vector3f;

/**
 * Small part to let the user select the direction of the lighting.  The light direction comes from the center out to the end point.  So a
 * line that looks like \ will make the light look like it is coming from the south-east, or lower right hand corner.
 * @author nels2426
 */
public class LightPanel extends JPanel implements MouseInputListener{
    
    private static final int HEIGHT = 200;
    private static final int WIDTH = 200;
    private Point2D endPoint = new Point2D.Float(0, 0);
    private Point2D startPoint = new Point2D.Float(WIDTH/2, HEIGHT/2);
    private View3DPanel viewWindow;
    
    /** Creates a new instance of LightPanel */
    public LightPanel() {
        add(new JLabel("Light direction"));
        setBackground(Color.white);
        setForeground(Color.black);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        repaint();
    }
    
    /**
     * Sets the end point of the line.
     */
    public void mouseClicked(MouseEvent e){
        endPoint = e.getPoint();
        Vector3f direction = new Vector3f((float)((endPoint.getX() - startPoint.getX()) / -startPoint.getX()), -1, (float)((endPoint.getY() - startPoint.getY()) / -startPoint.getY()));
        viewWindow.setLightDirection(direction);
        repaint();
    }
    
    /**
     * Not used.
     */
    public void mouseExited(MouseEvent e){}
    
    /**
     * If the panel has changed size since the last use, this resets the center of canvas.
     */
    public void mouseEntered(MouseEvent e){
        Component component = e.getComponent();
        Rectangle canvasBounds = component.getBounds();
        startPoint.setLocation(canvasBounds.width/2, canvasBounds.height/2);
        repaint();
    }
    
    /** Not used*/
    public void mouseReleased(MouseEvent e){}
    /** Not used */
    public void mousePressed(MouseEvent e){}
    /** Not used */
    public void mouseMoved(MouseEvent e){}
    /** Not used */
    public void mouseDragged(MouseEvent e){}
    
    /**
     * Draws the line from the center to the point that the user clicked.
     * Should not be called by application, but called as part of repaint().
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawLine((int)startPoint.getX(), (int)startPoint.getY(), (int)endPoint.getX(), (int)endPoint.getY());
    }
    
    /**
     * Sets the View3DPanel that this LightPanel will control.
     */
    public void setViewWindow(View3DPanel vp){
        viewWindow = vp;
    }
}
