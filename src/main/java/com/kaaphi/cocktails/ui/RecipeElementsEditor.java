package com.kaaphi.cocktails.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.kaaphi.cocktails.domain.Recipe;
import com.kaaphi.cocktails.domain.RecipeElement;
import com.kaaphi.cocktails.ui.DirtyListener.DirtySupport;
import com.kaaphi.cocktails.util.Fractions;

public class RecipeElementsEditor extends JPanel implements ModelViewer {
	private List<ElementEditor> editors;
	private Recipe recipe;
	
	private JButton add;
	private JPanel editorPanel;
	private CocktailAutoCompleteOptions options;
	private DirtySupport dirtySupport = new DirtySupport(this);
	
	public RecipeElementsEditor(CocktailAutoCompleteOptions options) {
		super(new BorderLayout());
		
		this.options = options;
		
		editorPanel = new JPanel();
		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
		
		editors = new ArrayList<ElementEditor>();
		
		add = new JButton("Add");
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addEditor(new RecipeElement(recipe)).ingredient.requestFocusInWindow();
				dirtySupport.fireDirtyChanged(true);
			}
		});
		
		setPreferredSize(new Dimension(600,200));
		
		add(new JScrollPane(editorPanel), BorderLayout.CENTER);
		add(add, BorderLayout.SOUTH);
	}
	
	public void addDirtyListener(DirtyListener l) {
		dirtySupport.addDirtyListener(l);
	}
	
	public void removeDirtyListener(DirtyListener l) {
		dirtySupport.removeDirtyListener(l);
	}
		
	public void clearEditors() {
		editors.clear();
		editorPanel.removeAll();
		editorPanel.revalidate();
	}
	
	private ElementEditor addEditor(RecipeElement e) {
		final ElementEditor editor = new ElementEditor(e, options, dirtySupport);
		editor.setMaximumSize(new Dimension(Integer.MAX_VALUE, editor.getPreferredSize().height));

		final JPopupMenu popup = new JPopupMenu();
		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteEditor(editor);
			}
		});

		popup.add(deleteItem);
		editor.add(popup);
		
		editor.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(),
							e.getX(), e.getY());
				}
			}
		});
		
		editors.add(editor);
		editorPanel.add(editor);
		
		
		resizeEditorPanel();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editorPanel.scrollRectToVisible(new Rectangle(new Point(0, editorPanel.getHeight())));
			}
		});
		return editor;
	}

	private void resizeEditorPanel() {
		Component editor = editorPanel.getComponent(0);
		
		int w = editor.getPreferredSize().width;
		int h = editor.getPreferredSize().height * editors.size();
		
		editorPanel.setPreferredSize(new Dimension(w, h));
		editorPanel.revalidate();
	}
	
	private void deleteEditor(ElementEditor editor) {
		editors.remove(editor);
		editorPanel.remove(editor);
		editorPanel.validate();
		
		resizeEditorPanel();
		editorPanel.repaint();
	}
	
	public void setModel(Recipe r) {
		this.recipe = r;
	}

	@Override
	public void modelToView() {
		clearEditors();
		for(RecipeElement e : recipe.getRecipeElements()) {
			addEditor(e).modelToView();
		}
		revalidate();
		repaint();
	}

	@Override
	public void viewToModel() {
		List<RecipeElement> elements = new ArrayList<RecipeElement>(editors.size());
		
		for(ElementEditor e : editors) {
			e.viewToModel();
			elements.add(e.model);
		}
		
		recipe.setRecipeElements(elements);
	}
	
	private static class ElementEditor extends JPanel implements ModelViewer {
		RecipeElement model;
		AutoCompleteJTextField ingredient;
		JTextField amount;
		AutoCompleteJTextField unit;
		JTextField note;
		JCheckBox isBase;
		
		
		public ElementEditor(RecipeElement e, CocktailAutoCompleteOptions options, DirtySupport support) {
			ingredient = new AutoCompleteJTextField(20);
			ingredient.setOptions(options.ingredients);
			ingredient.getDocument().addDocumentListener(support);
			amount = new JTextField(4);
			amount.setHorizontalAlignment(JTextField.RIGHT);
			amount.getDocument().addDocumentListener(support);
			unit = new AutoCompleteJTextField(4);
			unit.setOptions(options.units);
			unit.setMinimumLetters(0);
			unit.getDocument().addDocumentListener(support);
			note = new JTextField(20);
			note.getDocument().addDocumentListener(support);
			isBase = new JCheckBox();
			isBase.addItemListener(support);
			
			
			add(ingredient);
			add(amount);
			add(unit);
			add(note);
			add(isBase);
			
			this.model = e;
			
			SmartQuoteDocumentListener.listen(ingredient, note);
		}
		
		public void modelToView() {
			ingredient.setText(model.getIngredient());
			amount.setText(Fractions.toString(model.getAmount()));
			unit.setText(model.getUnit());
			note.setText(model.getNote());
			isBase.setSelected(model.isBase());
		}
		
		public void viewToModel() {
			model.setIngredient(ingredient.getText());
			model.setAmount(Fractions.toIntArray(amount.getText()));
			model.setUnit(unit.getText());
			model.setNote(note.getText());
			model.setIsBase(isBase.isSelected());
			ingredient.getOptions().addOption(ingredient.getText());
			unit.getOptions().addOption(unit.getText());
		}
	}
}
