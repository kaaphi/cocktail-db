package com.kaaphi.cocktails.dao;

import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomFormatRecipeDao implements RecipeDao {
  private static final int currentVersion = 7;
  private static final Loader[] loaders = new Loader[] {
      null, //no 0 version
      new V1Loader(),
      new V2LoaderUpgrader(),
      new V3LoaderUpgrader(),
      new V4LoaderUpgrader(),
      new V5LoaderUpgrader(),
      new V6LoaderUpgrader(),
      new V7LoaderUpgrader()
  };

  private static final Upgrader[] upgraders = new Upgrader[] {
      null, //no 0 version
      new V2LoaderUpgrader(),
      new V3LoaderUpgrader(),
      new V4LoaderUpgrader(),
      new V5LoaderUpgrader(),
      new V6LoaderUpgrader(),
      new V7LoaderUpgrader()
  };

  private File file;

  public CustomFormatRecipeDao(File file) {
    this.file = file;
  }

  @Override
  public List<Recipe> load() throws Exception {
    InputStream in = new FileInputStream(file);
    try {
      DataInputStream dataIn = new DataInputStream(in);

      int version = dataIn.readInt();
      if(version < 1 || version > currentVersion) {
        throw new Exception("Bad version: " + version);
      }
      int numRecipes = dataIn.readInt();

      List<Recipe> recipes = new ArrayList<Recipe>(numRecipes);
      Map<RecipeData, List<RecipeElementData>> dataMap = new LinkedHashMap<RecipeData, List<RecipeElementData>>();
      for(int i = 0; i < numRecipes; i++) {
        RecipeData rd = new RecipeData();
        loaders[version].loadData(rd, dataIn);
        List<RecipeElementData> reds = new ArrayList<RecipeElementData>(rd.numElements);
        for(int j = 0; j < rd.numElements; j++) {
          RecipeElementData red = new RecipeElementData();
          loaders[version].loadData(red, dataIn);
          reds.add(red);
        }

        dataMap.put(rd, reds);
      }

      for(int i = version; i < currentVersion; i++) {
        upgraders[i].upgrade(dataMap);
      }

      for(Entry<RecipeData, List<RecipeElementData>> entry : dataMap.entrySet()) {
        RecipeData d = entry.getKey();
        Recipe r = new Recipe(d.name, d.instructions, d.reference, d.referenceDetails, d.note, new ArrayList<String>(), d.indexElements, d.isArchived);
        r.setTagString(d.tagString);
        List<RecipeElement> elements = new LinkedList<RecipeElement>();
        for(RecipeElementData e : entry.getValue()) {
          elements.add(new RecipeElement(e.ingredient, e.amount, e.unit, e.note, e.isBase));
        }
        r.setRecipeElements(elements);
        recipes.add(r);
      }

      return recipes;
    } finally {
      in.close();
    }
  }

  @Override
  public void save(Collection<Recipe> recipes) throws Exception {
    OutputStream out = new FileOutputStream(file);
    try {
      DataOutputStream dataOut = new DataOutputStream(out);

      dataOut.writeInt(currentVersion);
      dataOut.writeInt(recipes.size());

      for(Recipe r : recipes) {
        writeRecipe(r, dataOut);
      }
    } finally {
      out.close();
    }
  }

  private static void writeRecipe(Recipe r, DataOutputStream out) throws IOException {
    /* OLD:
		//V1
		out.writeUTF(r.getName());
		out.writeUTF(r.getInstructions());
		out.writeUTF(r.getReference());
		out.writeUTF(r.getNote());
		out.writeInt(r.getRecipeElements().size());

		//V2
		//no changes

		//V3
		out.writeUTF(r.getTagString());

		//V4
		out.writeUTF(r.getReferenceDetail());
     */

    //V5
    out.writeUTF(r.getName());
    out.writeUTF(r.getInstructions());
    out.writeUTF(r.getReference());
    out.writeUTF(r.getNote());
    out.writeUTF(r.getTagString());
    out.writeUTF(r.getReferenceDetail());
    out.writeInt(r.getRecipeElements().size());

    //V6
    out.writeBoolean(r.getIndexElements());	
    
    //V7
    out.writeBoolean(r.isArchived());

    //elements are always last
    for(RecipeElement e : r.getRecipeElements()) {
      writeRecipeElement(e, out);
    }
  }

  private static void writeRecipeElement(RecipeElement e, DataOutputStream out) throws IOException {
    /* OLD:
		//V1
		out.writeUTF(e.getIngredient());
		out.writeDouble(e.getAmount());
		out.writeUTF(e.getUnit());
		out.writeUTF(e.getNote());

		//V2
		out.writeBoolean(e.isBase());

		//V3
		//no changes

		//V4
		//no changes
     */

    //V5
    out.writeUTF(e.getIngredient());
    out.writeInt(e.getAmount()[0]);
    out.writeInt(e.getAmount()[1]);
    out.writeUTF(e.getUnit());
    out.writeUTF(e.getNote());
    out.writeBoolean(e.getIsBase());

    //V6
    //V7
    //no changes
  }

  private static class V1Loader extends SimpleUpgrader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      data.name = in.readUTF();
      data.instructions = in.readUTF();
      data.reference = in.readUTF();
      data.note = in.readUTF();
      data.numElements = in.readInt();
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      data.ingredient = in.readUTF();
      data.preV5amount = in.readDouble();
      data.unit = in.readUTF();
      data.note = in.readUTF();
    }

  }

  private static class V2LoaderUpgrader extends V1Loader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      super.loadData(data, in);			
      //no changes
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      super.loadData(data, in);
      data.isBase = in.readBoolean();
    }

    @Override
    protected void upgrade(RecipeElementData d) throws Exception {
      d.isBase = false;
    }
  }

  private static class V3LoaderUpgrader extends V2LoaderUpgrader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      super.loadData(data, in);	
      data.tagString = in.readUTF();
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      super.loadData(data, in);	
      //no changes
    }

    @Override
    protected void upgrade(RecipeData d) throws Exception {
      d.tagString = "";
    }
  }

  private static class V4LoaderUpgrader extends V3LoaderUpgrader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      super.loadData(data, in);	
      data.referenceDetails = in.readUTF();
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      super.loadData(data, in);	
      //no changes
    }

    @Override
    protected void upgrade(RecipeData d) throws Exception {
      final Pattern p = Pattern.compile("(.*?)(\\s+\\((.*)\\))?");
      Matcher m = p.matcher(d.reference);
      if(m.matches()) {
        d.reference = m.group(1);
        d.referenceDetails = m.group(3);
      }

      if(d.referenceDetails == null) {
        d.referenceDetails = "";
      }
    }
  }

  private static class V5LoaderUpgrader extends SimpleUpgrader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      data.name = in.readUTF();
      data.instructions = in.readUTF();
      data.reference = in.readUTF();
      data.note = in.readUTF();
      data.tagString = in.readUTF();
      data.referenceDetails = in.readUTF();
      data.numElements = in.readInt();
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      data.ingredient = in.readUTF();
      data.amount = new int[2];
      data.amount[0] = in.readInt();
      data.amount[1] = in.readInt();
      data.unit = in.readUTF();
      data.note = in.readUTF();
      data.isBase = in.readBoolean();
    }

    @Override
    protected void upgrade(RecipeElementData d) throws Exception {
      int i = (int)Math.floor(d.preV5amount);
      double decimal = d.preV5amount-i;

      d.amount = new int[2];

      if(isApproxZero(decimal)) {
        d.amount[0] = i;
        d.amount[1] = 1;
      } else {
        boolean b = 
            setIfApproxEqual(d.amount, decimal, 1, 8)
            || setIfApproxEqual(d.amount, decimal, 1, 4)
            || setIfApproxEqual(d.amount, decimal, 1, 3)
            || setIfApproxEqual(d.amount, decimal, 1, 2)
            || setIfApproxEqual(d.amount, decimal, 2, 3)
            || setIfApproxEqual(d.amount, decimal, 3, 4);
        if(!b)
          throw new Exception("No recognized frac!");

        d.amount[0] += d.amount[1] * i;
      }
    }

    private static boolean setIfApproxEqual(int[] frac, double v, int n, int d) {
      if(isApproxZero(v - ((double)n/(double)d))) {
        frac[0] = n;
        frac[1] = d;
        return true;
      } else {
        return false;
      }
    }

    private static boolean isApproxZero(double v) {
      return v < .005;
    }
  }

  private static class V6LoaderUpgrader extends V5LoaderUpgrader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      super.loadData(data, in);	
      data.indexElements = in.readBoolean();
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      super.loadData(data, in);	
      //no changes
    }

    @Override
    protected void upgrade(RecipeData d) throws Exception {
      d.indexElements = true;
    }

    @Override
    protected void upgrade(RecipeElementData d) throws Exception {
      //nothing
    }
  }
  
  private static class V7LoaderUpgrader extends V6LoaderUpgrader implements Loader {
    @Override
    public void loadData(RecipeData data, DataInputStream in) throws IOException {
      super.loadData(data, in); 
      data.isArchived = in.readBoolean();
    }

    @Override
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException {
      super.loadData(data, in); 
      //no changes
    }

    @Override
    protected void upgrade(RecipeData d) throws Exception {
      d.isArchived = false;
    }

    @Override
    protected void upgrade(RecipeElementData d) throws Exception {
      //nothing
    }
  }

  private static class RecipeData {
    String name;
    String instructions;
    String reference;
    String referenceDetails;
    String note;
    String tagString;
    boolean indexElements;
    boolean isArchived;
    int numElements;

    @Override
    public String toString() {
      return reflectToString(RecipeData.class, this);
    }
  }

  private static class RecipeElementData {
    String ingredient;
    double preV5amount;
    String unit;
    String note;
    boolean isBase;
    int[] amount;

    @Override
    public String toString() {
      return reflectToString(RecipeElementData.class, this);
    }
  }

  private static <T> String reflectToString(Class<T> cls, T object) {
    StringBuilder sb = new StringBuilder("[");
    for(Field f : cls.getDeclaredFields()) {
      sb.append(f.getName());
      sb.append("=");
      try {
        sb.append(f.get(object));
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      sb.append(", ");
    }
    sb.append("]");
    return sb.toString();
  }

  private static abstract class SimpleUpgrader implements Upgrader {
    @Override
    public void upgrade(Map<RecipeData, List<RecipeElementData>> dataMap) throws Exception {
      for(Map.Entry<RecipeData, List<RecipeElementData>> e : dataMap.entrySet()) {
        upgrade(e.getKey());
        for(RecipeElementData red : e.getValue()) {
          upgrade(red);
        }
      }
    }

    protected void upgrade(RecipeData d) throws Exception {}
    protected void upgrade(RecipeElementData d) throws Exception {}
  }

  private static interface Loader {
    public void loadData(RecipeData data, DataInputStream in) throws IOException;
    public void loadData(RecipeElementData data, DataInputStream in) throws IOException;
  }

  private static interface Upgrader {
    public void upgrade(Map<RecipeData, List<RecipeElementData>> dataMap) throws Exception;
  }

  public static void main(String[] args) throws Exception {
    //File file = new File("test.dat");
    File file = new File("/home/kaaphi/cocktail_db/db.dat_bak");

    CustomFormatRecipeDao dao = new CustomFormatRecipeDao(file);
    List<Recipe> rs = dao.load();

    for(Recipe r : rs) {
      System.out.println(r);
      System.out.println(r.getRecipeElements());
    }
    //dao.save(rs);

  }
}
