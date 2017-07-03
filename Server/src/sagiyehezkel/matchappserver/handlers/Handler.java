package sagiyehezkel.matchappserver.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class Handler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		// Handling Request
    	String request;
    	BufferedReader reader = null;
    	
    	InputStream inputStream = t.getRequestBody();
    	
    	// Read the input stream into a String
        StringBuffer buffer = new StringBuffer();
        if (inputStream == null) {
            // Nothing to do.
            return;
        }
        
        reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }

        if (buffer.length() == 0) {
            // Stream was empty.  No point in parsing.
            return;
        }
        
        request = buffer.toString();
        
        // Handling Response
        System.out.println(getHandlerName());
        System.out.print("\tIn  Msg:\t" + request);
        String response = handleMsg(request);
        if (response == null)
        	response = "";
        System.out.println("\tOut Msg:\t" + response + "\n");
        
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
	
	protected abstract String getHandlerName();
	
	protected abstract String handleMsg(String request);
	
}
