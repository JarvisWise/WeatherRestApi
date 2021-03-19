package com.weather.rest.api.kolisnyk.model;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class MSWordModel is responsible for
 * creating docx model
 */


public class MSWordModel {

    /**
     * This method responsible for creating docx model
     *
     * @param serviceName name of service
     * @param str         string with data
     * @return ByteArrayInputStream with docx model data
     * @throws IOException  creating docx model failed
     * @throws XmlException creating docx model failed
     */

    public static ByteArrayInputStream createDocxModel(String serviceName, String str) throws IOException, XmlException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFDocument docxModel = new XWPFDocument();
            CTSectPr ctSectPr = docxModel.getDocument().getBody().addNewSectPr();
            XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(docxModel, ctSectPr);

            CTP ctpHeaderModel = createHeaderModel("Date taken form " + serviceName);
            XWPFParagraph headerParagraph = new XWPFParagraph(ctpHeaderModel, docxModel);
            headerFooterPolicy.createHeader(
                    XWPFHeaderFooterPolicy.DEFAULT,
                    new XWPFParagraph[]{headerParagraph}
            );

            CTP ctpFooterModel = createFooterModel("Date taken form " + serviceName);
            XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooterModel, docxModel);
            headerFooterPolicy.createFooter(
                    XWPFHeaderFooterPolicy.DEFAULT,
                    new XWPFParagraph[]{footerParagraph}
            );

            XWPFParagraph bodyParagraph = docxModel.createParagraph();
            bodyParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun paragraphConfig = bodyParagraph.createRun();
            paragraphConfig.setItalic(true);
            paragraphConfig.setFontSize(18);
            paragraphConfig.setColor("1d1c1f");
            paragraphConfig.setText(str);

            docxModel.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * This method responsible for creating footer of docx document
     *
     * @param footerContent text for footer
     * @return created footer
     */

    private static CTP createFooterModel(String footerContent) {
        CTP ctpFooterModel = CTP.Factory.newInstance();
        CTR ctrFooterModel = ctpFooterModel.addNewR();
        CTText cttFooter = ctrFooterModel.addNewT();

        cttFooter.setStringValue(footerContent);
        return ctpFooterModel;
    }

    /**
     * This method responsible for creating header of docx document
     *
     * @param headerContent text for header
     * @return created header
     */

    private static CTP createHeaderModel(String headerContent) {
        CTP ctpHeaderModel = CTP.Factory.newInstance();
        CTR ctrHeaderModel = ctpHeaderModel.addNewR();
        CTText cttHeader = ctrHeaderModel.addNewT();

        cttHeader.setStringValue(headerContent);
        return ctpHeaderModel;
    }

}
