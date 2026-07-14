package com.omnibot.simple;

import java.util.List;

/**
 * Simple, flat POJOs for the chat API.
 * No nesting, no generics — serializes cleanly to JSON out of the box.
 */
public class SimpleChatDto {

    // ── Inbound ──────────────────────────────────────────────────────
    public static class Request {
        private String message;
        private String sessionId; // optional; generated server-side if blank

        public String getMessage()          { return message; }
        public void   setMessage(String v)  { this.message = v; }
        public String getSessionId()        { return sessionId; }
        public void   setSessionId(String v){ this.sessionId = v; }
    }

    // ── Outbound ─────────────────────────────────────────────────────
    public static class Response {
        private String sessionId;
        private String reply;
        private String intent;          // FOOD | CABS | SHOPPING | UNKNOWN
        private String step;            // which conversational step we're on
        private List<Option> options;   // vendor comparison cards (may be empty)

        public Response() {}
        public Response(String sessionId, String reply, String intent,
                        String step, List<Option> options) {
            this.sessionId = sessionId;
            this.reply     = reply;
            this.intent    = intent;
            this.step      = step;
            this.options   = options;
        }

        public String       getSessionId() { return sessionId; }
        public String       getReply()     { return reply; }
        public String       getIntent()    { return intent; }
        public String       getStep()      { return step; }
        public List<Option> getOptions()   { return options; }
    }

    // ── Vendor option card ────────────────────────────────────────────
    public static class Option {
        private String vendor;
        private String price;
        private String eta;
        private boolean cheapest;
        private boolean fastest;

        public Option(String vendor, String price, String eta,
                      boolean cheapest, boolean fastest) {
            this.vendor   = vendor;
            this.price    = price;
            this.eta      = eta;
            this.cheapest = cheapest;
            this.fastest  = fastest;
        }

        public String  getVendor()   { return vendor; }
        public String  getPrice()    { return price; }
        public String  getEta()      { return eta; }
        public boolean isCheapest()  { return cheapest; }
        public boolean isFastest()   { return fastest; }
    }
}
