package com.ft.sdk.garble.http;
import com.ft.sdk.diagnostics.internal.DiagnosticsBridge;
import java.io.IOException;
import okhttp3.*;
import okio.*;
/** Counts HTTP body bytes at the network interceptor boundary, after request compression. */
final class DiagnosticNetworkInterceptor implements Interceptor {
    public Response intercept(Chain chain) throws IOException {
        final DiagnosticsBridge.Transfer t = DiagnosticsBridge.transfer();
        if (t == null) return chain.proceed(chain.request());
        Request request = chain.request(); final RequestBody body = request.body();
        if (body != null) {
            request = request.newBuilder().method(request.method(), new RequestBody() {
                public MediaType contentType() { return body.contentType(); }
                public long contentLength() throws IOException { return body.contentLength(); }
                public void writeTo(BufferedSink sink) throws IOException {
                    if(t.tx<0)t.tx=0;
                    BufferedSink counted = Okio.buffer(new ForwardingSink(sink) {
                        public void write(Buffer source,long bytes)throws IOException {super.write(source,bytes);t.tx+=bytes;}
                    });
                    body.writeTo(counted); counted.flush();
                }
            }).build();
        } else if(t.tx<0) t.tx=0;
        Response response = chain.proceed(request); final ResponseBody responseBody=response.body();
        if(responseBody==null)return response;
        if(t.rx<0)t.rx=0;
        final BufferedSource counted=Okio.buffer(new ForwardingSource(responseBody.source()) {
            public long read(Buffer sink,long bytes)throws IOException {long n=super.read(sink,bytes);if(n>0)t.rx+=n;return n;}
        });
        return response.newBuilder().body(new ResponseBody() {
            public MediaType contentType(){return responseBody.contentType();}
            public long contentLength(){return responseBody.contentLength();}
            public BufferedSource source(){return counted;}
        }).build();
    }
}
