package edu.umn.d.windows;

import com.sun.j3d.utils.image.TextureLoader;
import edu.umn.d.geometry.ElevationModel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.media.j3d.Texture2D;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author nels2426
 */
public class TextureButtonActionListener implements ActionListener{
    private ElevationModel model;
    public TextureButtonActionListener(ElevationModel model){
        this.model = model;
    }
    public void actionPerformed(ActionEvent e){
        JRadioButton source = (JRadioButton)e.getSource();
        if(source.getText().equals("None")){
            model.setTexture(new Texture2D());
        } else{
                String fileName = source.getActionCommand();
                try{
                    TextureLoader texget = new TextureLoader(new java.net.URL(fileName),null);
                    Texture2D tex = (Texture2D) texget.getTexture();
                    //model.setColor(Color.WHITE);
                    model.setTexture(tex);
                } catch(java.net.MalformedURLException exception){
                    System.err.println("TextureButtonActionListener: error loading textures");
                    exception.printStackTrace();
                }
            }
        }
    };