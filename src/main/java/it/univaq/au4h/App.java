package it.univaq.au4h;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import org.openni.GeneralException;
import org.openni.StatusException;

import it.univaq.au4h.controllers.MainController;

public class App 
{
	private static MainController controller;
	private static boolean isRunning=true;

	public static void main( String[] args )
	{

		try {
			controller = new MainController();
			JFrame frame = new JFrame("OpenNI User Tracker");

			frame.addKeyListener(new KeyListener(){
				public void keyPressed(KeyEvent arg0) {
					if(arg0.getKeyCode()==KeyEvent.VK_ESCAPE)
						isRunning=false;
				}

				public void keyReleased(KeyEvent arg0) {
					// TODO Auto-generated method stub

				}

				public void keyTyped(KeyEvent arg0) {
					// TODO Auto-generated method stub

				}

			});
			frame.add("Center", controller.getComponent());


			frame.pack();
			frame.setVisible(true);
			controller.run(isRunning);
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
