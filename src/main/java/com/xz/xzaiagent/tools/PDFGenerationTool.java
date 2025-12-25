package com.xz.xzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.xz.xzaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * PDF 生成工具类
 */
@Slf4j
public class PDFGenerationTool {

    @Tool(description = "根据内容生成 PDF 文件并保存")
    public String generatePDF(@ToolParam(description = "要保存的 PDF 文件名") String fileName,
                              @ToolParam(description = "要写入 PDF 的内容") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter pdfWriter = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(pdfWriter);
                 Document document = new Document(pdf)) {
                // 基本输入校验与预处理
                if (content == null) content = "";
                String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                log.info("生成 PDF: filename={}, content_length={}, preview={}", fileName, content.length(), preview);

                // 如果传入的是 HTML，尝试提取纯文本（避免把 HTML 标签写入 PDF 导致视觉为空白）
                if (content.trim().startsWith("<") && content.trim().endsWith(">")) {
                    try {
                        // 使用 Jsoup 提取正文文本（如果 classpath 中有 Jsoup）
                        content = org.jsoup.Jsoup.parse(content).text();
                        log.debug("检测到 HTML 内容，已提取纯文本，长度={}", content.length());
                    } catch (Throwable ignored) {
                        // 如果没有 Jsoup，可保留原始内容
                        log.debug("HTML 解析尝试失败，继续使用原始内容");
                    }
                }

                // 选择字体：优先加载项目 resources 中的本地中文字体（例如 simsun.ttf），然后尝试内置中文字体，最后回退到 Helvetica
                PdfFont font = null;
                // try {
                //     // 尝试本地 resources 路径（开发环境中常见位置）
                //     String localPath = "src/main/resources/static/fonts/simsun.ttf";
                //     File localFontFile = new File(localPath);
                //     if (localFontFile.exists()) {
                //         font = PdfFontFactory.createFont(localFontFile.getAbsolutePath(), PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                //         log.info("PDF 使用本地字体: {}", localFontFile.getAbsolutePath());
                //     } else {
                //         // 尝试从 classpath 中读取（例如打包后放在 resources/static/fonts）
                //         URL res = Thread.currentThread().getContextClassLoader().getResource("static/fonts/simsun.ttf");
                //         if (res != null) {
                //             File tmp = File.createTempFile("simsun-", ".ttf");
                //             try (InputStream in = res.openStream(); FileOutputStream out = new FileOutputStream(tmp)) {
                //                 in.transferTo(out);
                //             }
                //             tmp.deleteOnExit();
                //             font = PdfFontFactory.createFont(tmp.getAbsolutePath(), PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                //             log.info("PDF 使用 classpath 字体临时文件: {}", tmp.getAbsolutePath());
                //         }
                //     }
                // } catch (Exception e) {
                //     log.warn("加载本地字体失败：{}", e.getMessage());
                // }

                if (font == null) {
                    try {
                        font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                        // log.info("PDF 使用内置中文字体 STSongStd-Light");
                    } catch (Exception e) {
                        log.warn("内置中文字体创建失败，使用回退字体 Helvetica: {}", e.getMessage());
                        try {
                            font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
                        } catch (Exception ex) {
                            font = PdfFontFactory.createFont();
                        }
                    }
                }

                document.setFont(font);
                // 创建段落并写入（即使是空字符串，iText 也会在 PDF 中写入空白行）
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);
            }
            // 验证文件是否存在且大小合理
            try {
                File f = new File(filePath);
                if (!f.exists() || f.length() == 0) {
                    log.error("生成的 PDF 文件不存在或大小为0: {}", filePath);
                    return "PDF 生成失败：输出文件为空，请检查内容与字体配置";
                } else {
                    return "PDF 已生成并保存：" + filePath + " (size=" + f.length() + " bytes)";
                }
            } catch (Exception ex) {
                log.warn("生成后检查文件出现异常：{}", ex.getMessage());
                return "PDF 已生成并保存：" + filePath;
            }
        } catch (Exception e) {
            log.error("生成 PDF 失败：{}", e.getMessage());
            return "PDF 生成工具：生成 PDF 失败！";
        }
    }
}

// EN
// Generate a PDF file with given content
// Name of the file to save the generate PDF
// Content to be included in the PDF
// PDF generated successfully to:
// Error generating PDF: