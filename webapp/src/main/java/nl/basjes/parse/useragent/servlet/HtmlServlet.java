/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.servlet;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class HtmlServlet extends HttpServlet {

    protected static UserAgentAnalyzer uua;

    public void init() throws ServletException {
        uua = new UserAgentAnalyzer();
    }

    public synchronized void doGet(HttpServletRequest request,
                                   HttpServletResponse response)
        throws ServletException, IOException {
        long start = System.nanoTime();

        PrintWriter out = response.getWriter();
        try {
            response.setContentType(MediaType.TEXT_HTML);
            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            out.println("<title>Analyzing the useragent</title></head>");
            out.println("<body>");

            // Actual logic goes here.
            out.println("<h1>Analyzing your useragent string</h1>");

            String userAgentString = request.getHeader("User-Agent");
            if (userAgentString == null) {
                out.println("<b><u>The User-Agent header is missing</u></b>");
                return;
            }

            UserAgent userAgent = uua.parse(userAgentString); // This class is NOT threadsafe/reentrant !

//            System.out.println("Useragent: " + userAgentString);
            out.println("Received useragent header: <h2>" + escapeHtml4(userAgent.getUserAgentString()) + "</h2>");
            out.println("<table border=1>");
            out.println("<tr><th>Field</th><th>Value</th></tr>");
            for (String fieldname : userAgent.getAvailableFieldNamesSorted()) {
                out.println("<tr>" +
                    "<td>" + camelStretcher(escapeHtml4(fieldname)) + "</td>" +
                    "<td>" + escapeHtml4(userAgent.getValue(fieldname)) + "</td>" +
                    "</tr>");
            }
            out.println("</table>");
        } finally {
            long stop = System.nanoTime();
            double milliseconds = (stop - start) / 1000000.0;

            out.println("<br/>");
            out.println("<u>Building this page took " + String.format(Locale.ENGLISH, "%3.3f", milliseconds) + " ms.</u><br/>");
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    }

    private String camelStretcher(String input) {
        String result = input.replaceAll("([A-Z])", " $1");
        result = result.replaceAll("Device", "<b><u>Device</u></b>");
        result = result.replaceAll("Operating System", "<b><u>Operating System</u></b>");
        result = result.replaceAll("Layout Engine", "<b><u>Layout Engine</u></b>");
        result = result.replaceAll("Agent", "<b><u>Agent</u></b>");
        return result;
    }

}