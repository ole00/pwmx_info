package ole.pwmx;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class Main {

	private static final String VERSION = "1.1";
	
	private static void printHelp() {
		System.out.println("PWMX Info v." + VERSION);
		System.out.println("parameters: path [export_layer_start [export_layer_end]]");
		System.out.println("   path                :  .pwmx file");
		System.out.println("   export_layer_start  : first layer to export or * for all");
		System.out.println("   export_layer_end    : last layer to export");
		System.out.println("Examples:");
		System.out.println("   model.pwmx          :  prints the info");
		System.out.println("   model.pwmx 5        :  prints the info and export layer 5");
		System.out.println("   model.pwmx 5 7      :  prints the info and export layer 5 6 and 7");
		System.out.println("   model.pwmx *        :  prints the info and exports alllayers");
		System.out.println("                          This may take long time!");
	}
	
	public static void main(String[] args) {
		int exportLayerStart = 0;
		int exportLayerEnd = 0;
		// TODO Auto-generated method stub
		if (args.length < 1) {
			printHelp();
			return;
		}
		
		Pwmx p = Reader.read(args[0]);
		p.print(args[0]);

		if (args.length > 1) {
			if (args[1].equals("*")) {
				exportLayerStart = 1;
				exportLayerEnd = p.layerCount;
			} else {
				exportLayerStart = Integer.decode(args[1]).intValue();
				exportLayerEnd = exportLayerStart;
			}
		}
		if (args.length > 2) {
			if (args[2].equals("*")) {
				exportLayerEnd = p.layerCount;
			} else {
				exportLayerEnd = Integer.decode(args[2]).intValue();
			}
		}

		int total = exportLayerEnd - exportLayerStart + 1;
		if (exportLayerStart > 0) {
			int i = 0;
			boolean[] percPrinted = new boolean[]{false};
			long startTime = System.currentTimeMillis();
			
			//export preview image to png 
			PwmxImageExporter.exportPreview(p);
			
			//export the layers
			for (int layerIndex = exportLayerStart; layerIndex <= exportLayerEnd; layerIndex++) {
				// export layer image to png
				PwmxImageExporter.exportLayer(p, layerIndex);
				
				// show exporting progress
				printExportProgress(++i, total, percPrinted, startTime);
			}

			// print final 100% if not already printed
			printExportProgress(i + 1, total, percPrinted, startTime);
		}
	}
	
	private static void printExportProgress(int i, int total, boolean[] percPrinted, long startTime) {
		if (i > total) {
			if (!percPrinted[0]) {
				System.out.println(" [100%]");
			}
			System.out.println("Export time: " + ((System.currentTimeMillis() - startTime) / 1000) + " sec.");
			return;
		}
		if (total > 1) {
			if (i % 50 == 0) {
				System.out.println(". [" + ((i * 100) / total) + "%]");
				percPrinted[0] = true;
			} else {
				System.out.print(".");
				percPrinted[0] = false;
			}
		}
	}

}
