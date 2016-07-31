package com.justin.tool.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Proxy {

    private static final int DEFAULT_SERVICE_PORT = 8888;

    private static String host;
    private static int servicePort;

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.out.println("Usage:");
            System.out.println("      $JAVA com.justin.tool.util.Proxy $host [server port]");
            System.exit(1);
        }

        host = args[0];
        servicePort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_SERVICE_PORT;

        ServerSocket ss = new ServerSocket(servicePort);

        Logger.info(Proxy.class, "----Justin Proxy Service----");
        Logger.info(Proxy.class, "proxy host:" + host + " at port:" + servicePort);
        Logger.info(Proxy.class, "----Justin Proxy Service----");

        int idx = 1;
        while (true) {
            Socket s = ss.accept();
            new Thread(new WorkThread(String.format("Justin-Proxy-%05d", idx++), s)).start();
        }
    }

    static class WorkThread implements Runnable {

        private String threadName;
        private Socket socket;

        public WorkThread(String threadName, Socket socket) {
            this.threadName = threadName;
            this.socket = socket;
        }

        private AbstractMap.SimpleEntry<String, String> readLine(BufferedInputStream bis) throws IOException {
            int b;
            boolean keyEnd = false;
            int lineEnd = 0;

            StringBuffer k = new StringBuffer();
            StringBuffer v = new StringBuffer();

            while ((lineEnd != 11) && (b = bis.read()) != -1) {
                if ('\r' == b) {
                    lineEnd = 10;
                } else if ('\n' == b && lineEnd == 10) {
                    lineEnd = 11;
                } else {
                    lineEnd = 0;
                }
                if (keyEnd) {
                    v.append((char) b);
                } else if (':' == b) {
                    keyEnd = true;
                } else {
                    k.append((char) b);
                }
            }
            if (!keyEnd) {
                k.setLength(k.length() - 2);
                return new AbstractMap.SimpleEntry<String, String>("", k.toString());
            }
            v.setLength(v.length() - 2);
            return new AbstractMap.SimpleEntry<String, String>(k.toString(), v.substring(1));
        }

        private byte[] readData(BufferedInputStream bis, int len) throws IOException {
            byte[] d = new byte[len];
            int l = bis.read(d);
            if (l != len) {
                throw new RuntimeException("data not enough!expectedLength:" + len + " actualLength:" + l);
            }
            return d;
        }

        @Override
        public void run() {

            long _s = System.currentTimeMillis();

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {

                Map<String, String> header = new HashMap<String, String>();
                byte[] data = new byte[0];

                bis = new BufferedInputStream(socket.getInputStream());
                bos = new BufferedOutputStream(socket.getOutputStream());

                // read & parse head
                do {
                    Map.Entry<String, String> e = readLine(bis);

                    if (e.getKey().length() == 0 && e.getValue().length() == 0) {
                        break;
                    }

                    header.put(e.getKey(), e.getValue());

                } while (true);

                String line = header.get("");
                String url = line.replaceAll("HTTP/1\\.\\d", "").replaceAll(".* /", "/").replaceAll("\\s*", "");
                boolean isPost = line.startsWith("POST /");
                int contentLength = header.containsKey("Content-Length") ? Integer.parseInt(header.get("Content-Length")) : 0;

                Logger.info(Proxy.class, threadName + " << " + header);
                Logger.info(Proxy.class, threadName + " << " + url);


                // read body if contained
                if (isPost && contentLength > 0) {
                    data = readData(bis, contentLength);
                    Logger.info(Proxy.class, threadName + " << " + new String(data));
                }

                Http.Response r;
                if (isPost) {
                    r = Http.post(host + url, header, data);
                } else {
                    r = Http.get(host + url, header);
                }

                bos.write(("HTTP/1.1 " + r.statusCode + " " + r.statusMsg + "\r\n").getBytes("UTF-8"));

                for (Map.Entry<String, List<String>> h : r.header.entrySet()) {
                    if (h.getKey() != null && h.getKey().length() == 0) {
                        continue;
                    }
                    if (Arrays.asList("").contains(h.getKey())) {
                        continue;
                    }
                    for (String v : h.getValue()) {
                        bos.write((h.getKey() + ": " + v + "\r\n").getBytes("UTF-8"));
                    }
                }

                bos.write("\r\n".getBytes("UTF-8"));

                if (r.data != null && r.data.length > 0) {
                    bos.write(r.data);
                }
                bos.flush();

                Logger.info(Proxy.class, threadName + " >> " + r.statusCode + " " + r.statusMsg);
                Logger.info(Proxy.class, threadName + " >> " + r.header);
                Logger.info(Proxy.class, threadName + " >> " + new String(r.data));
            } catch (IOException e) {
                Logger.info(Proxy.class, threadName + " >> " + e);
                if (bos != null) {
                    try {
                        String errMsg = String.valueOf(e);
                        bos.write(("HTTP/1.1 " + 500 + " " + "Proxy Server ERROR" + "\r\n").getBytes("UTF-8"));
                        bos.write(("Date: " + DateUitl.formatGMT(new Date()) + "\r\n").getBytes("UTF-8"));
                        bos.write(("Connection: close" + "\r\n").getBytes("UTF-8"));
                        bos.write(("Content-Type: text/html; charset=utf-8" + "\r\n").getBytes("UTF-8"));
                        bos.write(("Server: Justin Proxy Server" + "\r\n").getBytes("UTF-8"));
                        bos.write(("Content-Length: " + errMsg.getBytes("UTF-8").length + "\r\n").getBytes("UTF-8"));
                        bos.write(("\r\n").getBytes("UTF-8"));
                        bos.write((errMsg.getBytes("UTF-8")));
                        bos.flush();
                    } catch (IOException e1) {
                        Logger.info(Proxy.class, threadName + " >> " + e1);
                    }

                }
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        Logger.info(Proxy.class, threadName + " >> " + e);
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        Logger.info(Proxy.class, threadName + " >> " + e);
                    }
                }
                Logger.info(Proxy.class, threadName + "--------------time:" + (System.currentTimeMillis() - _s) + "ms----------------");
            }
        }
    }
}
