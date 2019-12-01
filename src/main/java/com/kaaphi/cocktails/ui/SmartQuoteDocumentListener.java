package com.kaaphi.cocktails.ui;

import java.awt.Font;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


public class SmartQuoteDocumentListener implements DocumentListener {
	private static final String SINGLE_QUOTE = "'";
	private static final String LEFT_SINGLE_QUOTE = "\u2018";
	private static final String RIGHT_SINGLE_QUOTE = "\u2019";
	private static final String DOUBLE_QUOTE = "\"";
	private static final String LEFT_DOUBLE_QUOTE = "\u201C";
	private static final String RIGHT_DOUBLE_QUOTE = "\u201D";
	
	private static class QuotifierThread extends Thread {
		private BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		//private WeakReference<JTextComponent> c;
		private JTextComponent c;
		private Document d;

		public QuotifierThread(JTextComponent c) {
			setDaemon(true);
			//this.c = new WeakReference<JTextComponent>(c);
			this.c = c;
			this.d = c.getDocument();
		}

		public void run() {
			try {
				while(true) {
					int offset = queue.take();

					if(SINGLE_QUOTE.equals(d.getText(offset, 1))) {
						fixQuote(offset, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE);
					}

					else if(DOUBLE_QUOTE.equals(d.getText(offset, 1))) {
						fixQuote(offset, LEFT_DOUBLE_QUOTE, RIGHT_DOUBLE_QUOTE);						
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void fixQuote(int offset, String left, String right) throws BadLocationException {
			if(offset == 0 || d.getText(offset-1, 1).matches("\\s")) {
				replace(offset, left);
			}
			
			else if(offset > 0 && d.getText(offset-1, 1).matches("\\w")) {
				replace(offset, right);
			}
		}
		
		private void replace(int offset, String newText) throws BadLocationException {
			d.remove(offset, 1);
			d.insertString(offset, newText, null);
			c.setCaretPosition(offset+1);
		}
		
		public void addOffset(int offset) {
			queue.add(offset);
		}
	}
	
	
	public static void listen(JTextComponent... components) {
		for(JTextComponent c : components)
			new SmartQuoteDocumentListener(c);
	}
	
	private QuotifierThread thread;
	
	private SmartQuoteDocumentListener(JTextComponent c) {
		thread = new QuotifierThread(c);
		thread.start();
		
		c.getDocument().addDocumentListener(this);
	}
	
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		if(Thread.currentThread() == thread) {
			return;
		}
		
		int offset = e.getOffset();
		int length = e.getLength();
		
		Document d = e.getDocument();
		
		
		try {
			String text = d.getText(offset, length);
			if(SINGLE_QUOTE.equals(text) || DOUBLE_QUOTE.equals(text)) {
				thread.addOffset(offset);	
			}
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void changedUpdate(DocumentEvent e) {
		//noop
	}

	public void removeUpdate(DocumentEvent e) {
		//noop
	}
	
	
	
	public static void main(String[] args) {
		JFrame f = new JFrame("Test");
		JTextField field = new JTextField(20);
		field.setFont(new Font("serif", Font.PLAIN, 20));
		SmartQuoteDocumentListener.listen(field);
		f.getContentPane().add(field);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}

}
