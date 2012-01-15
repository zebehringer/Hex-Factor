package zeb.catan.google.app.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class Join extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String extra = "";
		if (request.getQueryString() != null && request.getQueryString().length() > 0) {
			extra = "?" + request.getQueryString();
		}

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			response.sendRedirect(userService.createLoginURL(request.getRequestURL().append(extra).toString()));
			return;
		}
		response.sendRedirect("/Play.html"+extra);
	}

	
}
