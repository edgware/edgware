package fabric.tools.json;

public class JsonCLIResponseListener extends Thread {

    JsonClient jsonClient = null;
    long timeout = 100000;

    public JsonCLIResponseListener(JsonClient jsonClient) {
        this.jsonClient = jsonClient;
        setName("JSON-CLI-Response-Listener");
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                String response = jsonClient.getResponse(timeout);
                if (response != null) {
                    System.out.println("RESPONSE : " + response);
                }
            } catch (InterruptedException e) {

                // Expected just loop again
            }
        }
    }

}
