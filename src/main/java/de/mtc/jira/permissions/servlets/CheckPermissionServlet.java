package de.mtc.jira.permissions.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckPermissionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final static String TEST_FILE_CONTENT = "This is an automatically created test file that should have been deleted automatically: Delete it now!";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String context = req.getContextPath() + "/plugins/servlet/checkperms";
		String path = req.getParameter("dir");
		if (path == null) {
			path = Paths.get(".").toAbsolutePath().normalize().toString();
		}
		String test = req.getParameter("test");

		File root = new File(path);
		File[] children = new File(path).listFiles();
		List<File> directories = new ArrayList<>();
		List<File> files = new ArrayList<>();

		for (File file : children) {
			if (file.isDirectory()) {
				directories.add(file);
			} else {
				files.add(file);
			}
		}

		ServletOutputStream out = resp.getOutputStream();
		out.println("<html><body>");
		out.println("<h2>" + path + "</h2>");

		out.println("<p style=\"font-weight:bold;\"><a href=\"" + context + "?test=yes&dir="
				+ root.getAbsolutePath() + "\">Test write permissions</a></p>");

		if (test != null) {
			writeTestResult(root, out);
		}

		File parent = root.getParentFile();
		if (parent != null) {
			out.println("<p style=\"font-weight:bold;\"><a href=\"" + context + "?dir="
					+ parent.getAbsolutePath() + "\">");
			out.println("../");
			out.println("</a></p>");
		}

		for (File f : directories) {
			printHtml(f, out, context);
		}
		for (File f : files) {
			printHtml(f, out, context);
		}

		out.println("</html></body>");
	}

	private void writeTestResult(File root, ServletOutputStream out) throws IOException {
		File test = null;
		try {
			test = new File(root, "writepermission_test.tmp");
			try (FileWriter writer = new FileWriter(test)) {
				writer.write(
						"This is an automatically created test file that should have been deleted automatically: Delete it now!");
			}
			out.println("<p style=\"color:green;font-weight:bold;\">Check write: ok</p>");
		} catch (Exception e) {
			out.println("<p style=\"color:red;font-weight:bold;\">Unable to write on file " + test + "</p>");
			out.println("<p style=\"color:red;font-weight:bold;\">Reason: " + e.getMessage() + "</p>");
		}

		if (test != null && test.exists()) {
			try {
				try (BufferedReader reader = new BufferedReader(new FileReader(test))) {
					String result = reader.lines().collect(Collectors.joining("\n"));
					if (result != null && result.trim().equals(TEST_FILE_CONTENT)) {
						out.println("<p style=\"color:green;font-weight:bold;\">Check read: ok</p>");
					} else {
						out.println("<p style=\"color:red;font-weight:bold;\">Reading failed</p>");
						out.println("<p style=\"color:red;font-weight:bold;\">" + result + " != " + TEST_FILE_CONTENT
								+ "</p>");
					}
				}
			} catch (Exception e) {
				out.println("<p style=\"color:red;font-weight:bold;\">Unable to read file " + test + "</p>");
				out.println("<p style=\"color:red;font-weight:bold;\">Reason: " + e.getMessage() + "</p>");
			}
		}

		if (test != null && test.exists()) {
			try {
				test.delete();
				out.println("<p style=\"color:green;font-weight:bold;\">Check delete: ok</p>");
			} catch (Exception e) {
				out.println("<p style=\"color:red;font-weight:bold;\">Unable to delete file " + test + "</p>");
			}
		}
	}

	private void printHtml(File file, ServletOutputStream out, String context) throws IOException {
		boolean canWrite = file.canWrite();
		if (file.isDirectory()) {
			out.println("<p style=\"color:light-blue;font-weight:bold;\">");
			out.println("<a href=\"" + context + "?dir=" + file.getAbsolutePath() + "\">");
			out.println(file.getAbsolutePath());
			out.println("\tWritable: " + canWrite);
			out.println("</a>");
			out.println("</p>");
		} else {
			out.println("<p>" + file.getAbsolutePath() + "\tWritable: " + canWrite + "</p>");
		}
	}
}
