import java.io.IOException;
/**
 * Program to assist with adding RA information to Project SEED's RedCap database
 * @author Colin Stout
 * Contact me at cstout2718@gmail.com
 */
public class Main {
	
	public static void main(String[] args) throws IOException {
		// attempt to read docx files; not working so great
//		try {
//			FileInputStream fis = new FileInputStream("DataTestDoc.docx");
//			XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis));
//			XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
//			System.out.println(extractor.getText());
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
		
		GUI gui = new GUI("Data Addition");

	}

}
