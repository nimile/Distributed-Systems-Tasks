package de.hrw.dsalab.distsys.tools;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class XmlCreator {
    private XmlCreator(){}

    private static void printUsage(){
        System.out.println("Usage: XmlCreator xmlFile1 xmlFile2 outputDtdFile");
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            printUsage();
            return;
        }
        File xmlFile1 = new File(args[0]);
        File xmlFile2 = new File(args[1]);
        File dtdFile = new File(args[2]);
        try {
            generate(xmlFile1, xmlFile2, dtdFile);
        } catch (Exception ex) {
            ex.printStackTrace();
            printUsage();
        }
    }

    private static Document getDocument(File xmlFile) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document result = db.parse(xmlFile);
        result.normalizeDocument();
        return result;
    }

    private static void generate(File xmlFile1, File xmlFile2, File dtdFile) throws Exception {
        if(!xmlFile1.exists() ||!xmlFile2.exists()){
            throw new Exception("One xml file does not exists");
        }
        Document doc1 = getDocument(xmlFile1);
        Document doc2 = getDocument(xmlFile2);

        var commons = getCommons(doc1, doc2);

        for (Node common : commons) {
            System.out.println(common.getNodeName() + ": " + common.getNodeValue());
        }

    }

    private static List<Node> getCommons(Document document1, Document document2){
        List<Node> result = new ArrayList<>();
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreWhitespace(true);

        var diff = new Diff(document1, document2);
        diff.overrideMatchTracker(new MatchTracker() {
            @Override
            public void matchFound(Difference difference) {
                result.add(difference.getTestNodeDetail().getNode());
            }
        });
        return result;
    }

}
