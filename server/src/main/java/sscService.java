import generated.*;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class sscService extends sscGrpc.sscImplBase {

    private final int port;

    public sscService(int port) {
        this.port = port;
    }

    @Override
    public void upsert(ride request, StreamObserver<response> responseObserver) {
        System.out.println("Server port [" + port + "]");
    }

    // Server streaming
//    @Override
//    public void listFeatures(Rectangle request, StreamObserver<Feature> responseObserver) {
//        // Calculate the rectangle boundaries
//        int left = min(request.getLo().getLongitude(), request.getHi().getLongitude());
//        int right = max(request.getLo().getLongitude(), request.getHi().getLongitude());
//        int top = max(request.getLo().getLatitude(), request.getHi().getLatitude());
//        int bottom = min(request.getLo().getLatitude(), request.getHi().getLatitude());
//
//        for (Feature feature : features) {
//            int lat = feature.getLocation().getLatitude();
//            int lon = feature.getLocation().getLongitude();
//            // Check whether the feature is in the rectangle
//            if (lon >= left && lon <= right && lat >= bottom && lat <= top) {
//                responseObserver.onNext(feature);
//            }
//        }
//        responseObserver.onCompleted();
//    }

//    // Bidirectional streaming
//    @Override
//    public StreamObserver<RouteNote> routeChat(final StreamObserver<RouteNote> responseObserver) {
//        return new StreamObserver<RouteNote>() {
//            @Override
//            public void onNext(RouteNote note) {
//                System.out.println("Request of " + note.getMessage());
//                RouteNote serverNote = RouteNote.newBuilder()
//                        .setLocation(note.getLocation())
//                        .setMessage("Respond to " + note.getMessage()).build();
//                responseObserver.onNext(serverNote);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("Server side stream completed");
//                responseObserver.onCompleted();
//            }
//        };
//    }
//
//    private List<RouteNote> getOrCreateNotes(Point location) {
//        List<RouteNote> notes = Collections.synchronizedList(new ArrayList<RouteNote>());
//        List<RouteNote> prevNotes = routeNotes.putIfAbsent(location, notes);
//        return prevNotes != null ? prevNotes : notes;
//    }
}