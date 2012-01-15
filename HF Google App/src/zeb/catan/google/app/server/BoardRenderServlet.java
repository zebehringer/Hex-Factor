package zeb.catan.google.app.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;

import catan.impl.Game;

public class BoardRenderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Game game = GameHostServiceImpl.getGame(req);
		if (game != null) {
			BoardRenderer renderer = new BoardRenderer(game, getIntAttribute(req,"width",500), getIntAttribute(req,"height",500));
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				//in case the client embeds the svg
				ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet","type='text/css' href='/board-svg.css'");
				doc.appendChild(pi);
				renderer.render(doc);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bytes));
				
				resp.setContentType("image/svg+xml");
				resp.setContentLength(bytes.size());
				resp.getOutputStream().write(bytes.toByteArray());
			}
			catch (Exception ex) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				ex.printStackTrace();
			}
		}
	}
	
	private int getIntAttribute(HttpServletRequest req, String name, int defaultValue) {
		String value = req.getParameter(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			}
			catch (Exception ex) {}
		}
		return defaultValue;
	}

	
}
