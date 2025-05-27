package com.cdr.msloader.parser;

import com.cdr.msloader.entity.CDR;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlParser implements CDRParser {
    private static final Logger log = LoggerFactory.getLogger(XmlParser.class);

    @Override
    public List<CDR> parse(File file) throws Exception {
        log.debug("Parsing XML file: {}", file.getName());
        List<CDR> records = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            
            // Get all CDR elements
            NodeList cdrNodes = document.getElementsByTagName("cdr");
            
            for (int i = 0; i < cdrNodes.getLength(); i++) {
                Element cdrElement = (Element) cdrNodes.item(i);
                CDR cdr = new CDR();
                
                cdr.setSource(getElementText(cdrElement, "source"));
                cdr.setDestination(getElementText(cdrElement, "destination"));
                cdr.setStartTime(LocalDateTime.parse(getElementText(cdrElement, "starttime")));
                cdr.setService(getElementText(cdrElement, "service"));
                cdr.setUsage(parseUsage(getElementText(cdrElement, "usage"), cdr.getService()));
                
                records.add(cdr);
            }
        } catch (Exception e) {
            log.error("Error parsing XML file: {}", file.getName(), e);
            throw e;
        }
        
        return records;
    }

    @Override
    public boolean canHandle(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".xml");
    }

    private String getElementText(Element parent, String tagName) {
        return parent.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private Integer parseUsage(String usage, String service) {
        if (service.equals("SMS")) {
            return 1;
        }
        return Integer.parseInt(usage);
    }
} 