package com.kaaphi.opf;

import com.kaaphi.cocktails.dao.CustomFormatRecipeDao;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.util.Fractions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class EbookOutputter {
  //private static final Namespace OPF = Namespace.getNamespace("http://www.idpf.org/2007/opf");
  private static final Namespace SUB_OPF = Namespace.getNamespace("opf", "http://www.idpf.org/2007/opf");
  private static final Namespace DC = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
  private static final Namespace NCX = Namespace.getNamespace("http://www.daisy.org/z3986/2005/ncx/");

  //these namespaces are fake
  private static final Namespace MBP = Namespace.getNamespace("mbp", "http://mobipocket.com/mbp");
  private static final Namespace IDX = Namespace.getNamespace("idx", "http://mobipocket.com/idx");

  private List<Recipe> recipes;
  private String title;
  private String author;

  public EbookOutputter(String title, String author, List<Recipe> recipes) {
    this.recipes = new ArrayList<Recipe>(recipes);
    this.title = title;
    this.author = author;

    Collections.sort(this.recipes, new Comparator<Recipe>() {
      @Override
      public int compare(Recipe o1, Recipe o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
  }

  private Document createNcx() {
    Element root = (new Element("ncx", NCX).setAttribute("version", "2005-1"
        ).addContent(new Element("head", NCX).
            addContent(new Element("meta", NCX).
                setAttribute("name", "dtb:uid").
                setAttribute("content", "BookId"))
            ).addContent(new Element("docTitle", NCX).
                addContent(new Element("text", NCX).setText(title))
                ).addContent(new Element("docAuthor", NCX).
                    addContent(new Element("text", NCX).setText(author)))
        );



    return null;
  }

  private static Element makeNavPoint(String label, String id, String content, int playOrder) {
    return (
        new Element("navPoint", NCX
            ).addContent(
                new Element("navLabel", NCX).
                addContent(new Element("text", NCX).setText(label))
                ).addContent(
                    new Element("content", NCX).setAttribute("src", content)
                    ).setAttribute("class", id
                        ).setAttribute("id", id
                            ).setAttribute("playOrder", playOrder+"")
        );
  }

  private Document createOpfDoc() {
    Document doc = new Document();
    doc.setRootElement(new Element("package"));
    Element root = doc.getRootElement();

    root.setAttribute("unique-identifier", "BookId");
    root.addNamespaceDeclaration(DC);

    Element metadata = new Element("metadata");
    root.addContent(metadata);

    Element titleE = new Element("title", DC);
    titleE.setText(title);

    Element language = new Element("language", DC);
    language.setText("en");

    Element creator = new Element("creator", DC);
    creator.setText(author);
    creator.setAttribute(new Attribute("file-as", "Kaasa, Phil"));
    creator.setAttribute(new Attribute("role", "edt"));

    Element identifier = new Element("identifier", DC);
    identifier.setAttribute(new Attribute("id", "BookId"));
    identifier.setText(title);

    metadata.addContent(
        new Element("dc-metadata")
        .addContent(titleE)
        .addContent(language)
        .addContent(creator)
        .addContent(identifier)
        );

    metadata.addContent(
        new Element("x-metadata")
        .addContent(new Element("DictionaryInLanguage").setText("en-us"))
        .addContent(new Element("DictionaryOutLanguage").setText("en-us"))
        );

    metadata.addContent(
        new Element("meta").setAttribute("name", "cover").setAttribute("content", "my_cover")
        );

    Element manifest = new Element("manifest");
    root.addContent(manifest);

    Element content = new Element("item");
    content.setAttribute("id", "content");
    content.setAttribute("href", "content.xhtml");
    content.setAttribute("media-type", "text/x-oeb1-document");

    Element toc = new Element("item");
    toc.setAttribute("id", "toc");
    toc.setAttribute("href", "toc.html");
    toc.setAttribute("media-type", "text/x-oeb1-document");

    Element cover = new Element("item");
    cover.setAttribute("id", "my_cover");
    cover.setAttribute("href", "cover.png");
    cover.setAttribute("media-type", "image/png");


    manifest.addContent(content);
    manifest.addContent(toc);
    manifest.addContent(cover);

    Element spine = new Element("spine");
    root.addContent(spine);

    Element itemref = new Element("itemref");
    itemref.setAttribute("idref", "content");
    spine.addContent(itemref);

    Element guide = new Element("guide");
    root.addContent(guide);

    guide
    .addContent(
        new Element("reference")
        .setAttribute("type", "search")
        .setAttribute("title", "Dictionary Search")
        .setAttribute("onclick", "index_search()")
        )
    .addContent(
        new Element("reference")
        .setAttribute("type", "toc")
        .setAttribute("title", "Table of Contents")
        .setAttribute("href", "toc.html")
        )
    ;
    return doc;
  }


  private Document createTocDoc() {
    Element body;
    Element root = (new Element("html").
        addContent(new Element("head")
            .addContent(textElement("title", "Table of Contents"))
            )
        .addContent(body = new Element("body"))		
        );

    body
    .addContent(new Element("h3").addContent(
        linkElement("content.xhtml#recipes", "Recipes")))
    .addContent(new Element("h3").addContent(
        linkElement("content.xhtml#alpha_index", "Alphabetical Index")))
    .addContent(new Element("h3").addContent(
        linkElement("content.xhtml#base_index", "Base Ingredient Index")))
    .addContent(new Element("h3").addContent(
        linkElement("content.xhtml#tag_index", "Tag Index")))
    .addContent(new Element("h3").addContent(
        linkElement("content.xhtml#all_ingredient_index", "All Ingredient Index")))
    ;

    return new Document(root);		
  }

  private Document createContentDoc() {
    Element body;
    Element root = (new Element("html").
        addContent(new Element("head")
            .addContent(textElement("title", title))
            .addContent(
                new Element("style")
                .setAttribute("type", "text/css")
                .setText(
                    "p {" +
                        "margin-top: 0em;" +
                        "margin-bottom: 1em;" +
                        "text-indent: 0em" +
                        "} " +
                        "h4 {" +
                        "margin-top: 1em" +
                        "}"
                    )
                )).
        addContent(body = new Element("body"))
        );

    root.addNamespaceDeclaration(MBP);
    root.addNamespaceDeclaration(IDX);

    body.addContent(new Element("pagebreak", MBP));
    body.addContent(body = new Element("frameset", MBP));

    body
    .addContent(
        new Element("slave-frame", MBP)
        .setAttribute("display", "bottom")
        .setAttribute("device", "all")
        .setAttribute("breadth", "auto")
        .setAttribute("leftmargin", "0")
        .setAttribute("rightmargin", "0")
        .setAttribute("bottommargin", "0")
        .setAttribute("topmargin", "0")
        .addContent(
            new Element("div")
            .setAttribute("align", "center")
            .setAttribute("bgcolor","yellow")
            .addContent(
                new Element("a")
                .setText("Dictionary Search")
                .setAttribute("onclick", "index_search()")
                )
            )
        )
    .addContent(new Element("pagebreak", MBP));

    Element section = new Element("section", MBP).setAttribute("id", "recipes");
    body.addContent(section);

    //Element section = body;

    for(Recipe r : recipes) {
      Element div = new Element("entry", IDX)
          .setAttribute("name", "word")
          .setAttribute("scriptable", "yes");
      section.addContent(div);
      div.addContent(
          new Element("h2")
          .addContent(new Element("orth", IDX).setText(r.getName()))
          //.addContent(new Element("key", IDX).setAttribute("key", r.getName()))
          .setAttribute("id", "recipe_"+r.getName())					
          );

      Element ingredients = new Element("table");
      div.addContent(ingredients);

      for(RecipeElement e : r.getRecipeElements()) {
        //Element i = textElement("li", String.format("%s %s %s", formatAmount(e.getAmount()), e.getUnit(), e.getIngredient()));
        Element tr = new Element("tr");

        Element iTd;
        tr
        .addContent(
            new Element("td")
            .addContent(formatAmount(e.getAmount()))
            .addContent(new Element("small").addContent(" "+e.getUnit()))
            )
        .addContent(iTd = new Element("td").setText(e.getIngredient()));
        ;
        if(isNullOrEmpty(e.getNote())) {
          //ingredients.addContent(new Element("span"));
          //tr.addContent(new Element("td"));
        } else {
          iTd.addContent(" ").addContent(new Element("small").addContent(new Element("i").setText(String.format("(%s)", e.getNote()))));
        }
        ingredients.addContent(tr);


      }

      div.addContent(new Element("br"));

      div.addContent(textElement("p", r.getInstructions()));

      if(!isNullOrEmpty(r.getNote())) {
        div.addContent(
            new Element("p")
            .addContent(
                new Element("small").addContent(new Element("b")
                    .addContent("NOTE: "))
                )
            .addContent(r.getNote())
            );
      }

      if(!isNullOrEmpty(r.getReferenceWithDetail())) {
        Element ref = new Element("p");
        ref
        .addContent(
            new Element("small").addContent(new Element("b")
                .addContent("REFERENCE: "))
            )
        .addContent(new Element("i").addContent(r.getReference()))
        .addContent(isNullOrEmpty(
            r.getReferenceDetail()) ? new Text("") : 
              new Element("small").addContent(" (" + r.getReferenceDetail() + ")")
            );
        div.addContent(ref);
      }

      if(!r.getTags().isEmpty()) {
        Element tags;
        div.addContent(new Element("p")
            .addContent(new Element("i")
                .addContent(tags = new Element("small")
                .addContent("tags: "))
                ));

        Iterator<String> it = r.getTags().iterator();


        String tag = it.next();
        tags.addContent(linkElement("#tag_index_" + tag, tag));

        while(it.hasNext()) {
          tag = it.next();
          tags.addContent(", ");
          tags.addContent(linkElement("#tag_index_" + tag, tag));
        }
      }


      section.addContent(new Element("pagebreak", MBP));
    }

    body.addContent(createIndex("Alphabetical Index", "alpha_index", 
        new IndexGenerator<String>(false, new Indexer<String>() {
          @Override
          public String getIndexEntryTitle(String obj) {
            return obj.substring(0,1);
          }

          @Override
          public void loopElements(Map<String, Element> map,
              List<Recipe> recipes) {
            for(Recipe r : recipes) {
              processObject(map, r, r.getName());
            }
          }

        }))
        );

    body.addContent(createIndex("Base Ingredient Index", "base_index", 
        new IndexGenerator<RecipeElement>(true, new Indexer<RecipeElement>() {
          @Override
          public String getIndexEntryTitle(RecipeElement obj) {
            return obj.getIngredient();
          }

          @Override
          protected List<RecipeElement> getExcludedElements(Recipe r, RecipeElement obj) {
            return Arrays.asList(obj);
          }

          @Override
          public void loopElements(Map<String, Element> map,
              List<Recipe> recipes) {
            for(Recipe r : recipes) {
              for(RecipeElement e : r.getBaseSpirits()) {
                processObject(map, r, e);
              }
            }
          }
        }))
        );

    body.addContent(createIndex("Tag Index", "tag_index", 
        new IndexGenerator<String>(true, new Indexer<String>() {
          @Override
          public String getIndexEntryTitle(String obj) {
            return obj;
          }

          @Override
          public void loopElements(Map<String, Element> map,
              List<Recipe> recipes) {
            for(Recipe r : recipes) {
              for(String tag : r.getTags()) {
                processObject(map, r, tag);
              }
            }
          }
        }))
        );

    body.addContent(createIndex("All Ingredient Index", "all_ingredient_index", 
        new IndexGenerator<RecipeElement>(true, new Indexer<RecipeElement>() {
          private Set<String> recipeNames;

          @Override
          public String getIndexEntryTitle(RecipeElement obj) {
            if(recipeNames.contains(obj.getIngredient())) {
              return obj.getIngredient() + " (ingredient)";
            } else {
              return obj.getIngredient();
            }
          }

          @Override
          protected List<RecipeElement> getExcludedElements(Recipe r, RecipeElement obj) {
            return Arrays.asList(obj);
          }

          @Override
          public Element makeIndexSectionElement(String title) {
            return new Element("div").addContent(new Element("entry", IDX).setAttribute("name", "word").setAttribute("scriptable", "yes")
                .addContent(new Element("h4")
                    .addContent(new Element("orth", IDX).addContent(title))
                    /*
							.addContent(new Element("key", IDX).setAttribute("key", "i " + title))
                     */
                    ));
          }


          @Override
          public void loopElements(Map<String, Element> map,
              List<Recipe> recipes) {
            Map<String, List<RecipeElement>> elementMap = new HashMap<String, List<RecipeElement>>();

            recipeNames = new HashSet<String>();

            for(Recipe r : recipes) {
              recipeNames.add(r.getName());
              if(r.getIndexElements()) {
                for(RecipeElement e : r.getRecipeElements()) {
                  List<RecipeElement> elements = elementMap.get(e.getIngredient());
                  if(elements == null) {
                    elementMap.put(e.getIngredient(), elements = new ArrayList<RecipeElement>());
                  }

                  elements.add(e);
                }
              }
            }

            for(Entry<String, List<RecipeElement>> e : elementMap.entrySet()) {
              for(RecipeElement re : e.getValue()) {
                processObject(map, re.getRecipe(), re);
              }
            }
          }
        }))
        );

    return new Document(root);
  }

  private Element createIndex(String title, String name, IndexGenerator<?> generator) {
    Element section = new Element("section", MBP).setAttribute("id", name);
    section.addContent(textElement("h1", title));

    generator.addIndex(section, recipes, name);

    return section;
  }

  public void outputBook() throws IOException {
    File opf = new File("test_opf/test.opf");
    File content = new File("test_opf/content.xhtml");
    File toc = new File("test_opf/toc.html");

    XMLOutputter out = new XMLOutputter(Format.getRawFormat());
    output(out, createOpfDoc(), opf);
    output(out, createContentDoc(), content);
    output(out, createTocDoc(), toc);
  }

  private void output(XMLOutputter out, Document doc, File file) throws IOException {
    FileOutputStream os = new FileOutputStream(file);
    try {
      out.output(doc, os);
    } finally {
      os.close();
    }
  }

  private static boolean isNullOrEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  private static List<Content> formatAmount(int[] a) {
    List<Content> content = new LinkedList<Content>();

    String[] parts = Fractions.toStringParts(a);
    int i = 0;
    if(parts.length == 0) {
      content.add(new Text(""));
      return content;
    }
    
    if(parts.length != 2) {
      content.add(new Text(parts[i++]));
    }

    if(parts.length != 1) {
      content.add(makeFraction(parts[i++], parts[i]));
    }

    return content;
  }

  private static Content makeFraction(String n, String d) {
    if("1".equals(n) && "4".equals(d)) {
      return new Text("\u00BC");
    }

    if("1".equals(n) && "2".equals(d)) {
      return new Text("\u00BD");
    }

    if("3".equals(n) && "4".equals(d)) {
      return new Text("\u00BE");
    }


    return new Element("small")
        .addContent(new Element("sup").setText(n))
        .addContent("\u2044")
        .addContent(new Element("sub").setText(d));
  }

  private static boolean append(double decimal, double compare, List<Content> content, Content frac) {
    if(isApproxZero(decimal - compare)) {
      content.add(frac);
      return true;
    } 
    return false;
  }

  private static boolean isApproxZero(double d) {
    return d < .005;
  }

  private static Element textElement(String name, String text) {
    return new Element(name).setText(text);
  }

  private static Element linkElement(String href, String text) {
    return new Element("a").setAttribute("href", href).setText(text);
  }

  private static abstract class Indexer<T> {
    public abstract String getIndexEntryTitle(T obj);
    public abstract void loopElements(Map<String, Element> map, List<Recipe> recipes);

    public Element makeIndexSectionElement(String title) {
      return new Element("div").addContent(textElement("h4", title));
    }

    public void addIndexEntry(Element e, Recipe r, T obj) {
      e.addContent(linkElement("#recipe_"+r.getName(), r.getName()));
      e.addContent(new Element("small").addContent(createOtherElementsString(r, getExcludedElements(r, obj))));
      e.addContent(new Element("br"));
    }

    protected List<RecipeElement> getExcludedElements(Recipe r, T obj) {
      return Collections.emptyList();
    }

    private String createOtherElementsString(Recipe r,
        List<RecipeElement> obj) {
      List<RecipeElement> elements = new ArrayList<RecipeElement>();
      int i = 0;
      List<RecipeElement> toIterate = new ArrayList<RecipeElement>(r.getRecipeElements());
      toIterate.removeAll(obj);
      for(RecipeElement re : toIterate) {
        if(++i > 3)
          break;
        elements.add(re);
      }

      Iterator<RecipeElement> it = elements.iterator();

      if(it.hasNext()) {
        StringBuilder sb = new StringBuilder(" (");
        sb.append(it.next().getIngredient());
        while(it.hasNext()) {
          sb.append(", ").append(it.next().getIngredient());
        }
        if(i > 3) {
          sb.append(",\u2026");
        }
        sb.append(")");

        return sb.toString();
      } else {
        return "";
      }
    }

    protected final void processObject(Map<String, Element> map, Recipe r, T obj) {
      String title = getIndexEntryTitle(obj);
      Element e = map.get(title);
      if(e == null) {
        map.put(title, e = makeIndexSectionElement(title));
      }

      addIndexEntry(e, r, obj);
    }
  }

  private static class IndexGenerator<T> {
    private boolean indexListNewLines;
    private Indexer<T> indexer;

    public IndexGenerator(boolean indexListNewLines, Indexer<T> indexer) {
      this.indexListNewLines = indexListNewLines;
      this.indexer = indexer;
    }

    public void addIndex(Element parent, List<Recipe> recipes, String indexName) {
      Map<String, Element> indexMap = generateIndex(recipes);

      Set<String> indexSet = new TreeSet<String>(indexMap.keySet());
      for(String indexEntry : indexSet) {
        parent.addContent(linkElement("#" + indexName + "_" +indexEntry, indexEntry));
        if(indexListNewLines)
          parent.addContent(new Element("br"));
        else {
          parent.addContent(" ");
        }
      }

      parent.addContent(new Element("hr"));

      for(String indexEntry : indexSet) {
        Element entry = indexMap.get(indexEntry);
        entry.setAttribute("id", indexName+"_"+indexEntry);
        parent.addContent(entry);
      }
    }

    private Map<String, Element> generateIndex(List<Recipe> recipes) {
      Map<String, Element> map = new HashMap<String, Element>();
      indexer.loopElements(map, recipes);
      return map;
    }



  }

  public static void main(String[] args) throws Exception {
    RecipeDao dao = new CustomFormatRecipeDao(new File("/home/kaaphi/cocktail_db/db.dat"));
    //IRecipeDao dao = new CustomFormatRecipeDao(new File("db.dat"));
    EbookOutputter out = new EbookOutputter("Cocktail Compendium", "Phil Kaasa", dao.load());
    out.outputBook();
  }
}
