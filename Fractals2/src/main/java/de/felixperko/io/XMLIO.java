package de.felixperko.io;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.main.api.sax.EXIResult;
import com.siemens.ct.exi.main.api.sax.EXISource;
 
public class XMLIO {
 
    static final String xmlFilePath = "C:\\Users\\Felix\\testfile.xml";
	static String fileEXI = xmlFilePath+".exi"; // EXI output
    
	static EXIFactory exiFactory = DefaultEXIFactory.newInstance();

    static DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

	static {
		//Not worth it for most param data, just use exi
//		exiFactory.setCodingMode(CodingMode.COMPRESSION);
	}
	
    public static void main(String argv[]) {
    	
    	boolean base64 = true;
		boolean exi = true;
    	Document document = createDocument();
        setTestParamConfigData(document);
    	
    	String path = exi ? fileEXI : xmlFilePath;
    	encodeToFile(document, new File(path), exi);
    	if (exi) {
    		decodeExiToXMLFile();
    	}
    	
    	byte[] data = encodeToByteArray(document, exi, base64);
    	
    	System.out.println("result (exi="+exi+" base64="+base64+"):");
    	System.out.println();
    	System.out.println(new String(data));
    	System.out.println();
    	
    	if (base64) {
    		data = Base64.getUrlDecoder().decode(data);
    	}
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
    	System.out.println(decodeToString(bais, exi));
    }
    
    public static byte[] encodeToByteArray(Document document, boolean exi, boolean base64) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	encode(document, baos, exi);
		byte[] data = baos.toByteArray();
		if (base64) {
	    	return Base64.getUrlEncoder().withoutPadding().encode(data);
		}
		else {
			return data;
		}
    }
    
    public static void encodeToFile(Document document, File file, boolean exi) {
		try {
	    	OutputStream osEXI = new FileOutputStream(file);
	    	encode(document, osEXI, exi);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    public static Document createDocument() {
		try {
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
	        Document document = documentBuilder.newDocument();
	        return document;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
    }

	protected static void encode(Document document, OutputStream outputStream, boolean exi) {
		try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            
            OutputStream uncompressedStream;
            if (exi) {
            	uncompressedStream = new ByteArrayOutputStream();
            } else {
            	uncompressedStream = outputStream;
            }
            StreamResult streamResult = new StreamResult(uncompressedStream);
            
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);
        	
        	if (exi) {
        		byte[] uncompressedData = ((ByteArrayOutputStream)uncompressedStream).toByteArray();

	        	EXIResult exiResult = new EXIResult(exiFactory);
	        	exiResult.setOutputStream(outputStream);
	        	
	        	XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	        	xmlReader.setContentHandler( exiResult.getHandler() );
				InputSource inputSource = new InputSource(new ByteArrayInputStream(uncompressedData));
	        	xmlReader.parse(inputSource);

        	}
        	outputStream.close();
 
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (EXIException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
    
    public static void decodeExiToXMLFile() {
    	try {
	    	Result result = new StreamResult(xmlFilePath);
	    	InputSource is = new InputSource(fileEXI);
	    	SAXSource exiSource = new EXISource(exiFactory);
	    	exiSource.setInputSource(is);
	    	TransformerFactory tf = TransformerFactory.newInstance();
	    	Transformer transformer = tf.newTransformer();
//            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	transformer.transform(exiSource, result);
    	} catch (TransformerException e) {
    		e.printStackTrace();
    	} catch (EXIException e) {
			e.printStackTrace();
		}
    }
    
    public static String decodeToString(InputStream inputStream, boolean exi) {
    	try {
    		if (!exi) {
    			BufferedInputStream bis = new BufferedInputStream(inputStream);
    			ByteArrayOutputStream buf = new ByteArrayOutputStream();
    			for (int result = bis.read(); result != -1; result = bis.read()) {
    			    buf.write((byte) result);
    			}
    			return buf.toString(StandardCharsets.UTF_8.name());
    		}
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	Result result = new StreamResult(baos);
	    	InputSource is = new InputSource(inputStream);
	    	SAXSource exiSource = new EXISource(exiFactory);
	    	exiSource.setInputSource(is);
	    	TransformerFactory tf = TransformerFactory.newInstance();
	    	Transformer transformer = tf.newTransformer();
//            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	transformer.transform(exiSource, result);
	    	return baos.toString();
	    	
    	} catch (TransformerException e) {
    		e.printStackTrace();
    	} catch (EXIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    protected static void setTestParamConfigData(Document document) {
    	Element config = document.createElementNS(null, "paramconfig");
    	config.setAttribute("def", "IXnjFL");
    	config.setAttribute("v", "1.0");
    	document.appendChild(config);
    	
    	Element param2 = document.createElementNS(null, "p");
    	param2.setAttribute("id", "YoC1cX");
    	param2.appendChild(document.createTextNode("tanhi(z)^3 - z^2 + c"));
    	config.appendChild(param2);
    	
    	Element param0 = document.createElementNS(null, "p");
    	param0.setAttribute("id", "utffSy");
    	param0.appendChild(document.createTextNode("9.1552734375E-5"));
    	config.appendChild(param0);
    	
    	Element param1 = document.createElementNS(null, "p");
    	param1.setAttribute("id", "ZZ3ttS");
    	param1.appendChild(document.createTextNode("-0.19406917889912853,-0.6481941968202588"));
    	config.appendChild(param1);
    }
}