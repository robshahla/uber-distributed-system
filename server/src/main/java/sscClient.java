import generated.*;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.util.logging.Logger;



public class sscClient {
    private static final Logger logger = Logger.getLogger(sscClient.class.getName());

    private final sscGrpc.sscBlockingStub blockingStub;
    private final sscGrpc.sscStub asyncStub;

    public sscClient(Channel channel) {

        blockingStub = sscGrpc.newBlockingStub(channel);
        asyncStub = sscGrpc.newStub(channel);
    }

    /**
     * Blocking server-streaming example. Calls listFeatures with a rectangle of interest. Prints each
     * response feature as it arrives.
     */
    public void upsert() {
        ride request =
                ride.newBuilder()
                        .setFirstName("Emil").setLastName("Kh").setPhone("05000").setStartPosition("A")
                        .setEndPosition("B").setDepartureTime("11/2/2020").setVacancies(3).setPd(4).build();

        try {
            blockingStub.upsert(request);

        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }
}