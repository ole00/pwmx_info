package ole.pwmx;

public class Pwmx {
	private static final String SPACER ="                           ";
	
	
	int version; //12  (value 1 - older printers, 515 - PhoUltra *.dlp, 516 - PhoMonoX4k *.pwma) 
	int areaNum; //16 - unknown
	int headerDataOffset; //20
	int previewDataOffset; //24
	float intro24;
	int layerDataOffset; //28
	int intro32;
	int imageDataOffset; //Layer images
	float intro40;
	
	//pwma specific
	int extraDataOffset; //Extra section
	int machineDataOffset; //Machine section
	
	int headerPayloadSize;
	float pixelSizeUm;
	float layerHeight;
	float exposureTime;
	float delayBeforeExposure;
	float bottomExposureTime;
	float bottomLayerCount;
	float liftDistance;
	float liftSpeed;
	float retractSpeed;
	float volumeMl;
	int  antiAliasing;
	int resX; //60
	int resY; //64
	float weightG;
	float price;
	int priceCurrency;
	int perLayerOverride;
	int printTime;
	int transitionLayerCount;
	int padding2;
	int padding3;
	
	// extra - in version 516
	int extra0; //0 - value 24
	int extra4; //4 - value 2
	float extraLiftDistance1; //8
	float extraLiftSpeed1;  //12
	float extraRetractSpeed1; //16
	float extraLiftDistance2; //20
	float extraLiftSpeed2; //24
	float extraRetractSpeed2; //28
	int extra32; //32 - value 2
	float extraLiftDistance3; //36
	float extraLiftSpeed3;  //40
	float extraRetractSpeed3; //44
	float extraLiftDistance4; //48
	float extraLiftSpeed4; //52
	float extraRetractSpeed4; //56
	
	// machine - in version 516
	int machinePayloadSize; //0
	String machineName;  // 4 
	String machineLayerImageFormat; //100
	float machineVolumeX; //124
	float machineVolumeY; //128
	float machineVolumeZ; //132
	int machineVersion; //136
	int machine140;//
	
	int previewPayloadSize;
	int previewW;
	int previewDpi;
	int previewH;
	int previewImageOffset;
	int[] previewUnknown = new int[7]; // Background Colors ? Text palette? 
	
	int layerCount;
	PwmxLayer[] layers;
	
	byte[] rawData;
	
	public void print(String label) {
		System.out.println("PWMX " + (label != null ? label : ""));
		System.out.println("---[intro]------------------------");
		printInt("version major", version);
		printInt("area number", areaNum);
		printInt("header offset", headerDataOffset);
		printFloat("intro24", intro24);
		printInt("preview offset", previewDataOffset);
		if (version == 516) {
			printInt("preview unknown offset", intro32);		
		} else {
			printInt("intro32", intro32);
		}
		printInt("layer-def offset", layerDataOffset);
		if (version == 516) {
			printInt("extra section offset", extraDataOffset);
			printInt("machine section offset", machineDataOffset);			
		} else {
			printFloat("intro40", intro40);		
		}
		printInt("layer-img offset", imageDataOffset);

		
		System.out.println("---[header]------------------------");
		printInt("payload size", headerPayloadSize);
		printFloat("pixel size uM", pixelSizeUm);
		printFloat("layer height mm", layerHeight);
		printFloat("exposure time s", exposureTime);
		printFloat("delay before exposure s", delayBeforeExposure);
		printFloat("bottom exposure time s", bottomExposureTime);
		printFloat("bottom layer count", bottomLayerCount);
		printFloat("lift distance mm", liftDistance);
		printFloat("lift speed mm/s", liftSpeed);
		printFloat("retract speed mms/s", retractSpeed);
		printFloat("volume ml", volumeMl);
		printFloat("antialiasing", antiAliasing);
		printInt("resolution X", resX);
		printInt("resolution Y", resY);
		printFloat("weightG", weightG);
		printFloat("price", price);
		printInt("price currency (char)", priceCurrency);
		printInt("per layer override", perLayerOverride);
		printInt("print time s", printTime);
		printInt("transition layer count", transitionLayerCount);
		printInt("padding2", padding2);
		
		if (version == 516) {
			System.out.println("---[extra]-------------------------");
			printInt("extra0", extra0);
			printInt("extra4", extra4);		
			printFloat("lift distance (1) mm", extraLiftDistance1);
			printFloat("lift speed (1) mm/s", extraLiftSpeed1);
			printFloat("retract speed (1) mms/s", extraRetractSpeed1);
			printFloat("lift distance (2) mm", extraLiftDistance2);
			printFloat("lift speed (2) mm/s", extraLiftSpeed2);
			printFloat("retract speed (2) mms/s", extraRetractSpeed2);
			printInt("extra32", extra32);
			printFloat("lift distance (3) mm", extraLiftDistance3);
			printFloat("lift speed (3) mm/s", extraLiftSpeed3);
			printFloat("retract speed (3) mms/s", extraRetractSpeed3);
			printFloat("lift distance (4) mm", extraLiftDistance4);
			printFloat("lift speed (4) mm/s", extraLiftSpeed4);
			printFloat("retract speed (4) mms/s", extraRetractSpeed4);
			
			System.out.println("---[machine]-------------------------");
			printInt("machine payload size", machinePayloadSize);
			printString("machine name", machineName);
			printString("layer image format", machineLayerImageFormat);
			printFloat("print size X", machineVolumeX);
			printFloat("print size Y", machineVolumeY);
			printFloat("print size Z", machineVolumeZ);
			printInt("version", machineVersion);
			printInt("machine140", machine140);
		}
		
		System.out.println("---[preview]------------------------");
		printInt("preview payload size", previewPayloadSize);
		printInt("preview img width", previewW);
		printInt("preview dpi", previewDpi);
		printInt("preview img height", previewH);
		printInt("preview img offset", previewImageOffset);
		if (version == 516) {
			for (int i = 0; i < 7; i++) {
				printInt("preview unknown " + i, previewUnknown[i]);			
			}
		}
		System.out.println("---[layers]------------------------");
		printInt("layer count", layerCount);
		
		for (int i = 0;  i < layerCount; i++) {
			PwmxLayer l = layers[i];
			System.out.println("Layer " + (i+1));
			printInt("    img offset", l.imgOffset);
			printInt("    img size", l.imgSize);
			printFloat("    lift distance", l.liftDistance);
			printFloat("    lift speed mm/s", l.liftSpeed);
			printFloat("    exposure time s", l.exposureTime);
			printFloat("    layer height mm", l.height);
			printFloat("    l44 / unknown", l.layer44);
			printFloat("    l48 / unknown ", l.layer48);
		}

		
	}
	
	private void printInt(String label, int value) {
		String s = label + SPACER.substring(label.length());
		s = s.concat(": ");
		s = s.concat(Integer.toString(value));
		s = s.concat(" (0x").concat(Integer.toHexString(value)).concat(")");
		System.out.println(s);
	}
	private void printFloat(String label, float value) {
		String s = label + SPACER.substring(label.length());
		s = s.concat(": ");
		s = s.concat(Float.toString(value));
		s = s.concat(" (0x").concat(Integer.toHexString(Float.floatToRawIntBits(value))).concat(")");
		System.out.println(s);
	}
	private void printString(String label, String value) {
		String s = label + SPACER.substring(label.length());
		s = s.concat(": ");
		s = s.concat(value);
		System.out.println(s);
	}

}
