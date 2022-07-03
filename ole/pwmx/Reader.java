package ole.pwmx;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class Reader {
	
	static final byte[] TAG_INTRO   = "ANYCUBIC\0\0\0\0".getBytes();
	static final byte[] TAG_HEADER  = "HEADER\0\0\0\0\0\0".getBytes();
	static final byte[] TAG_PREVIEW = "PREVIEW\0\0\0\0\0".getBytes();
	static final byte[] TAG_LAYERS  = "LAYERDEF\0\0\0\0".getBytes();
	static final byte[] TAG_EXTRA   = "EXTRA\0\0\0\0\0\0\0".getBytes();
	static final byte[] TAG_MACHINE = "MACHINE\0\0\0\0\0".getBytes();
	
	private static int parseIndex;
	private static byte[] ptmp = new byte[4];

	// main entry point for reading a .pwmx file : pass the filename to
	// get the Pwmx class instance in return.
	public static Pwmx read(String fname) {
		try {
			//open the file
			File f = new File(fname);
			int len = (int) f.length();
			
			//create file contents buffer
			byte[] data = new byte[len];
			
			//read the file into the buffer
			DataInputStream din = new DataInputStream(new FileInputStream(f));
			din.readFully(data);
			din.close();
			
			//parse the buffer / file contents
			return parse(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Pwmx parse(byte[] data) {
		// create the Pwmx object
		Pwmx p = new Pwmx();
		// store the raw data (file buffer) in the pwmx object for layer decoding purposes
		p.rawData = data;
		
		//start decoding the file: contains 5 parts stored sequentially
		// 1.st part - Intro
		parseIntro(p);
		// 2. header
		parseHeader(p);
		// 3. preview image (displayed on the printer's LCD)
		parsePreview(p);
		// 4. layer list and layer parameters
		parseLayers(p);
		
		if (p.version == 516) {
			parseExtra(p);
			parseMachine(p);
		}
		
		// 5. layer images - rle encoded. They are decoded on demand (see PwmxImageExport.java)
		return p;
	}
	
	private static void parseIntro(Pwmx p) {
		byte[] data = p.rawData;
		// check the first 12 bytes contain the intro tag
		if (!compareTag(data, 0, TAG_INTRO)) {
			throw new RuntimeException("intro tag parse error");
		}
		//initial seek value
		parseIndex = 12;
		
		//reading starts here, each read seeks +4 bytes
		p.version = parseInt(data);  //12
		if (p.version != 1 && p.version != 515 && p.version != 516) {
			throw new RuntimeException("intro version error: " + p.version);
		}
		p.areaNum = parseInt(data); //16
		p.headerDataOffset = parseInt(data); //20
		p.intro24 = parseFloat(data); //24 - unknown
		p.previewDataOffset = parseInt(data); //28
		p.intro32 = parseInt(data); //32 - unknown ? Preview Unknown data on v516?
		p.layerDataOffset = parseInt(data); //36
		
		// Photon Mono 4k - .pwma
		if (p.version == 516) {
			p.extraDataOffset = parseInt(data);
			p.machineDataOffset = parseInt(data);
		} else {
			p.intro40 = parseFloat(data); //40 - unknown
		}
		p.imageDataOffset = parseInt(data); // start offset of the layer data images
	}

	private static void parseHeader(Pwmx p) {
		byte[] data = p.rawData;
		
		//set initial seek offset for reading
		parseIndex = p.headerDataOffset;
		
		// check the first 12 bytes contain the header tag
		if (!compareTag(data, parseIndex, TAG_HEADER)) {
			throw new RuntimeException("header tag parse error");
		}
		//skip past the tag
		parseIndex += 12;
		
		//reading starts here, each read seeks +4 bytes
		p.headerPayloadSize = parseInt(data);
		p.pixelSizeUm = parseFloat(data);
		p.layerHeight = parseFloat(data); // in mm
		p.exposureTime = parseFloat(data); //in sec.
		p.delayBeforeExposure = parseFloat(data);
		p.bottomExposureTime = parseFloat(data);
		p.bottomLayerCount = parseFloat(data);
		p.liftDistance = parseFloat(data);
		p.liftSpeed = parseFloat(data);
		p.retractSpeed = parseFloat(data);
		p.volumeMl = parseFloat(data);
		p.antiAliasing = parseInt(data);
		p.resX = parseInt(data);
		p.resY = parseInt(data);
		p.weightG = parseFloat(data);
		p.price = parseFloat(data);
		p.priceCurrency = parseInt(data);
		p.perLayerOverride = parseInt(data);
		p.printTime = parseInt(data);
		p.transitionLayerCount = parseInt(data);
		p.padding2 = parseInt(data);
		if (p.version == 516) {
			p.padding3 = parseInt(data);
		}
	}
	
	private static void parsePreview(Pwmx p) {
		byte[] data = p.rawData;
		// set initial seek offset
		parseIndex = p.previewDataOffset;
		// check the first 12 bytes contain the preview tag
		if (!compareTag(data, parseIndex, TAG_PREVIEW)) {
			throw new RuntimeException("preview tag parse error. parseIndex= 0x" + Integer.toHexString(parseIndex) );
		}
		// skip past the tag
		parseIndex += 12;
		
		//reading starts here, each read seeks +4 bytes
		p.previewPayloadSize = parseInt(data);
		p.previewW = parseInt(data);
		p.previewDpi = parseInt(data);
		p.previewH = parseInt(data);
		p.previewImageOffset = parseIndex; // offset of the preview image within the file
		
		if (p.version == 516) {
			parseIndex = p.intro32;
			for (int i = 0; i < 7; i++) {
				p.previewUnknown[i] = parseInt(data);;
			}
		}
	}
	
	private static void parseLayers(Pwmx p) {
		byte[] data = p.rawData;
		parseIndex = p.layerDataOffset;
		// check the first 12 bytes contain the layer tag
		if (!compareTag(data, parseIndex, TAG_LAYERS)) {
			throw new RuntimeException("layerdef tag parse error");
		}
		parseIndex += 12;
		int payloadSize = parseInt(data);
		p.layerCount = parseInt(data);

		p.layers = new PwmxLayer[p.layerCount];
		for (int i = 0; i < p.layerCount; i++) {
			parseLayer(p, i);
		}
		
		if (p.version != 516) {
			// sanity check: check the layer image data follow the layer definition
			if (parseIndex != p.imageDataOffset) {
				throw new RuntimeException("layerdef size/parse error");		
			}
		}
	}
	
	private static void parseLayer(Pwmx p, int index) {
		byte[] data = p.rawData;
		PwmxLayer l = new PwmxLayer();
		p.layers[index] = l;
		
		//reading starts here
		l.imgOffset = parseInt(data); //absolute offset of the rle encoded image in the file 
		l.imgSize = parseInt(data); //size of the rle encoded image
		l.liftDistance = parseFloat(data); // in mm
		l.liftSpeed = parseFloat(data); //in sec
		l.exposureTime = parseFloat(data); // n sec
		l.height = parseFloat(data); //in mm
		l.layer44 = parseFloat(data); // unknown, usually 0
		l.layer48 = parseFloat(data); // unknown, usually 0
	}
	
	private static void parseExtra(Pwmx p) {	
		byte[] data = p.rawData;
		
		//set initial seek offset for reading
		parseIndex = p.extraDataOffset;
		
		// check the first 12 bytes contain the header tag
		if (!compareTag(data, parseIndex, TAG_EXTRA)) {
			throw new RuntimeException("Extra tag parse error");
		}
		//skip past the tag
		parseIndex += 12;
		
		//reading starts here, each read seeks +4 bytes
		p.extra0 = parseInt(data);
		p.extra4 = parseInt(data);
		p.extraLiftDistance1 = parseFloat(data);
		p.extraLiftSpeed1 = parseFloat(data);
		p.extraRetractSpeed1 = parseFloat(data);
		p.extraLiftDistance2 = parseFloat(data);
		p.extraLiftSpeed2 = parseFloat(data);
		p.extraRetractSpeed2 = parseFloat(data);

		p.extra32 = parseInt(data);
		p.extraLiftDistance3 = parseFloat(data);
		p.extraLiftSpeed3 = parseFloat(data);
		p.extraRetractSpeed3 = parseFloat(data);
		p.extraLiftDistance4 = parseFloat(data);
		p.extraLiftSpeed4 = parseFloat(data);
		p.extraRetractSpeed4 = parseFloat(data);
	}
	
	private static void parseMachine(Pwmx p) {	
		byte[] data = p.rawData;
		
		//set initial seek offset for reading
		parseIndex = p.machineDataOffset;
		
		// check the first 12 bytes contain the header tag
		if (!compareTag(data, parseIndex, TAG_MACHINE)) {
			throw new RuntimeException("Machine tag parse error (offset=0x" + Integer.toHexString(parseIndex) +")");
		}
		//skip past the tag
		parseIndex += 12;
		
		//reading starts here, each read seeks +4 bytes
		p.machinePayloadSize = parseInt(data);
		p.machineName = parseString(data, 96);
		p.machineLayerImageFormat = parseString(data, 24);
		p.machineVolumeX = parseFloat(data);
		p.machineVolumeY = parseFloat(data);
		p.machineVolumeZ = parseFloat(data);
		p.machineVersion = parseInt(data);
		p.machine140 = parseInt(data);
	}
	
	// checks the passed tag matches a byte sequence in raw data 
	private static boolean compareTag(byte[] data, int offset, byte[] tag) {
		int max = tag.length;

		for (int i = 0; i < max; i++) {
			if (tag[i] != data[i + offset]) {
				return false;
			}
		}
		return true;
	}
	private static String parseString(byte[] data, int max) {
		int end = parseIndex;
		for (; end < parseIndex + max; end++) {
			if (data[end] == 0) {
				break;
			}
		}
		String result = new String(data, parseIndex, end - parseIndex); 
		parseIndex += max;
		return result;
	}

	// read 32bit integer, Little Endian 
	private static int parseInt(byte[] data) {
		int x1 = data[parseIndex++] & 0xFF;
		int x2 = data[parseIndex++] & 0xFF;
		int x3 = data[parseIndex++] & 0xFF;
		int x4 = data[parseIndex++] & 0xFF;
		return x1 | (x2 << 8) | (x3 << 16) | (x4 << 24);
	}
	
	// read 32bit float, Little Endian
	private static float parseFloat(byte[] data) {
		return Float.intBitsToFloat(parseInt(data));
	}
}
