package localPrint;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

/*
 * LQ690印表機的API
 */
public class LocalPrinter {

	private boolean streamOpenSuccess;
	PrintService[] services = null;
	PrintService myPrinter = null;
	private boolean escp24pin;

	public LocalPrinter(String printerName, boolean escp24pin) {
		this.escp24pin = escp24pin;
		services = PrintServiceLookup.lookupPrintServices(null, null);
		for (int i = 0; i < services.length; i++) {
			System.out.println("service found: " + services[i]);
			String svcName = services[i].toString();
			if (svcName.contains(printerName)) {
				myPrinter = services[i];
				System.out.println("my printer found: " + myPrinter);
				break;
			}
		}
	}
	
	public LocalPrinter(PrintService printer, boolean escp24pin) {
		this.escp24pin = escp24pin;
		this.myPrinter = printer;
	}

	// 設定成big-5
	public void setCharacterSet() {
		// assign character table
		command(FS);
		command(t);
		command(ARGUMENT_3);
	}

	// send print content in bytes (should be encoded with default character set) to
	// LQ printer
	public void print(String text) throws UnsupportedEncodingException {
		if (text == null) {
		} else {
			if (myPrinter != null) {
				Doc myDoc = new SimpleDoc(text.getBytes("BIG5"), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
				DocPrintJob job = myPrinter.createPrintJob();
				try {
					job.print(myDoc, null);
				} catch (PrintException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// send printer command (bytes should be encoded with ASCII) to LQ printer
	public void command(char symbol) {
		if (myPrinter != null) {
			String result = String.valueOf(symbol);
			Doc myDoc = new SimpleDoc(result.getBytes(StandardCharsets.US_ASCII), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
			DocPrintJob job = myPrinter.createPrintJob();
			try {
				job.print(myDoc, null);
			} catch (PrintException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean initialize() throws FileNotFoundException {
		// post: returns true iff stream to network printer successfully opened, streams
		// for writing to esc/p printer created
		streamOpenSuccess = false;

		// reset default settings
		command(ESC);
		command(AT);
		command(FS);
		command(AMPERSAND);
		// select 10-cpi character pitch
		select10CPI();

		// select draft quality printing
		selectLQPrinting();

		// set character set
		// setCharacterSet(BRAZIL);
		streamOpenSuccess = true;

		return streamOpenSuccess;
	}

	public void selectDraftPrinting() { // set draft quality printing
		command(ESC);
		command(x);
		command((char) 48);
	}

	public void selectLQPrinting() { // set letter quality printing
		command(ESC);
		command(x);
		command((char) 49);
	}

	public void select10CPI() { // 10 characters per inch (condensed available)
		command(ESC);
		command(P);
	}

	public void select15CPI() { // 15 characters per inch (condensend not available)
		command(ESC);
		command(g);
	}

	public void lineFeed() {
		// post: performs new line
		command(CR); // according to epson esc/p ref. manual always send carriage return before line
						// feed
		command(LINE_FEED);
	}

	public void formFeed() {
		// post: ejects single sheet
		command(CR); // according to epson esc/p ref. manual it is recommended to send carriage
						// return before form feed
		command(FF);
	}

	// 兩倍寬列印
	public void doubleWidth() {
		command(ESC);
		command(W);
		command(ARGUMENT_1);
	}

	// 取消兩倍寬列印
	public void cancelDoubleWidth() {
		command(ESC);
		command(W);
		command(ARGUMENT_0);
	}

	public void advanceVertical(float centimeters) {
		// pre: centimeters >= 0 (cm)
		// post: advances vertical print position approx. y centimeters (not precise due
		// to truncation)
		float inches = centimeters / CM_PER_INCH;
		int units = (int) (inches * (escp24pin ? MAX_ADVANCE_24PIN : MAX_ADVANCE_9PIN));

		while (units > 0) {
			char n;
			if (units > MAX_UNITS)
				n = (char) MAX_UNITS; // want to move more than range of parameter allows (0 - 255) so move max amount
			else
				n = (char) units; // want to move a distance which fits in range of parameter (0 - 255)

			command(ESC);
			command(J);
			command(n);

			units -= MAX_UNITS;
		}
	}

	public void advanceHorizontal(float centimeters) {
		// pre: centimeters >= 0
		// post: advances horizontal print position approx. centimeters
		float inches = centimeters / CM_PER_INCH;
		int units_low = (int) (inches * 120) % 256;
		int units_high = (int) (inches * 120) / 256;

		command(ESC);
		command(BACKSLASH);
		command((char) units_low);
		command((char) units_high);
	}

	public void setAbsoluteHorizontalPosition(float centimeters) {
		// pre: centimenters >= 0 (cm)
		// post: sets absolute horizontal print position to x centimeters from left
		// margin
		float inches = centimeters / CM_PER_INCH;
		int units_low = (int) (inches * 60) % 256;
		int units_high = (int) (inches * 60) / 256;

		command(ESC);
		command($);
		command((char) units_low);
		command((char) units_high);
	}

	public boolean isInitialized() {
		// post: returns true iff printer was successfully initialized
		return streamOpenSuccess;
	}

	private static int MAX_ADVANCE_9PIN = 216; // for 24/48 pin esc/p2 printers this should be 180
	private static int MAX_ADVANCE_24PIN = 180;
	private static int MAX_UNITS = 127; // for vertical positioning range is between 0 - 255 (0 <= n <= 255) according
										// to epson ref. but 255 gives weird errors at 1.5f, 127 as max (0 - 128) seems
										// to be working
	private static final float CM_PER_INCH = 2.54f;

	/* decimal ascii values for epson ESC/P commands */
	private static final char ESC = 27; // escape
	private static final char FS = 28; //
	private static final char AT = 64; // @
	private static final char AMPERSAND = 38;// &
	private static final char LINE_FEED = 10; // line feed/new line
	private static final char PARENTHESIS_LEFT = 40;// (
	private static final char BACKSLASH = 92;
	private static final char CR = 13; // carriage return
	private static final char TAB = 9; // horizontal tab
	private static final char FF = 12; // form feed
	private static final char g = 103; // 15cpi pitch
	private static final char p = 112; // used for choosing proportional mode or fixed-pitch
	private static final char t = 116; // used for character set assignment/selection
	private static final char l = 108; // used for setting left margin
	private static final char x = 120; // used for setting draft or letter quality (LQ) printing
	private static final char E = 69; // bold font on
	private static final char F = 70; // bold font off
	private static final char J = 74; // used for advancing paper vertically
	private static final char P = 80; // 10cpi pitch
	private static final char V = 86;// used for absolute vertical positioning
	private static final char W = 87;
	private static final char Q = 81; // used for setting right margin
	private static final char $ = 36; // used for absolute horizontal positioning
	private static final char ARGUMENT_0 = 0;
	private static final char ARGUMENT_1 = 1;
	private static final char ARGUMENT_2 = 2;
	private static final char ARGUMENT_3 = 3;
	private static final char ARGUMENT_4 = 4;
	private static final char ARGUMENT_5 = 5;
	private static final char ARGUMENT_6 = 6;
	private static final char ARGUMENT_7 = 7;
	private static final char ARGUMENT_25 = 25;
}
