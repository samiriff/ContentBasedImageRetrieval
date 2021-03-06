package gui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import retriever.ImageCollection;
import retriever.ImageData;
import retriever.UserVariables;
import acm.graphics.GImage;
import acm.program.GraphicsProgram;
import acmx.export.javax.swing.JFileChooser;

public class MainGraphics extends GraphicsProgram
{		
	public static final int APPLICATION_WIDTH = 1920;
	public static final int APPLICATION_HEIGHT = 1080;
	
	private Button openButton;
	private JFileChooser fileChooser;
	
	private Button runButton;
	
	private JSlider sliders[] = new JSlider[3];
	private JTextField sliderLabels[] = new JTextField[3];
	
	private JCheckBox checkbox;
	
	private ImageData queryImage;
	private ImageCollection imageCollection = null;
	
	public void init()
	{
		openButton = new Button("Open");
		fileChooser = new JFileChooser();
		runButton = new Button("Retrieve Similar Images");
		checkbox = new JCheckBox("CSFD");
		
		for(int i=0; i<3; i++)
		{
			sliders[i] = new JSlider(0, 60);
			sliders[i].setValue(16);
			sliderLabels[i] = new JTextField("" + sliders[i].getValue());		
			sliderLabels[i].setEditable(false);
		}
		
		add(openButton, NORTH);
		
		add(runButton, NORTH);
		
		initSliders();		
		
		UserVariables.setNumOfSimilarImages(3);
		
		initCheckbox();
		add(checkbox, NORTH);
		
		addActionListeners();		
	}

	private void initCheckbox()
	{
		checkbox.addChangeListener(new ChangeListener()
		{			
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				UserVariables.setCSFD(checkbox.isSelected());
			}
		});
	}

	private void initSliders()
	{
		for(int i=0; i<3; i++)
		{
			add(sliders[i], NORTH);
			add(sliderLabels[i], NORTH);
		}
		
		sliders[0].addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) 
            {
            	sliderLabels[0].setText(String.valueOf(sliders[0].getValue()));	
            	UserVariables.setRedBin(sliders[0].getValue());
            }
        });
		sliders[1].addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) 
            {
            	sliderLabels[1].setText(String.valueOf(sliders[1].getValue()));	    
            	UserVariables.setGreenBin(sliders[1].getValue());
            }
        });
		sliders[2].addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) 
            {
            	sliderLabels[2].setText(String.valueOf(sliders[2].getValue()));	 
            	UserVariables.setBlueBin(sliders[2].getValue());
            }
        });
	}
	
	public void run()
	{			
		System.out.println(Runtime.getRuntime().maxMemory());
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == openButton) 
	    {
			removeAll();
			
	        int returnVal = fileChooser.showOpenDialog(this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) 
	        {
	            File file = fileChooser.getSelectedFile();
	            System.out.println(file);
	            System.out.println("Opening: " + file.getAbsolutePath());
	            queryImage = new ImageData(file.getAbsolutePath(), true);          
	            
	            GImage img = new GImage(queryImage.getImage().getImage());
	            img.scale(0.5);
	            add(img, (int)(getWidth() / 2 - queryImage.getImage().getWidth() / 2), 0);
	            
	            img = queryImage.getQuantizedImage();
	            img.scale(0.5);
	            add(img, (int)(getWidth() / 2 + queryImage.getImage().getWidth() / 2), 0);
	        } 
	        else 
	        {
	            System.out.println("Open command cancelled by user.");
	        }
	   } 
	   
	   if(e.getSource() == runButton)
	   {			    
		   if(imageCollection == null || UserVariables.hasChanged())
		   {
			   imageCollection = new ImageCollection("images");		
			   UserVariables.resetChanged();
		   }
		   
		   ArrayList<ImageData> targets;
		   if(UserVariables.useCSFD() == true)
			   targets = imageCollection.computeNearestCFSDTarget(queryImage);
		   else
			   targets = imageCollection.computeNearestTarget(queryImage);			
		   System.out.println("TARGETS");			
		   displayImages(targets, UserVariables.getNumOfSimilarImages());
		   
	   }	   
	}
	
	private void displayImages(ArrayList<ImageData> targets, int numRequired)
	{
		int x = 350, y = (int)queryImage.getImage().getHeight() / 2 + 10;
		
		int imgNum = 0;
		for(ImageData target : targets)
		{
			if(imgNum >= numRequired)
				break;
			GImage image = new GImage(target.getImage().getImage());
			image.scale(0.45, 0.45);
			add(image, x, y);
			
			GImage quantizedImage = target.getQuantizedImage();
			quantizedImage.scale(0.45);
			add(quantizedImage, x, y + image.getHeight() + 10);
			
			x += image.getWidth() + 10;
			if(x > getWidth())
			{
				x = 0;
				y += image.getHeight();
			}
			
			imgNum++;
		}
	}
} 