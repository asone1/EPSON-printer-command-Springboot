package localPrint;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.print.PrintService;

/*
 * 託運單的列印格式(使用LQ690印表機的API)
 */
public class PrintUsingDotMatrix {

	// zip code之間的間距
	private static final float zipCodeSpan = 0.6f;
	LocalPrinter escp;
	HashMap<String, String> msgInMap;

	// 一次前進0.8f
	// due to LQ690 printer's limit: units_low between 127 and 255 is ignored by
	// printer
	void next(int times) {
		for (int i = 0; i < times; ++i) {
			escp.advanceHorizontal(0.8f);
		}
	}

	boolean isNotEmpty(String s) {
		if (s != null && !s.isEmpty()) {
			return true;
		} else
			return false;
	}

	// 斷行列印
	 void printStringWithNewLine(String stringToPrint, int lengthPerLine, int MaxNumberOfLines, float horizontalPosition)
			throws UnsupportedEncodingException {
		int counter=0;
		if(stringToPrint==null||stringToPrint.length()==0) {
			while(counter<MaxNumberOfLines-1) {
				escp.advanceVertical(0.4f);
				System.out.println("newLine");
				++counter;
			}
		}else{

			StringBuilder s = new StringBuilder(stringToPrint);
			while(s.length()>0&&counter<MaxNumberOfLines) {
				if(counter >0) {
					escp.advanceVertical(0.4f);
				}
				if(s.length()>lengthPerLine) {
					escp.setAbsoluteHorizontalPosition(horizontalPosition);
					escp.print(s.substring(0, lengthPerLine));
					System.out.println(s.substring(0, lengthPerLine));
					s.delete(0, lengthPerLine);
					
				}else {
					escp.setAbsoluteHorizontalPosition(horizontalPosition);
					escp.print(s.substring(0, s.length()));
					System.out.println(s.substring(0, s.length()));
					s.delete(0, s.length());
				}
				++counter;
			}
			for(;counter<MaxNumberOfLines;++counter) {
				escp.advanceVertical(0.4f);
				System.out.println("newLine");
			}
		}
		

	}

	// 設定印表機列印資訊
	public void initializePrinterSetting() {
		escp.setCharacterSet();
		escp.select15CPI(); // 15 characters per inch printing
	}

