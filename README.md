# pwmx_info
A tool to decode parameters from PWMX files for Anycubic Photon Mono X 3D printer. It decodes images of layers as well.

**Disclaimer**
* I'm not in any way affiliated with the manufacturer of Photon Mono X 3D printer. All information about
  the pwmx file format was gathered by inspecting the .pwmx files and searching the internet.
* The RLE decoding algorithm of the layer images is taken from the following project:
  https://github.com/sn4k3/UVtools


**Goals of this project:**
* document the .pwmx file structure for programmers of 3D slicing software
* have a tool to check the .pwmx file structure is correct
* inspect the parameters of the .pwmx file before printing

**Supported files:**
* pwmx: Photon Mono X
* pwmo: Photon Mono
* pwma: Photon Mono 4k
* dlp : Photon Ultra
* possibly other

**PWMX file structure:**
* check the ole/pwmx/Reader.java that parses the file data
* check the ole/pwmx/PwmxImageExporter.java which decodes the layer images and
  the preview image. 
* for more information check the https://github.com/sn4k3/UVtools/blob/master/UVtools.Core/FileFormats/PhotonWorkshopFile.cs


**RLE decoding schema:**
* layer images are run-length encoded (RLE)
* there are 2 types or RLE records: BW record, Antialising record
* BW Record: encoded in 2 bytes
<pre>
    Byte 0        Byte 1
   7654 3210     7654 3210 
  +----+----+   +----+----+
  | C  | N2 |   | N1 | N0 |
  *----+----+   +----+----+

  C - color of the pixel 4 bits (allowed values are 0x0 - no resin curing, 0xF - full resin curing)
  N - number of pixels of that color in 12 bits: N2 (bits 11-8)  N1 N0 (bits 7 - 0)
</pre>
  
  
* Antialisaing record: encoded in one byte
<pre>
    Byte 0
   7654 3210
  +----+----+
  | C  | N  |
  *----+----+
  C - color of the pixel 4 bits (allowed values are 0x1 to 0xE)
  N - number of pixels of that color in 4 bits
</pre>

* RLE records are stored in sequential order. To distinguish the BW and Antialiasing record use the top 4 bits
  of the first byte.
* To decode the layer image: treat your destination image as 1D array of bytes. Read each record from the source data
  and fill in the number of pixel in the destination array (your layer image) by the decoded color. Advance
  the destination array pointer by the number of pixels decoded.

**Compilation and running:** 
* ensure java jdk is installed (at least version 1.4)
* to compile run: javac ole/pwmx/Main.java
* to run the tool: java ole.pwmx.Main [parameters]

**Examples:**
<pre>
java ole.pwmx.Main model.pwmx          :  prints the info about 'model.pwmx' file
java ole.pwmx.Main model.pwmx 5        :  prints the info and exports layer 5
java ole.pwmx.Main model.pwmx 5 7      :  prints the info and exports layer 5 6 and 7
java ole.pwmx.Main model.pwmx *        :  prints the info and exports all layers.

When layer images are exported then the preview image is exported as well.
</pre>

