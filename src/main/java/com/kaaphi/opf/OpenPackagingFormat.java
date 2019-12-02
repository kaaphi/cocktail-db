package com.kaaphi.opf;

import java.io.FileOutputStream;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class OpenPackagingFormat {
  private static final Namespace OPF = Namespace.getNamespace("http://www.idpf.org/2007/opf");
  private static final Namespace SUB_OPF = Namespace.getNamespace("opf", "http://www.idpf.org/2007/opf");
  private static final Namespace DC = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
  private Document doc;

  public OpenPackagingFormat() {
    doc = new Document();
    doc.setRootElement(new Element("package", OPF));
    Element root = doc.getRootElement();

    Element metadata = new Element("metadata", OPF);
    root.addContent(metadata);

    Element title = new Element("title", DC);
    title.setText("Drinks Compendium");

    Element language = new Element("language", DC);
    language.setText("en");

    Element creator = new Element("creator", DC);
    creator.setText("Phil Kaasa");
    creator.setAttribute(new Attribute("file-as", "Kaasa, Phil", SUB_OPF));
    creator.setAttribute(new Attribute("role", "edt", SUB_OPF));

    Element identifier = new Element("identifier", DC);
    identifier.setAttribute(new Attribute("id", "BookId"));
    identifier.setText("CocktailDB");

    metadata.addContent(title);
    metadata.addContent(language);
    metadata.addContent(creator);
    metadata.addContent(identifier);

    Element manifest = new Element("manifest", OPF);
    root.addContent(manifest);

    Element content = new Element("item", OPF);
    content.setAttribute("id", "content");
    content.setAttribute("href", "content.xhtml");
    content.setAttribute("media-type", "application/xhtml+xml");

    manifest.addContent(content);

    Element spine = new Element("spine", OPF);
    root.addContent(spine);

    Element itemref = new Element("itemref", OPF);
    itemref.setAttribute("idref", "content");
    spine.addContent(itemref);

  }

  public static void main(String[] args) throws Exception {
    FileOutputStream out = new FileOutputStream("test_opf/test.opf");
    new XMLOutputter(Format.getPrettyFormat()).output(new OpenPackagingFormat().doc,out);
    out.close();
  }


}
