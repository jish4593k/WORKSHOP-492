import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class InsertSignature {

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            Options options = new Options();

            options.addOption("p", "page", true, "The pages where to add the signature");
            options.addOption("o", "output", true, "Output PDF file");
            options.addOption("i", "overwrite", false, "Overwrite the input file");
            options.addOption("s", "scale", true, "Scaling of the signature");
            options.addOption("x", true, "Position of signature on the page in mm (x)");
            options.addOption("y", true, "Position of signature on the page in mm (y)");

            CommandLine cmd = parser.parse(options, args);

            String inputFile = cmd.getArgs()[0];
            String outputFile = cmd.hasOption("o") ? cmd.getOptionValue("o") : inputFile;
            int[] pages = parsePages(cmd.getOptionValue("p"));
            float x = Float.parseFloat(cmd.getOptionValue("x")) / 25.4f * 72;
            float y = Float.parseFloat(cmd.getOptionValue("y")) / 25.4f * 72;
            float scale = cmd.hasOption("s") ? Float.parseFloat(cmd.getOptionValue("s")) : 1.0f;

            if (cmd.hasOption("i")) {
                outputFile = inputFile;
            }

            insertSignature(inputFile, outputFile, pages, x, y, scale);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] parsePages(String pagesOption) {
        if (pagesOption != null) {
            String[] pageStrings = pagesOption.split(",");
            int[] pages = new int[pageStrings.length];
            for (int i = 0; i < pageStrings.length; i++) {
                pages[i] = Integer.parseInt(pageStrings[i]);
            }
            return pages;
        }
        return new int[]{1};
    }

    private static void insertSignature(String inputFile, String outputFile, int[] pages, float x, float y, float scale)
            throws IOException {
        String signatureFileName = "~/.signature.pdf";

        try (PdfReader inputReader = new PdfReader(inputFile);
             PdfReader signatureReader = new PdfReader(signatureFileName);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            PdfStamper stamper = new PdfStamper(inputReader, outputStream);
            PdfImportedPage signaturePage = stamper.getImportedPage(signatureReader, 1);

            for (int pageNum : pages) {
                PdfContentByte content = stamper.getOverContent(pageNum);
                content.addTemplate(signaturePage, scale, 0, 0, scale, x, y);
            }

            stamper.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
