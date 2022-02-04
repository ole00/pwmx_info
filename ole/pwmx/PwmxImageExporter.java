package ole.pwmx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class PwmxImageExporter {
	private static BufferedImage img;
	
	public static void exportPreview(Pwmx p) {
		BufferedImage preview = new BufferedImage(p.previewW, p.previewH, BufferedImage.TYPE_INT_ARGB);
		
		int max = p.previewW * p.previewH;
		int i = 0;
		int offset = p.previewImageOffset;
		final byte[] data = p.rawData;
		final int w = p.previewW;
		
		// convert for BGR565 to ARG8B888
		while (i < max) {
			int pixel = (data[offset++] & 0xFF);
			pixel |= (data[offset++] & 0xFF)  << 8;
			//BGR565
			int b = pixel & 0xF800;
			int g = pixel & 0x7E0;
			int r = pixel & 0x1F;
						
			int x = i % w;
			int y = i / w;
			i++;
			preview.setRGB(x, y, 0xFF000000 | (r << 19) | (g << 5) | (b >> 8));		
		}
		//write image to png
		try {
			ImageIO.write((RenderedImage) preview, "png", new File("preview.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportLayer(Pwmx p, int layerIndex) {
		if (layerIndex < 1 || layerIndex > p.layers.length) {
			throw new RuntimeException("layer index out of bound: total layers=" + p.layers.length);
		}
		byte[] pix = decodePW0(p, layerIndex - 1);
		
		savePixels(pix, "layer_" + formatNumber(layerIndex) + ".png", p.resX, p.resY);
	}
	
	private static String formatNumber(int val) {
		String result = Integer.toString(val);
		return "00000".substring(result.length()).concat(result);
	}
	
	// saves decoded bitmap to png
	private static void savePixels(byte[] pixels, String path, int w, int h) {
		if (img == null) {
			img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		}
		
		// fill the image with black color
		Graphics g = img.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0,0, w,h);
		
		int i = 0;
		final int max = pixels.length;
		while (i < max) {
			int x = i % w;
			int y = i / w;
			int rgb = pixels[i++] & 0xFF;
			
			// render non-black pixels only
			if (rgb > 0) {
				img.setRGB(x, y, 0xFF000000 | (rgb << 16) | (rgb << 8) | rgb);
			}
		}

		//write image to png
		try {
			ImageIO.write((RenderedImage) img, "png", new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// credits: UVTools
	// https://github.com/sn4k3/UVtools/blob/master/UVtools.Core/FileFormats/PhotonWorkshopFile.cs
	// method: Decode PW0
	
	//decodes raw rle encoded data into a bitmap
	// note: .pwmx files do not seem to have a crc16 appended at the end of each layer iamge data
	public static byte[] decodePW0(Pwmx p, int layerIndex) {
		int start = p.layers[layerIndex].imgOffset;
		int size = p.layers[layerIndex].imgSize;
		
		int w = p.resX;
		int h = p.resY;
		int total = w * h;
		byte[] pix = new byte[total];
		int rleMax = start + size;
		
		int pixelPos = 0;
		
		for (int i = start; i < rleMax; i++) {
			int b = p.rawData[i] & 0xFF;
			int code = b >> 4;
			int repeat = b & 0xf;
			int color;
			
			switch (code) {
			case 0:
			case 0xF:
				color = (code == 0) ? 0 : 255;
				i++;
				if (i >= rleMax  - 1) {
					repeat = total - pixelPos;
					break;
				}
				repeat = (repeat << 8) | (p.rawData[i] & 0xFF);
				break;
			default: //antialiasing, up to 15 pixels
				color = ((code << 4) | code);
				if (i >= rleMax - 1) {
					repeat = total - pixelPos;
				}
				break;
			} // end of switch
			
			pixelPos = fillSpan(pix, pixelPos, repeat, color);
			
			if (pixelPos == total) {
				break;
			}
			if (pixelPos > total) {
				pix = null;
				throw new RuntimeException("image pixel overflow");
				
			}
		}
		if (pixelPos > 0 && pixelPos != total)
        {
            pix = null;
            throw new RuntimeException("image pixel underflow: pos=" + pixelPos + " total=" + total);
        }
		return pix;
	}

	
	private static int fillSpan(byte[]pix, int pixelPos, int repeat, int color) {
		if (color > 0) {
			byte b = (byte) (color);
			int i = pixelPos;
			final int max = i + repeat;
			while (i < max) {
				pix[i++] = b;
			}
		}
		return pixelPos + repeat;
	}
}
