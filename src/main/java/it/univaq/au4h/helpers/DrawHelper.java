package it.univaq.au4h.helpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ShortBuffer;

import org.openni.StatusException;

@SuppressWarnings("serial")
public class DrawHelper extends Component{
	
	Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};

	private byte[] imgbytes;
	private float histogram[];


	private boolean drawBackground = true;
	private boolean drawPixels = true;
	private boolean drawSkeleton = true;

	private BufferedImage bimg;
	int width, height;
	
	private NIHelper niHelper;
	
	public DrawHelper(NIHelper niHelper) {
		
		this.niHelper=niHelper;
		
		histogram = new float[10000];

		width = niHelper.getWidth();
		height = niHelper.getHeigth();

		imgbytes = new byte[width*height*3];
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	public void updateDepth() throws StatusException {

		niHelper.waitUpdates();

		ShortBuffer scene = niHelper.getSceneData();
		ShortBuffer depth = niHelper.getDepthData();

		calcHist(depth);
		depth.rewind();

		while(depth.remaining() > 0) {
			int pos = depth.position();
			short pixel = depth.get();
			short user = scene.get();

			imgbytes[3*pos] = 0;
			imgbytes[3*pos+1] = 0;
			imgbytes[3*pos+2] = 0;                	

			if (drawBackground || pixel != 0) {
				int colorID = user % (colors.length-1);
				if (user == 0){
					colorID = colors.length-1;
				}
				if (pixel != 0) {
					float histValue = histogram[pixel];
					imgbytes[3*pos] = (byte)(histValue*colors[colorID].getRed());
					imgbytes[3*pos+1] = (byte)(histValue*colors[colorID].getGreen());
					imgbytes[3*pos+2] = (byte)(histValue*colors[colorID].getBlue());
				}
			}
		}
	} 



	private void calcHist(ShortBuffer depth)
	{
		for (int i = 0; i < histogram.length; ++i)
			histogram[i] = 0;

		depth.rewind();

		int points = 0;
		while(depth.remaining() > 0) {
			short depthVal = depth.get();
			if (depthVal != 0) {
				histogram[depthVal]++;
				points++;
			}
		}

		for (int i = 1; i < histogram.length; i++) {
			histogram[i] += histogram[i-1];
		}

		if (points > 0) {
			for (int i = 1; i < histogram.length; i++) {
				histogram[i] = 1.0f - (histogram[i] / (float)points);
			}
		}
	}

	public void paint(Graphics g)
	{
		if (drawPixels)
		{
			DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);

			WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null); 

			ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

			bimg = new BufferedImage(colorModel, raster, false, null);

			g.drawImage(bimg, 0, 0, null);
		}

		int[] users;
		try {
			users = niHelper.getUsers();
			for (int i = 0; i < users.length; ++i)
			{
				Color c = colors[users[i]%colors.length];
				c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());

				g.setColor(c);
				if (drawSkeleton && niHelper.isSkeletonTracking(users[i]))	
				{
					niHelper.drawSkeleton(g, users[i]);
				}

			}
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}
	

}
