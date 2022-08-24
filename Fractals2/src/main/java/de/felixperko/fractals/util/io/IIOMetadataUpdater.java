package de.felixperko.fractals.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.NodeList;

//Source: https://stackoverflow.com/a/41282314
public class IIOMetadataUpdater {

    public static void main(final String[] args) throws IOException {
        File in = new File(args[0]);
        File out = new File(in.getParent(), createOutputName(in));
        String key = "foo";
        String value = "bar";

        System.out.println("Output path: " + out.getAbsolutePath());

        copyFileWithMetadata(in, out, key, value);

        String readValue = readMetadata(out, key);

        System.out.println("value: " + readValue);
    }

	public static String readMetadata(File file, String key) throws IOException {
		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next(); // TODO: Validate that there are readers

            reader.setInput(input);
            return getTextEntry(reader.getImageMetadata(0), key);
        }
		
	}

	public static void copyFileWithMetadata(File originalFile, File outputFile, String key, String value)
			throws IOException, IIOInvalidTreeException {
		try (ImageInputStream input = ImageIO.createImageInputStream(originalFile);
             ImageOutputStream output = ImageIO.createImageOutputStream(outputFile)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next(); // TODO: Validate that there are readers

            reader.setInput(input);
            IIOImage image = reader.readAll(0, null);

            addTextEntry(image.getMetadata(), key, value);

            ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
            writer.setOutput(output);
            writer.write(image);
        }
	}

	public static void writeFileWithMetadata(InputStream inputStream, File outputFile, String key, String value)
			throws IOException, IIOInvalidTreeException {
		try (ImageInputStream input = ImageIO.createImageInputStream(inputStream);
             ImageOutputStream output = ImageIO.createImageOutputStream(outputFile)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next(); // TODO: Validate that there are readers

            reader.setInput(input);
            IIOImage image = reader.readAll(0, null);

            addTextEntry(image.getMetadata(), key, value);

            ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
            writer.setOutput(output);
            writer.write(image);
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
