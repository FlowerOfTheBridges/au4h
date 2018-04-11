package it.univaq.au4h;

import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import org.openni.GeneralException;
import org.openni.StatusException;

import it.univaq.au4h.controllers.MainController;

public class App 
{
	private static MainController controller;

	public static void main( String[] args )
	{

		try {
			controller = new MainController();
			JFrame frame = new JFrame("OpenNI User Tracker");
			frame.add("Center", controller.getComponent());
			frame.pack();
			frame.setVisible(true);
			controller.run();
		} 
		catch (SocketException e) {
			e.printStackTrace();
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (GeneralException e) {
			e.printStackTrace();
		}
	}
}
