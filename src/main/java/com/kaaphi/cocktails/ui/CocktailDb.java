package com.kaaphi.cocktails.ui;

import com.kaaphi.cocktails.dao.CustomFormatRecipeDao;
import com.kaaphi.cocktails.dao.RecipeDao;
import com.kaaphi.cocktails.domain.AutoTag;
import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.ui.MenuUtil.MenuAction;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

public class CocktailDb extends JSplitPane {
  private SortedListModel<Recipe> cocktailListModel;
  private RecipeEditor editor;
  private RecipeDao dao;
  private JList cocktailList;
  private CocktailAutoCompleteOptions options = new CocktailAutoCompleteOptions();

  public CocktailDb() {
    super(JSplitPane.HORIZONTAL_SPLIT);

    cocktailListModel = new SortedListModel<Recipe>(new Comparator<Recipe>() {
      @Override
      public int compare(Recipe r1, Recipe r2) {
        int c = r1.getReference().compareToIgnoreCase(r2.getReference());
        if(c == 0) {
          c = r1.getReferenceDetail().compareToIgnoreCase(r2.getReferenceDetail());
        }
        return c;

        //return r1.getName().compareToIgnoreCase(r2.getName());
      }
    });
    cocktailList = new JList(cocktailListModel);


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

  private void openFile(File file) {
    try {
      dao = new CustomFormatRecipeDao(file);
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
        if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
          openFile(chooser.getSelectedFile());
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
