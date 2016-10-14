package com.softhinkers.galsen.server.inspector;

import org.json.JSONException;

import com.softhinkers.galsen.server.http.HttpRequest;

public abstract class BaseInspectorViewRenderer {
	public String buildHtml(HttpRequest request) throws JSONException {
		StringBuilder b = new StringBuilder();

		appendLine(b, "<!DOCTYPE html>");
		appendLine(b, "<html>");
		appendLine(b, "<head>");
		appendLine(b, "<title>Galsen Inspector</title>");
		appendLine(b, "<link rel=\"stylesheet\" href='"
				+ getResource("inspector.css") + "' type='text/css'/>");

		appendLine(b, "<link rel=\"stylesheet\" href='"
				+ getResource("ide.css") + "' type='text/css'/>");

		appendLine(b, "<link rel=\"stylesheet\" href='"
				+ getResource("jquery.layout.css") + "' type='text/css'/>");

		appendLine(b, "<link rel=\"stylesheet\" href='"
				+ getResource("jquery-ui.css") + "' type='text/css'/>");

		appendLine(b, "<link rel=\"stylesheet\" href='"
				+ getResource("prettify.css") + "' type='text/css'/>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("jquery-1.9.1.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("jquery-ui-1.10.2.min.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("inspector1.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("jquery.jstree.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("jquery.xpath.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("prettify.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("Logger.js") + "'></script>");
		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("Recorder.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("inspector.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("ide.js") + "'></script>");
		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("uiactions.js") + "'></script>");

		appendLine(b, "<script type='text/javascript' src='"
				+ getResource("jquery.layout1.3.js") + "'></script>");

		appendLine(b, "<script>");
		appendLine(b, "jQuery(window).load(function () {");
		appendLine(b, "  console.log('before calling resize()');");
		appendLine(b, "  resize();");
		appendLine(b, "});");
		appendLine(b, "</script>");

		appendLine(b, "</head>");
		appendLine(b, "<body>");
		appendLine(b, "<div id='header'>");
		appendLine(
				b,
				"<div><a target=\"_blank\" href='http://www.softhinkers.com'><img id='logo' src=\""
						+ getResource("images/selendroid-logo.png")
						+ "\"></a><div id='logo'> Inspector shortcuts: <em>CTRL</em> locks tree element selection, <em>ESC</em> opens XPath helper</div></div>");

		appendLine(b,
				"<div style=\"display: none\"><input type='checkbox' id='record'/>");
		appendLine(b,
				"<label for='record' id='record_text'>Record</label></div>");

		appendLine(b, "</div>");

		appendLine(b, "<div id='content' style='height: 750px'>");
		appendLine(b, "<div class='ui-layout-center'>");
		appendLine(b, "<div class='ui-layout-west' id='device'>");
		appendLine(b, "<div id='simulator'>");

		appendLine(b, "<div id='rotationCenter'>");
		appendLine(b, "<div id='mouseOver'></div>");
		appendLine(b, "<div id='frame'>");

		appendLine(b, "<div id='screen'>");
		appendLine(b, "<img id='screenshot' src='" + getScreen(request)
				+ "' width='320px'/>");
		appendLine(b, "</div>");
		appendLine(b, "</div>");
		appendLine(b, "</div>");
		appendLine(b, "</div>");
		appendLine(b, "</div>");
		appendLine(b, "<div class='ui-layout-center'>");
		appendLine(b, "    <div id='tree'></div>");
		appendLine(b, "</div>");
		appendLine(b, "<div class='ui-layout-east'>");
		appendLine(b, "    <div id='details'></div>");
		appendLine(b, "</div>");

		appendLine(b, "</div>");
		appendLine(b, "<div class='ui-layout-south'>");
		appendLine(b, "<ul>");
		appendLine(b, "<li><a href='#java'>Java</a></li>");
		appendLine(b, "<li><a href='#htmlSource'>Html Source</a></li>");
		appendLine(b, "</ul>");

		appendLine(b, "<div class='ui-layout-content ui-widget-content'>");
		appendLine(b, "<pre id='java' class='prettyprint' width='100%'></pre>");
		appendLine(b,
				"<pre id='htmlSource' class='prettyprint' width='100%'></pre>");
		appendLine(b, "</div>");
		appendLine(b, "</div>");

		appendLine(b, "</div>");
		appendLine(b, "</div>");
		appendLine(b, "<div id='xpathHelper' title='Xpather helper'>");
		appendLine(b, "<input type='text' value='' id='xpathInput'/>");

		appendLine(b, "<div id='xpathLog'> log</div>");
		appendLine(b, "</div>");

		appendLine(b, "<div id='footer'>");

		appendLine(
				b,
				"Maintained by <a href=\"https://github.com/selendroid/AdarshMaurya\" target=\"_blank\">Galsen Author</a> | ");

		appendLine(b, "</div>");
		appendLine(b,
				"<script >configure('iphone','Regular','PORTRAIT');</script>");
		appendLine(b, "</body>");
		appendLine(b, "</html>");

		return b.toString();
	}

	private void appendLine(StringBuilder buffer, String line) {
		buffer.append(line + System.getProperty("line.separator"));
	}

	public abstract String getResource(String paramString);

	public abstract String getScreen(HttpRequest paramHttpRequest);

	private String getFrame() {
		return getResource("frameNexus4.png");
	}
}