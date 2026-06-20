package com.pdftoolkit.support;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builds small, valid sample files for processor tests.
 */
public final class TestFiles {

    private TestFiles() {
    }

    /** A PDF with {@code pages} pages, each carrying a line of text. */
    public static byte[] pdf(int pages) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 1; i <= pages; i++) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    cs.newLineAtOffset(72, 700);
                    cs.showText("Sample page " + i);
                    cs.endText();
                }
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    public static Path pdfFile(Path dir, String name, int pages) throws Exception {
        Path path = dir.resolve(name);
        Files.write(path, pdf(pages));
        return path;
    }

    /** A solid-colour PNG image written to {@code dir}. */
    public static Path pngFile(Path dir, String name, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        Path path = dir.resolve(name);
        try (var out = Files.newOutputStream(path)) {
            ImageIO.write(image, "png", out);
        }
        return path;
    }
}
