package it.univaq.au4h.controllers;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class LabelController {
	
	private JLabel label1=new JLabel("");
	private JLabel label2=new JLabel("");
	private JLabel label3=new JLabel("");
	private JLabel label4=new JLabel("");
	
	private JFrame frame;
	
	public LabelController(JFrame frame) {
		this.frame=frame;
		label1.setText("RHUP: ");
		label1.setLayout(null);
		label1.setLocation(300, 300);
		label1.setVisible(true);
		this.frame.add(label1);
		label2.setText("LARMUP: ");
		this.frame.add(label2);
		label2.setLocation(5, 10);
		label3.setText("OPENG LEGS: ");
		this.frame.add(label3);
		label3.setLocation(5, 20);
		label4.setText("NECK TO CAMERA: ");
		this.frame.add(label4);
		label4.setLocation(5, 30);
		this.frame.revalidate();
	}	
}
