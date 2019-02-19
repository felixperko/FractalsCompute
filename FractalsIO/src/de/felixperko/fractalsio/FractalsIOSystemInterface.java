package de.felixperko.fractalsio;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.network.ClientSystemInterface;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public class FractalsIOSystemInterface implements ClientSystemInterface {
	
	BufferedImage image = null;
	int imgWidth;
	int imgHeight;
	
	int chunkCount = 0;
	
	Map<String, ParamSupplier> parameters = new HashMap<>();
	
	public void setParameters(Map<String, ParamSupplier> parameters) {
		int newImgWidth = (int)parameters.get("width").get(0, 0);
		int newImgHeight = (int)parameters.get("height").get(0, 0);
		
		if (image == null || newImgWidth != imgWidth || newImgHeight != imgHeight) {
			imgWidth = newImgWidth;
			imgHeight = newImgHeight;
			image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		}
	}

	@Override
	public void chunkUpdated(Chunk chunk) {
		int chunkSize = chunk.getChunkSize();
		long cx = chunkSize*chunk.getChunkX();
		long cy = chunkSize*chunk.getChunkY();
		for (int i = 0 ; i < chunk.getArrayLength() ; i++) {
			int x = (int) (i / chunkSize + cx);
			int y = (int) (i % chunkSize + cy);
			double value = chunk.getValue(i);
			if (value > 0) {
				float hue = (float)Math.log(Math.log(value)+1);
				int color = Color.HSBtoRGB(hue, 0.4f, 0.6f);
				image.setRGB(x, y, color);
			}
		}
		chunkCount--;
		if (chunkCount == 0) {
			try {
//				long endTime = System.nanoTime();
//				System.out.println("calculated in "+NumberUtil.getElapsedTimeInS(startTime, 2)+"s.");
				ImageIO.write(image, "png", new File("test.png"));
				System.out.println("done!");
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addChunkCount(int count) {
		chunkCount += count;
	}

	@Override
	public void chunksCleared() {
		image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
	}

}
