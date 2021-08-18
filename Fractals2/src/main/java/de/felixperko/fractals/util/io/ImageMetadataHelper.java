package de.felixperko.fractals.util.io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.imageio.plugins.png.PNGMetadata;

//Source:   https://stackoverflow.com/a/8735707
//			https://stackoverflow.com/a/41282314
public class ImageMetadataHelper {
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
//		try {
//			copyFileWithMetadata("test1.png", new HashMap<String, String>(){{put("Description", "parameters will go here...");}});
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	
	public static byte[] writeCustomData(BufferedImage buffImg, String key, String value) throws Exception {
	    ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

	    ImageWriteParam writeParam = writer.getDefaultWriteParam();
	    ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

	    //adding metadata
	    IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

	    IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
	    textEntry.setAttribute("keyword", key);
	    textEntry.setAttribute("value", value);

	    IIOMetadataNode text = new IIOMetadataNode("tEXt");
	    text.appendChild(textEntry);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
	    root.appendChild(text);

	    metadata.mergeTree("javax_imageio_png_1.0", root);

	    //writing the data
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageOutputStream stream = ImageIO.createImageOutputStream(baos);
	    writer.setOutput(stream);
	    writer.write(metadata, new IIOImage(buffImg, null, metadata), writeParam);
	    stream.close();

	    return baos.toByteArray();
	}
	
	public static String readCustomData(byte[] imageData, String key) throws IOException{
	    ImageReader imageReader = ImageIO.getImageReadersByFormatName("png").next();

	    imageReader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(imageData)), true);

	    // read metadata of first image
	    IIOMetadata metadata = imageReader.getImageMetadata(0);

	    //this cast helps getting the contents
	    
	    
	    
//TODO disabled Image metadata!
	    
	    
//	    PNGMetadata pngmeta = (PNGMetadata) metadata; 
//	    NodeList childNodes = pngmeta.getStandardTextNode().getChildNodes();
//
//	    for (int i = 0; i < childNodes.getLength(); i++) {
//	        Node node = childNodes.item(i);
//	        String keyword = node.getAttributes().getNamedItem("keyword").getNodeValue();
//	        String value = node.getAttributes().getNamedItem("value").getNodeValue();
//	        if(key.equals(keyword)){
//	            return value;
//	        }
//	    }
	    return null;
	}

	    public static void copyFileWithMetadata(String inputFile, Map<String, String> newMetadata) throws IOException {
	        File in = new File(inputFile);
	        File out = new File(in.getParent(), createOutputName(in));

	        System.out.println("Output path: " + out.getAbsolutePath());

	        try (ImageInputStream input = ImageIO.createImageInputStream(in);
	             ImageOutputStream output = ImageIO.createImageOutputStream(out)) {

	            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
	            ImageReader reader = readers.next(); // TODO: Validate that there are readers

	            reader.setInput(input);
	            IIOImage image = reader.readAll(0, null);
	            
	            newMetadata.forEach((k,v) -> {
					try {
						addTextEntry(image.getMetadata(), k, v);
					} catch (IIOInvalidTreeException e) {
						throw new IllegalStateException();
					}
				});
//	            addTextEntry(image.getMetadata(), "Description", "description");
	            
	            ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
	            writer.setOutput(output);
	            writer.write(image);
	        }

	        try (ImageInputStream input = ImageIO.createImageInputStream(out)) {
	            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
	            ImageReader reader = readers.next(); // TODO: Validate that there are readers

	            reader.setInput(input);
	            String value = getTextEntry(reader.getImageMetadata(0), "foo");

	            System.out.println("value: " + value);
	        }
	    }

	    private static String createOutputName(final File file) {
	        String name = file.getName();
	        int dotIndex = name.lastIndexOf('.');

	        String baseName = name.substring(0, dotIndex);
	        String extension = name.substring(dotIndex);

	        return baseName + "_copy" + extension;
	    }

	    private static void addTextEntry(final IIOMetadata metadata, final String key, final String value) throws IIOInvalidTreeException {
	        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
	        textEntry.setAttribute("keyword", key);
	        textEntry.setAttribute("value", value);

	        IIOMetadataNode text = new IIOMetadataNode("Text");
	        text.appendChild(textEntry);

	        IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
	        root.appendChild(text);

	        metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
	    }

	    private static String getTextEntry(final IIOMetadata metadata, final String key) {
	        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
	        NodeList entries = root.getElementsByTagName("TextEntry");

	        for (int i = 0; i < entries.getLength(); i++) {
	            IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
	            if (node.getAttribute("keyword").equals(key)) {
	                return node.getAttribute("value");
	            }
	        }

	        return null;
	    }
}
