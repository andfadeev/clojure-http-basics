# Clojure HTTP basics 

## Web server

We are using popular web servers to do the heavy-lifting (they will accept incoming request, scale internal thread-pools according to load, etc.)

The most popular option I've encountered is Jetty. Ring comes with Jetty adaptor that makes it really easy to use.

A simple Jetty example in plain Java:
```java 
// Define HTTP Servlet 
public class ExampleServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().println("{ \"status\": \"ok\"}");
  }
}

// Start Embedded Jetty
public class StartJettyWebserver {
    public static void main(String[] args) throws Exception {
        var server = new Server(8082);

        var handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(ExampleServlet.class, "/*");
        server.start();
        server.join();
    }
}
```

## Ring core concepts 

### Ring Request
Requests is a Clojure map representation of incoming HTTP request.

Ring HTTP request keys:
- `:server-port` The port on which the request is being handled.
- `:server-name` The resolved server name, or the server IP address.
- `:remote-addr` The IP address of the client or the last proxy that sent the request.
- `:uri` The request URI (the full path after the domain name).
- `:query-string` The query string, if present.
- `:scheme` The transport protocol, either :http or :https.
- `:request-method` The HTTP request method, which is one of :get, :head, :options, :put, :post, or :delete.
- `:headers` A Clojure map of lowercase header name strings to corresponding header value strings.
- `:body` An InputStream for the request body, if present.

```clojure 
{:ssl-client-cert nil,
 :protocol "HTTP/1.1",
 :remote-addr "127.0.0.1",
 :headers
 {"cache-control" "max-age=0",
  "accept"
  "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
  "upgrade-insecure-requests" "1",
  "connection" "keep-alive",
  "user-agent"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36",
  "host" "0.0.0.0:3000",
  "accept-encoding" "gzip, deflate",
  "accept-language" "en-GB,en-US;q=0.9,en;q=0.8"},
 :server-port 3000,
 :content-length nil,
 :content-type nil,
 :character-encoding nil,
 :uri "/test",
 :server-name "0.0.0.0",
 :query-string "q=1",
 :body
 #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x693fecbf "HttpInputOverHTTP@693fecbf[c=0,q=0,[0]=null,s=STREAM]"],
 :scheme :http,
 :request-method :get}
```

## Ring Responses

The response map is created by the handler, and contains three keys:

- `:status` The HTTP status code, such as 200, 302, 404 etc.

- `:headers` A Clojure map of HTTP header names to header values. These values may either be strings, in which case one name/value header will be sent in the HTTP response, or a collection of strings, in which case a name/value header will be sent for each value.
- `:body` A representation of the response body, if a response body is appropriate for the response's status code. The body can be one of four types:
  - `String` The body is sent directly to the client.
  - `ISeq` Each element of the seq is sent to the client as a string.
  - `File` The contents of the referenced file is sent to the client.
  - `InputStream` The contents of the stream is sent to the client. When the stream is exhausted, the stream is closed.

### Ring Handlers

Handlers are functions that define your web application. Synchronous handlers take one argument, a map representing a HTTP request, and return a map representing the HTTP response.

```clojure 
(defn what-is-my-ip 
  [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (:remote-addr request)})
```

### Middleware
Middleware are higher-level functions that add additional functionality to handlers. The first argument of a middleware function should be a handler, and its return value should be a new handler function that will call the original handler.

```clojure
(defn wrap-content-type [handler content-type]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))
```
Example usage:
```clojure
(def app
  (-> handler
      (wrap-content-type "text/html")
      (wrap-some-other-middleware)
      (wrap-keyword-params)
      (wrap-params)))
```

## HTTP routing libraries 
- Compojure 
- Bidi
- Reitit
- Pedestal (as alternative to Ring)
