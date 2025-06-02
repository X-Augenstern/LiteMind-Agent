package com.xz.xzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.xz.xzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * PDF 生成工具类
 */
public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(@ToolParam(description = "Name of the file to save the generate PDF") String fileName,
                              @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter pdfWriter = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(pdfWriter);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
                // String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
                //         .toAbsolutePath().toString();
                // PdfFont font = PdfFontFactory.createFont(
                //         fontPath,
                //         PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                // );
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont(
                        "STSongStd-Light",
                        "UniGB-UCS2-H"
                );
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 修改段落并关闭文档
                document.add(paragraph);
            }
            return "PDF generated successfully to: " + filePath;
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
