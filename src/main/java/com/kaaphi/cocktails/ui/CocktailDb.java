package com.kaaphi.cocktails.ui;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.kaaphi.cocktails.dao.CustomFormatRecipeDao;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.AutoTag;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.ui.MenuUtil.MenuAction;
import com.kaaphi.cocktails.web.MongoRecipeDaoModule;
import com.kaaphi.cocktails.web.data.RecipeDataWatcher;
import com.kaaphi.cocktails.web.data.mongo.MongoRecipeDao;
import com.kaaphi.cocktails.web.data.mongo.MongoRecipeDataWatcher;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.bson.BsonDateTime;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

public class CocktailDb extends JSplitPane {
  private SortedListModel<Recipe> cocktailListModel;
  private RecipeEditor editor;
  private RecipeDao dao;
  private JList<Recipe> cocktailList;
  private CocktailAutoCompleteOptions options = new CocktailAutoCompleteOptions();
  
  private static final Comparator<Recipe> RECIPE_ORDER = (a,b) -> {
    if(a.isArchived() == b.isArchived()) {
      return a.getName().compareTo(b.getName());
    } else if (a.isArchived()) {
      return 1;
    } else {
      return -1;
    }        
  };

  public CocktailDb() {
    super(JSplitPane.HORIZONTAL_SPLIT);

    cocktailListModel = new SortedListModel<Recipe>(RECIPE_ORDER);
    cocktailList = new JList<>(cocktailListModel);


    JPanel editorPanel = new JPanel(new BorderLayout());
    editor = new RecipeEditor(options);

    final JButton save = new JButton("Save");
    save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        editor.viewToModel();
      }
    });
    final JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        editor.modelToView();
      }
    });
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(save);
    buttonPanel.add(cancel);

    editor.addDirtyListener(new DirtyListener() {
      @Override
      public void dirtyStateChange(Object source, boolean isDirty) {
        save.setEnabled(isDirty);
        cancel.setEnabled(isDirty);
      }
    });

    editorPanel.add(editor, BorderLayout.CENTER);
    editorPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(new JScrollPane(cocktailList));
    add(editorPanel);

    editor.setAllEnabled(false);
    save.setEnabled(false);
    cancel.setEnabled(false);

    cocktailList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          Recipe r = (Recipe)cocktailList.getSelectedValue();
          boolean flag = r != null;
          editor.setAllEnabled(flag);
          if(flag) {
            editor.setRecipe(r);
            editor.modelToView();
          }

        }
      }
    });
  }

  public void showFrame() {
    JFrame frame = new JFrame("CocktailDb");

    createMenu(frame);

    frame.getContentPane().add(this);
    frame.pack();

    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  private void save(File file) {
    dao = new CustomFormatRecipeDao(file);
    save();
  }

  private void save() {
    try {
      List<Recipe> r = new ArrayList<Recipe>();
      for(Recipe recipe : cocktailListModel) {
        r.add(recipe);
      }
      dao.save(r);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  private void openMongo(String connectionString) {
    Injector injector = Guice.createInjector(
        new MongoRecipeDaoModule(),
        binder -> binder.bind(String.class)
          .annotatedWith(Names.named("mongo.connectionString")).toInstance(connectionString)
      );
    dao = injector.getInstance(RecipeDao.class);
    injector.getInstance(RecipeDataWatcher.class).addListener(() -> {

    });

    open();
  }

  private void openFile(File file) {
      dao = new CustomFormatRecipeDao(file);
      open();
  }

  private void open() {
    try {
    List<Recipe> recipes = dao.load();

    cocktailListModel.clearObjects();
    options.ingredients.clearOptions();
    options.units.clearOptions();
    for(Recipe r : recipes) {
      cocktailListModel.addObject(r);
      options.references.addOption(r.getReference());
      options.tags.addOptions(r.getTags());
      for(RecipeElement e : r.getRecipeElements()) {
        options.ingredients.addOption(e.getIngredient());
        options.units.addOption(e.getUnit());
      }
    }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createMenu(final JFrame frame) {
    MenuUtil menu = new MenuUtil();

    final JFileChooser chooser = new JFileChooser(".");

    menu.createMenu("&File",
        new MenuAction("&Open", "O") {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
              openFile(chooser.getSelectedFile());
            }
          }
        },
        new MenuAction("Open Mongo") {
          @Override
          public void actionPerformed(ActionEvent e) {
            String connectionString = JOptionPane.showInputDialog(frame, "Enter connection string:");
            if(connectionString != null) {
              openMongo(connectionString);
            }
          }
        },
    new MenuAction("&Save", "S") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(dao == null) {
          if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            save(chooser.getSelectedFile());
          }
        } else {
          save();
        }
      }
    });

    menu.createMenu("&Cocktails",
        new MenuAction("&Add...", 
            KeyStroke.getKeyStroke(
                KeyEvent.VK_A,
                InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK)) {
      @Override
      public void actionPerformed(ActionEvent e) {
        String name = JOptionPane.showInputDialog(CocktailDb.this, "Name:");
        if(name != null) {
          Recipe r = new Recipe(name);
          cocktailListModel.addObject(r);
          cocktailList.setSelectedValue(r, true);
        }
      }
    },
        new MenuAction("Auto &Tag...") {
      @Override
      public void actionPerformed(ActionEvent e) {
        for(Recipe recipe : cocktailListModel) {
          AutoTag.autoTag(recipe);
        }
      }
    });

    menu.installOn(frame);
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    new CocktailDb().showFrame();
  }

}