	// 列印一張託運單
	public void printPerPage() throws UnsupportedEncodingException {
		escp.advanceVertical(0.42f);

		/*
		 * receiver info
		 */
		// zipcode
		String receiverZipCode = msgInMap.get("receiverZipCode");
		escp.doubleWidth();
		if (isNotEmpty(receiverZipCode)) {
			if (receiverZipCode.length() == 3) {
				escp.setAbsoluteHorizontalPosition(4.5f);
				escp.print(String.valueOf(receiverZipCode.charAt(0)));
				escp.advanceHorizontal(zipCodeSpan);
				escp.print(String.valueOf(receiverZipCode.charAt(1)));
				escp.advanceHorizontal(zipCodeSpan);
				escp.print(String.valueOf(receiverZipCode.charAt(2)));
			}
		}
		escp.cancelDoubleWidth();

		// receiverPhone
		String receiverPhone = msgInMap.get("receiverPhone");
		escp.advanceVertical(0.9f);
		if (isNotEmpty(receiverPhone)) {
			escp.setAbsoluteHorizontalPosition(4.5f);
			escp.print(receiverPhone);
		}

		// 收貨日
//		escp.advanceVertical(0.4f);
//		escp.setAbsoluteHorizontalPosition(12.8f);
//		escp.print("109");

		// receiverArea
		String receiverArea = msgInMap.get("receiverArea");
		escp.advanceVertical(0.8f);
		if (isNotEmpty(receiverArea)) {
			escp.setAbsoluteHorizontalPosition(4.5f);
			next(3);
			escp.print(receiverArea);
		}

		// temp_lev
		escp.advanceVertical(0.3f);
		String tempLv = msgInMap.get("tempLv");
		if (isNotEmpty(tempLv)) {
			escp.setAbsoluteHorizontalPosition(13.1f);
			if (tempLv.equals("tempLv1")) {
				next(3);
			} else if (tempLv.equals("tempLv4")) {
				next(6);
				escp.advanceHorizontal(0.3f);
			}
			escp.doubleWidth();
			escp.print("V");
			escp.cancelDoubleWidth();
		}
		
		// address
		String receiverAddr = msgInMap.get("receiverAddr");
		escp.advanceVertical(1.0f);
		if (isNotEmpty(receiverAddr)) {
			escp.setAbsoluteHorizontalPosition(3.5f);
		}
		printStringWithNewLine(receiverAddr, 20, 2, 3.5f);

		// name
		escp.advanceVertical(0.7f);
		String receiverName = msgInMap.get("receiverName");
		if (isNotEmpty(receiverName)) {
			escp.setAbsoluteHorizontalPosition(4.5f);
			next(3);
			escp.print(receiverName);
		}

		// wishTime

		escp.advanceVertical(0.15f);
		String wishTime = msgInMap.get("wishTime");
		if (isNotEmpty(wishTime)) {
			escp.setAbsoluteHorizontalPosition(13.7f);
			if (wishTime.equals("2")) {
				next(3);
			} else if (wishTime.equals("3")) {
				next(6);
			}
			escp.doubleWidth();
			escp.print("V");
			escp.cancelDoubleWidth();
		}

		/*
		 * sender info
		 */
		// ZIP CODE
		String senderZipCode = msgInMap.get("senderZipCode");
		escp.advanceVertical(0.7f);
		if (isNotEmpty(senderZipCode)) {
			if (senderZipCode.length() == 3) {
				escp.setAbsoluteHorizontalPosition(4.5f);
				escp.doubleWidth();
				escp.print(String.valueOf(senderZipCode.charAt(0)));
				escp.advanceHorizontal(zipCodeSpan);
				escp.print(String.valueOf(senderZipCode.charAt(1)));
				escp.advanceHorizontal(zipCodeSpan);
				escp.print(String.valueOf(senderZipCode.charAt(2)));
				escp.cancelDoubleWidth();
			} else {
				next(4);
			}
		} else {
			next(4);
		}

		// 客戶代碼
		String ecCompanyId = msgInMap.get("ecCompanyId");
		//escp.advanceHorizontal(6.0f);
		escp.advanceVertical(0.1f);
		if (isNotEmpty(ecCompanyId)) {
			escp.setAbsoluteHorizontalPosition(4.5f);
			next(10);
			escp.print(ecCompanyId);
		}

		// telephone
		String senderPhone = msgInMap.get("senderPhone");
		escp.advanceVertical(0.7f);
		if (isNotEmpty(senderPhone)) {
			escp.setAbsoluteHorizontalPosition(4.5f);
			escp.print(senderPhone);
		}

		// memo
		String note = msgInMap.get("note");
		escp.advanceVertical(0.1f);
		if (isNotEmpty(note)) {
			escp.setAbsoluteHorizontalPosition(13.3f);
		}
		printStringWithNewLine(note, 12, 3, 13.3f);

		// address
		escp.advanceVertical(0.2f);
		String senderAddr = msgInMap.get("senderAddr");
		if (isNotEmpty(senderAddr)) {
			escp.setAbsoluteHorizontalPosition(3.5f);
		}
		printStringWithNewLine(senderAddr, 20, 2, 3.5f);
		
		// box spec(60/90)
		String boxSpec_0 = null;
		String boxSpec_1 = null;
		// escp.advanceVertical(0.1f);

		String boxSpec = msgInMap.get("boxSpec");
		escp.advanceVertical(0.1f);
		if (isNotEmpty(boxSpec)) {
			escp.setAbsoluteHorizontalPosition(13.7f);
			if (boxSpec.equals("1") | boxSpec.equals("2")) {
				boxSpec_0 = "V";
				if (boxSpec.equals("2")) {
					next(2);
				}
			}
			if (boxSpec_0 != null) {
				escp.doubleWidth();
				escp.print(boxSpec_0);
				escp.cancelDoubleWidth();
			}
		}

		// senderName
		String senderName = msgInMap.get("senderName");
		escp.advanceVertical(0.5f);
		if (isNotEmpty(senderName)) {
			escp.setAbsoluteHorizontalPosition(4.5f);
			next(4);
			escp.print(senderName);
		}

		// box spec(120/150)

		if (isNotEmpty(boxSpec)) {
			escp.setAbsoluteHorizontalPosition(13.7f);
			if (boxSpec.equals("3") | boxSpec.equals("4")) {
				boxSpec_1 = "V";
				if (boxSpec.equals("4")) {
					next(2);
				}
			}
			if (boxSpec_1 != null) {
				escp.doubleWidth();
				escp.print(boxSpec_1);
				escp.cancelDoubleWidth();
			}
		}
		
		//escp.print("__END");
	}

	// NumberFormatException, FileNotFoundException, UnsupportedEncodingException
//	public void print(HashMap<String, String> msgInMap, String printerName) throws Exception {
//		escp = new LocalPrinter(printerName, true); // create ESCPrinter on network location
//		this.msgInMap = msgInMap;
//
//		if (escp.initialize()) {
//			initializePrinterSetting();
//			int page = Integer.valueOf(msgInMap.get("page"));
//			int counterPage = 0;
//			while (counterPage < page) {
//				++counterPage;
//				printPerPage();
//				
//				// 每三頁會多小間距??
//				if (counterPage % 2 == 0)
//					escp.advanceVertical(0.04f);
//			}
//
//			//escp.formFeed(); // eject paper
//		} else
//			System.out.println("Couldn't open stream to printer");
//	}

	public void print(HashMap<String, String> msgInMap, PrintService printer) throws Exception {
		escp = new LocalPrinter(printer, true); // create ESCPrinter on network location
		this.msgInMap = msgInMap;

		if (escp.initialize()) {
			initializePrinterSetting();
			int page = Integer.valueOf(msgInMap.get("page"));
			int counterPage = 1;
			while (counterPage < page) {
				++counterPage;
				printPerPage();
				// 剩餘託運單空間
				escp.advanceVertical(2.01f);
				// 每三頁會多小間距??
				if (counterPage>0&&counterPage % 2 == 0) {
					escp.advanceVertical(0.04f);
				}
			}
			if((counterPage==page)) {
				printPerPage();
				//0.0.0.2
				escp.formFeed();
			}

			//escp.formFeed(); // eject paper//這裡要改 如果不是連續進紙
		} else
			System.out.println("Couldn't open stream to printer");
	}
}
