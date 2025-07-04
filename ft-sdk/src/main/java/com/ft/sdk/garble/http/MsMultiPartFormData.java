package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Form submission
 */
public class MsMultiPartFormData {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private final String charset;
    private final OutputStream outputStream;
    private final PrintWriter writer;

    /**
     * multipart/form-data form request, construct http request
     *
     * @throws java.io.IOException
     */
    public MsMultiPartFormData(HttpURLConnection connection)
            throws IOException {
        this.charset = "UTF-8";
        // creates a unique boundary based on time stamp
        boundary = "------" + System.currentTimeMillis();

        connection.setRequestProperty(Constants.SYNC_DATA_CONTENT_TYPE_HEADER,
                "multipart/form-data; boundary=" + boundary);
        outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    private String getContentType(String fileName) {
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        return contentType == null ? "application/octet-stream" : contentType;
    }

    /**
     * Add Field
     *
     * @param name  field parameter name
     * @param value field parameter value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }


    /**
     * Add form file
     *
     * @param fieldName Equivalent to <input type="file" name="..." />
     * @throws java.io.IOException
     */
    public void addFilePart(String fieldName, InputStream inputStream, String fileName)
            throws IOException {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                        "Content-Disposition: form-data; name=\"" + fieldName
                                + "\"; filename=\"" + fileName + "\""
                )
                .append(LINE_FEED);
        writer.append("Content-Type: " + getContentType(fileName)).append(LINE_FEED);
//        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Add form header parameter
     *
     * @param name  header parameter name
     * @param value header parameter value
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Return form submission Response
     *
     * @return Success returns 200, error returns error content
     * @throws java.io.IOException
     */
    public void finish() {
        List<String> response = new ArrayList<String>();

//        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();
    }


}