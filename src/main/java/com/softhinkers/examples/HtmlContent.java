package com.softhinkers.examples;

import java.awt.Container;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;


public class HtmlContent extends JFrame{

	public static void main(String args[]) { new HtmlContent().start(); }

	private void start() {
	
		try{
			 Container contentPane = getContentPane();
			String html;
			html="<html><head><title>Simple Page</title></head>";
			html+="<body bgcolor='#777779'><hr/><font size=50>This is Html content</font><hr/>";
			html+="</body></html>";
			URL u=new URL("http://www.softhinkers.com");
			//JEditorPane ed1=new JEditorPane("text/html",html);
			JEditorPane ed1=new JEditorPane(u);
			ed1.setContentType("text/html");
			JScrollPane jsp =
                    new JScrollPane( ed1,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
            contentPane.add( jsp );
			//add(ed1);
			setVisible(true);
			setSize(600,600);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Some problem has occured"+e.getMessage());
		}
		
	}

}
