package com.kaaphi.cocktails.ui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;

public class AutoCompleteJTextField extends JTextField {
	private AutoCompleteOptions options = new AutoCompleteOptions();
	private boolean changing = false;
	private int letters = 1;
	private String delimiter;
	
	public AutoCompleteJTextField() {
		super();
		init();
	}

	public AutoCompleteJTextField(int columns) {
		super(columns);
		init();
	}

	public AutoCompleteJTextField(String text, int columns) {
		super(text, columns);
		init();
	}

	public AutoCompleteJTextField(String text) {
		super(text);
		init();
	}
	
	public void setOptions(AutoCompleteOptions options) {
		this.options = options;
	}
	
	public AutoCompleteOptions getOptions() {
		return options;
	}
	
	public void setMinimumLetters(int letters) {
		this.letters = letters;
	}
	
	public void setDelimiter(String d) {
		this.delimiter = d;
	}
	
	private void init() {
		getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				//updateTextAndSelection(e);
			}
			public void insertUpdate(DocumentEvent e) {
				updateTextAndSelection(e);
			}
			public void changedUpdate(DocumentEvent e) {
				//noop
			}
		});
		
		addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				select(0, 0);
			}
		});
		
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					Caret c = getCaret();
					if(c.getMark() != c.getDot()) {
						c.setDot(c.getDot());
					}
				}
			}
		});
	}
	
	private void updateTextAndSelection(DocumentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String totalText = getText();

				int caret = getCaretPosition();
				int startDelimiter = 0;
				if(delimiter != null) {
					int nextLocation;
					while((nextLocation = totalText.indexOf(delimiter, startDelimiter)) < caret && nextLocation >= 0) {
						//System.out.format("loc: %d (caret: %d)%n", nextLocation, caret);
						startDelimiter = nextLocation+1;
					}
				}
				
				String typed = totalText.substring(startDelimiter, caret).replaceFirst("\\s*", "");
				//System.out.format("typed: <%s>%n", typed);
					
				
				//String typed = totalText;
				if(!changing) {
					if(typed.trim().length() > letters) {
						changing = true;
						String text = options.getCurrentMatch(typed);
						if(text != null) {
							//System.out.format("total: <%s>%n", totalText);
							String b = totalText.substring(0, caret-typed.length());
							String c = totalText.substring(caret);
							String newText = b + text + c;
							//System.out.format("new: <%s><%s><%s>%n", b, text, c);
							setText(newText);
							setSelectionStart(caret);
							setSelectionEnd(caret + text.length() - typed.length());
						}

						changing = false;
					}
				}
			}
		});
	}

	public static class AutoCompleteOptions {
		private List<Option> options;
		
		private static class Option implements Comparable<Option> {
			String str;
			int count;
			
			public Option(String s) {
				str = s;
				count = 1;
			}
			
			@Override
			public int compareTo(Option that) {
				return this.str.compareTo(that.str);
			}
		}
		
		public AutoCompleteOptions(String...strings) {
			this(Arrays.asList(strings));
		}
		
		public AutoCompleteOptions(List<String> strings) {
			options = new ArrayList<Option>(strings.size());
			for(String s : strings) {
				addOption(s);
			}
		}
		
		public void clearOptions() {
			options.clear();
		}
		
		public void addOption(String text) {
			Option o = new Option(text);
			int i = Collections.binarySearch(options, o);
			if(i < 0) {
				options.add(-i-1, o);
			} else {
				options.get(i).count++;
			}
		}
		
		public void addOptions(Collection<String> options) {
			for(String o : options) {
				addOption(o);
			}
		}
		
		public String getCurrentMatch(String text) {
			int val = Collections.binarySearch(options, new Option(text));
			if(val < 0) {
				int i = -val - 1;
				int maxI = -1;
				int maxCount = 0;
				while(i < options.size() && options.get(i).str.startsWith(text)) {
					if(options.get(i).count > maxCount) {
						maxI = i;
						maxCount = options.get(i).count;
					}
					i++;
				}
				
				if(maxCount > 0)
					return options.get(maxI).str;
			}
			
			return null;
		}
	}

	
	public static void main(String[] args) {
		
		String[] options = new String[] {
			"Rum",
			"Bourbon",
			"Rye Whiskey",
			"Gin",
			"Vodka",
			"Angostura Bitters",
			"Orange Bitters",
			"Sweet Vermouth",
			"Sweet Vermouth",
			"Dry Vermouth",
			"Dry Vermouth",
			"Dry Sherry",
			"Dry Vermouth",
			"Lillet Blanc",
			"Brandy"
		};
		
		JFrame f = new JFrame("Test");
		AutoCompleteJTextField acf = new AutoCompleteJTextField(20);
		acf.setDelimiter(",");
		acf.setOptions(new AutoCompleteOptions(options));
		f.getContentPane().add(acf);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
		
	}
}
