/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.io.github;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RESTCaller {
    private int issueNumber;
    private String status;

    public RESTCaller() {
    }

    public void put(String url, String requestMethod, String content, String accessToken) {
        try {
            URL obj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();

            httpURLConnection.setRequestMethod(requestMethod);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Token " + accessToken);

            httpURLConnection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
            wr.writeBytes(content);
            wr.flush();
            wr.close();

            parseResponse(httpURLConnection);
        } catch (Exception e) {
           log.error("Failed put", e);
        }
    }

    private HttpURLConnection createUrlConnection(String url, String requestMethod, String accessToken) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();

        httpURLConnection.setRequestMethod(requestMethod);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        if (accessToken != null)
            httpURLConnection.setRequestProperty("Authorization", "Token " + accessToken);

        return httpURLConnection;
    }

    public Integer getResponseCode(String url, String requestMethod, String accessToken) {
        try {
            HttpURLConnection httpURLConnection = createUrlConnection(url, requestMethod, accessToken);
            return httpURLConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String get(
        String url,
        String requestMethod,
        String accessToken // nullable
    ) {
        try {
            HttpURLConnection httpURLConnection = createUrlConnection(url, requestMethod, accessToken);
            return parseResponse(httpURLConnection);
        } catch (Exception e) {
            log.error("Failed get", e);
            throw new RuntimeException(e);
        }
    }


    private String parseResponse(HttpURLConnection httpURLConnection) throws IOException {
        StringBuilder builder = getResponse(httpURLConnection);

        int responseCode = httpURLConnection.getResponseCode();
        if (!(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED)) {
            log.error("Unexpected response code: " + responseCode + "\n" + status + ".\n" + builder);
            throw new RuntimeException();
        } else {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((httpURLConnection.getInputStream())));
            final StringBuilder stringBuilder = new StringBuilder();
            String output;
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            return stringBuilder.toString();
        }
    }

    private StringBuilder getResponse(HttpURLConnection httpURLConnection) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(httpURLConnection.getResponseCode())
            .append(" ")
            .append(httpURLConnection.getResponseMessage())
            .append("\n");

        Map<String, List<String>> map = httpURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() == null)
                continue;
            builder.append(entry.getKey())
                .append(": ");

            List<String> headerValues = entry.getValue();
            Iterator<String> it = headerValues.iterator();
            if (it.hasNext()) {
                builder.append(it.next());

                while (it.hasNext()) {
                    builder.append(", ")
                        .append(it.next());
                }
            }

            if (entry.getKey().equals("Location")) {
                final String[] split = entry.getValue().get(0).split("/");
                issueNumber = Integer.parseInt(split[split.length - 1]);
            }

            if (entry.getKey().equals("Status")) {
                status = entry.getValue().get(0);
            }

            builder.append("\n");
        }
        return builder;
    }

    public int getIssueNumber() {
        return issueNumber;
    }
}
