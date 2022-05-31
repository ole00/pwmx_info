package ole.pwmx;

public class Pwmx {
	private static final String SPACER ="                           ";
	
	
	int version; //12
	int areaNum; //16 - unknown
	int headerDataOffset; //20
	int previewDataOffset; //24
	float intro24;
	int layerDataOffset; //28
	float intro32;
	int imageDataOffset; //Layer images
	float intro40;
	
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
	
	int previewW;
	int previewDpi;
	int previewH;
	int previewImageOffset;
	
	int layerCount;
	PwmxLayer[] layers;
	
	byte[] rawData;
	
	public void print(String label) {
		System.out.println("PWMX " + (label != null ? label : ""));
		System.out.println("---[intro]------------------------");
		printInt("version major", version);
		printInt("area number", areaNum);
		printInt("header offset", headerDataOffset);
		printInt("preview offset", previewDataOffset);
		printInt("layer-def offset", layerDataOffset);
		printInt("layer-img offset", imageDataOffset);
		printFloat("intro24", intro24);
		printFloat("intro32", intro32);
		printFloat("intro40", intro40);
		
		System.out.println("---[header]------------------------");
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
		System.out.println("---[preview]------------------------");
		printInt("preview img width", previewW);
		printInt("preview dpi", previewDpi);
		printInt("preview img height", previewH);
		printInt("preview img offset", previewImageOffset);
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
}
