package com.kaaphi.cocktails.ui;

import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.ui.DirtyListener.DirtySupport;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class RecipeEditor extends JPanel implements ModelViewer {
  private Recipe recipe;

  private JTextField name;
  private AutoCompleteJTextField reference;
  private JTextField referenceDetail;
  private JTextArea instructions;
  private JTextArea note;
  private AutoCompleteJTextField tags;
  private JCheckBox dontIndexElements;
  private RecipeElementsEditor elementsEditor;
  private DirtySupport dirtySupport = new DirtySupport(this);
  private CocktailAutoCompleteOptions options;

  public RecipeEditor(CocktailAutoCompleteOptions options) {
    super(new BorderLayout());
    this.options = options;

    name = new JTextField();
    name.getDocument().addDocumentListener(dirtySupport);
    reference = new AutoCompleteJTextField();
    reference.setOptions(options.references);
    reference.getDocument().addDocumentListener(dirtySupport);
    referenceDetail = new JTextField();
    referenceDetail.getDocument().addDocumentListener(dirtySupport);
    instructions = new JTextArea();
    instructions.setRows(3);
    instructions.setLineWrap(true);
    instructions.setWrapStyleWord(true);
    instructions.getDocument().addDocumentListener(dirtySupport);
    useTabForFocus(instructions);
    note = new JTextArea();
    note.getDocument().addDocumentListener(dirtySupport);
    note.setRows(2);
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    useTabForFocus(note);
    tags = new AutoCompleteJTextField();
    tags.setDelimiter(",");
    tags.setOptions(options.tags);
    tags.getDocument().addDocumentListener(dirtySupport);
    dontIndexElements = new JCheckBox("Don't Index Recipe Elements");
    dontIndexElements.addItemListener(dirtySupport);

    elementsEditor = new RecipeElementsEditor(options);
    elementsEditor.addDirtyListener(dirtySupport);

    JPanel additional = new JPanel();
    additional.setLayout(new BoxLayout(additional, BoxLayout.Y_AXIS));

    additional.add(labeledComponent("Reference: ", reference));
    additional.add(labeledComponent("Reference Detail: ", referenceDetail));
    JLabel inst = new JLabel("Instructions:");
    additional.add(inst);
    additional.add(new JScrollPane(instructions));
    additional.add(new JLabel("Note:"));
    additional.add(new JScrollPane(note));
    additional.add(labeledComponent("Tags: ", tags));
    additional.add(dontIndexElements);

    for(Component c : additional.getComponents()) {
      ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    add(labeledComponent("Name: ", name), BorderLayout.NORTH);
    add(elementsEditor, BorderLayout.CENTER);
    add(additional, BorderLayout.SOUTH);		

    SmartQuoteDocumentListener.listen(name, reference, instructions, note, tags);
  }

  public void addDirtyListener(DirtyListener l) {
    dirtySupport.addDirtyListener(l);
  }

  public void removeDirtyListener(DirtyListener l) {
    dirtySupport.removeDirtyListener(l);
  }

  public void setRecipe(Recipe r) {
    this.recipe = r;
  }

  @Override
  public void modelToView() {
    dirtySupport.setSuppressEvents(true);
    name.setText(recipe.getName());
    reference.setText(recipe.getReference());
    referenceDetail.setText(recipe.getReferenceDetail());
    instructions.setText(recipe.getInstructions());
    note.setText(recipe.getNote());
    tags.setText(recipe.getTagString());
    dontIndexElements.setSelected(!recipe.getIndexElements());
    elementsEditor.setModel(recipe);
    elementsEditor.modelToView();
    dirtySupport.setSuppressEvents(false);
    dirtySupport.fireDirtyChanged(false);
  }

  @Override
  public void viewToModel() {
    recipe.setName(name.getText());
    recipe.setReference(reference.getText());
    recipe.setReferenceDetail(referenceDetail.getText());
    recipe.setInstructions(instructions.getText());
    recipe.setNote(note.getText());
    recipe.setTagsFromString(tags.getText());
    recipe.setIndexElements(!dontIndexElements.isSelected());
    elementsEditor.viewToModel();

    options.references.addOption(recipe.getReference());
    options.tags.addOptions(recipe.getTags());

    dirtySupport.fireDirtyChanged(false);
  }


  public void setAllEnabled(boolean flag) {
    setAllEnabled(this, flag);
  }

  private static void setAllEnabled(Component component, boolean flag) {
    component.setEnabled(flag);
    if(component instanceof Container) {
      for(Component c : ((Container)component).getComponents()) {
        setAllEnabled(c, flag);
      }
    }
  }

  /**
   * Patch the behaviour of a component. 
   * TAB transfers focus to the next focusable component,
   * SHIFT+TAB transfers focus to the previous focusable component.
   * 
   * @param c The component to be patched.
   */
  private static void useTabForFocus(Component c) {
    Set<KeyStroke> 
    strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
    c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
    strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
    c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);
  }

  private static JPanel labeledComponent(String text, Component c) {
    JPanel panel = new JPanel(new BorderLayout());
    JLabel l = new JLabel(text);
    l.setLabelFor(c);
    panel.add(l, BorderLayout.WEST);
    panel.add(c, BorderLayout.CENTER);

    return panel;
  }

}
