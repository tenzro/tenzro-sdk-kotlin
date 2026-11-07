import network.tenzro.sdk.TenzroClientBlocking;

public class BasicJava {
    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "http://127.0.0.1:28545";
        try (TenzroClientBlocking c = new TenzroClientBlocking(url, 13380L)) {
            c.verifyChainId();
            System.out.println("block " + c.getBlockNumber() + ", state " + c.getStatus().getState());
            System.out.println("balance " + c.getBalance("0x0000000000000000000000000000000000000001"));
        }
    }
}
