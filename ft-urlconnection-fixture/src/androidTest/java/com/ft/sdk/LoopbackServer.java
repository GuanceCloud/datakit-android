package com.ft.sdk;

import android.content.Context;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.*;

/** Real loopback HTTP/TLS transport; deterministic responses and captured wire requests. */
final class LoopbackServer implements Closeable {
    final ServerSocket listener;
    final SSLContext tls;
    final BlockingQueue<String> requests = new LinkedBlockingQueue<>();
    final BlockingQueue<Map<String, String>> requestHeaders = new LinkedBlockingQueue<>();
    final ExecutorService workers = Executors.newCachedThreadPool();
    final List<Socket> sockets = Collections.synchronizedList(new ArrayList<>());
    volatile boolean closed;

    LoopbackServer(Context context, boolean secure, boolean mutual) throws Exception {
        KeyStore keys = KeyStore.getInstance("PKCS12");
        try(InputStream in=context.getAssets().open("localhost.p12")) { keys.load(in,"test-only".toCharArray()); }
        KeyManagerFactory km=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        km.init(keys,"test-only".toCharArray());
        TrustManagerFactory tm=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); tm.init(keys);
        tls=SSLContext.getInstance("TLS"); tls.init(km.getKeyManagers(),tm.getTrustManagers(),null);
        listener=secure?tls.getServerSocketFactory().createServerSocket(0,50,InetAddress.getByName("127.0.0.1"))
                :new ServerSocket(0,50,InetAddress.getByName("127.0.0.1"));
        if(mutual)((SSLServerSocket)listener).setNeedClientAuth(true);
        workers.execute(()->{
            while(!closed)try { Socket socket=listener.accept(); sockets.add(socket); workers.execute(()->serve(socket)); }
            catch(IOException e){ if(!closed)throw new RuntimeException(e); }
        });
    }

    URL url(String path) throws Exception {
        return new URL((listener instanceof SSLServerSocket?"https":"http")+"://localhost:"+listener.getLocalPort()+path);
    }

    void configure(HttpURLConnection connection) {
        connection.setConnectTimeout(3000); connection.setReadTimeout(3000);
        if(connection instanceof HttpsURLConnection)((HttpsURLConnection)connection).setSSLSocketFactory(tls.getSocketFactory());
    }

    static String line(InputStream in) throws IOException {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); int c;
        while((c=in.read())!=-1 && c!='\n') { if(c!='\r')bytes.write(c); }
        return bytes.toString("UTF-8");
    }

    private void serve(Socket socket) {
        try {
            socket.setSoTimeout(4000);
            InputStream in=socket.getInputStream(); OutputStream out=socket.getOutputStream();
            String start=line(in); if(start.isEmpty())return;
            Map<String,String> headers=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for(String h=line(in); !h.isEmpty(); h=line(in)) {int i=h.indexOf(':');if(i>0)headers.put(h.substring(0,i),h.substring(i+1).trim());}
            ByteArrayOutputStream body=new ByteArrayOutputStream();
            if("chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
                for(int chunk=Integer.parseInt(line(in).split(";",2)[0],16);chunk>0;chunk=Integer.parseInt(line(in).split(";",2)[0],16)) {
                    copy(in,body,chunk); line(in);
                }
                line(in);
            } else copy(in,body,Integer.parseInt(headers.containsKey("Content-Length")?headers.get("Content-Length"):"0"));
            String[] parts=start.split(" "); String path=parts[1];
            if(path.startsWith("http"))path=new URL(path).getFile();
            requestHeaders.add(new TreeMap<>(headers));
            requests.add(parts[0]+" "+path+"\n"+headers.toString()+"\n"+Arrays.toString(body.toByteArray()));
            int status=path.equals("/slow-error")?500:path.startsWith("/status/")?Integer.parseInt(path.substring(8)):200;
            byte[] response="response-body".getBytes(StandardCharsets.UTF_8);
            String extra="";
            if(path.equals("/redirect")){status=302;extra="Location: /data\r\n";}
            if(path.equals("/gzip")){
                ByteArrayOutputStream compressed=new ByteArrayOutputStream();
                try(GZIPOutputStream gzip=new GZIPOutputStream(compressed)){gzip.write(response);}
                response=compressed.toByteArray();extra="Content-Encoding: gzip\r\n";
            }
            if(path.equals("/cache"))extra="Cache-Control: max-age=3600\r\n";
            if(path.equals("/cookie"))extra="Set-Cookie: fixture=value; Path=/\r\n";
            if(path.equals("/echo-cookie"))response=String.valueOf(headers.get("Cookie")).getBytes(StandardCharsets.UTF_8);
            if(path.equals("/auth")&&!headers.containsKey("Authorization")){status=401;extra="WWW-Authenticate: Basic realm=fixture\r\n";}
            if(path.equals("/stall") || path.equals("/slow-error"))Thread.sleep(400);
            boolean noBody=parts[0].equals("HEAD")||status==204||status==304;
            int length=status==204||status==304?0:path.equals("/large")?2*1024*1024:response.length;
            out.write(("HTTP/1.1 "+status+" Status\r\nContent-Type: text/plain\r\nContent-Length: "+length+"\r\n"+extra+"Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            if(!noBody) {
                if(path.equals("/large")){byte[] buffer=new byte[8192];for(int i=0;i<length;i+=buffer.length)out.write(buffer);}
                else if(path.equals("/broken"))out.write(response,0,2);
                else out.write(response);
            }
            out.flush();
        } catch(Exception ignored) { /* TLS rejection and timeout are intentional test cases. */ }
        finally {try{socket.close();}catch(IOException ignored){} sockets.remove(socket);}
    }

    static void copy(InputStream in,OutputStream out,int length)throws IOException{
        byte[] buffer=new byte[4096];while(length>0){int count=in.read(buffer,0,Math.min(length,buffer.length));if(count<0)throw new EOFException();out.write(buffer,0,count);length-=count;}
    }

    @Override public void close() throws IOException {
        closed=true;listener.close();
        synchronized(sockets){for(Socket socket:sockets)socket.close();}
        workers.shutdownNow();
    }
}
