package generated;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.33.0)",
    comments = "Source: scheme.proto")
public final class sscGrpc {

  private sscGrpc() {}

  public static final String SERVICE_NAME = "ssc.ssc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<generated.ride,
      generated.response> getUpsertMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "upsert",
      requestType = generated.ride.class,
      responseType = generated.response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.ride,
      generated.response> getUpsertMethod() {
    io.grpc.MethodDescriptor<generated.ride, generated.response> getUpsertMethod;
    if ((getUpsertMethod = sscGrpc.getUpsertMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getUpsertMethod = sscGrpc.getUpsertMethod) == null) {
          sscGrpc.getUpsertMethod = getUpsertMethod =
              io.grpc.MethodDescriptor.<generated.ride, generated.response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "upsert"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.ride.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.response.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("upsert"))
              .build();
        }
      }
    }
    return getUpsertMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.emptyMessage,
      generated.response> getGetRidesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRides",
      requestType = generated.emptyMessage.class,
      responseType = generated.response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.emptyMessage,
      generated.response> getGetRidesMethod() {
    io.grpc.MethodDescriptor<generated.emptyMessage, generated.response> getGetRidesMethod;
    if ((getGetRidesMethod = sscGrpc.getGetRidesMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getGetRidesMethod = sscGrpc.getGetRidesMethod) == null) {
          sscGrpc.getGetRidesMethod = getGetRidesMethod =
              io.grpc.MethodDescriptor.<generated.emptyMessage, generated.response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRides"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.emptyMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.response.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("getRides"))
              .build();
        }
      }
    }
    return getGetRidesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.ride,
      generated.response> getAddRideLeaderMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "addRideLeader",
      requestType = generated.ride.class,
      responseType = generated.response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.ride,
      generated.response> getAddRideLeaderMethod() {
    io.grpc.MethodDescriptor<generated.ride, generated.response> getAddRideLeaderMethod;
    if ((getAddRideLeaderMethod = sscGrpc.getAddRideLeaderMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getAddRideLeaderMethod = sscGrpc.getAddRideLeaderMethod) == null) {
          sscGrpc.getAddRideLeaderMethod = getAddRideLeaderMethod =
              io.grpc.MethodDescriptor.<generated.ride, generated.response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "addRideLeader"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.ride.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.response.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("addRideLeader"))
              .build();
        }
      }
    }
    return getAddRideLeaderMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.ride,
      generated.response> getAddRideFollowerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "addRideFollower",
      requestType = generated.ride.class,
      responseType = generated.response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.ride,
      generated.response> getAddRideFollowerMethod() {
    io.grpc.MethodDescriptor<generated.ride, generated.response> getAddRideFollowerMethod;
    if ((getAddRideFollowerMethod = sscGrpc.getAddRideFollowerMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getAddRideFollowerMethod = sscGrpc.getAddRideFollowerMethod) == null) {
          sscGrpc.getAddRideFollowerMethod = getAddRideFollowerMethod =
              io.grpc.MethodDescriptor.<generated.ride, generated.response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "addRideFollower"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.ride.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.response.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("addRideFollower"))
              .build();
        }
      }
    }
    return getAddRideFollowerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.reservation,
      generated.ride> getReserveRideLeaderMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "reserveRideLeader",
      requestType = generated.reservation.class,
      responseType = generated.ride.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.reservation,
      generated.ride> getReserveRideLeaderMethod() {
    io.grpc.MethodDescriptor<generated.reservation, generated.ride> getReserveRideLeaderMethod;
    if ((getReserveRideLeaderMethod = sscGrpc.getReserveRideLeaderMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getReserveRideLeaderMethod = sscGrpc.getReserveRideLeaderMethod) == null) {
          sscGrpc.getReserveRideLeaderMethod = getReserveRideLeaderMethod =
              io.grpc.MethodDescriptor.<generated.reservation, generated.ride>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "reserveRideLeader"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.reservation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.ride.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("reserveRideLeader"))
              .build();
        }
      }
    }
    return getReserveRideLeaderMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static sscStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<sscStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<sscStub>() {
        @java.lang.Override
        public sscStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new sscStub(channel, callOptions);
        }
      };
    return sscStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static sscBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<sscBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<sscBlockingStub>() {
        @java.lang.Override
        public sscBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new sscBlockingStub(channel, callOptions);
        }
      };
    return sscBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static sscFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<sscFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<sscFutureStub>() {
        @java.lang.Override
        public sscFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new sscFutureStub(channel, callOptions);
        }
      };
    return sscFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class sscImplBase implements io.grpc.BindableService {

    /**
     */
    public void upsert(generated.ride request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnimplementedUnaryCall(getUpsertMethod(), responseObserver);
    }

    /**
     */
    public void getRides(generated.emptyMessage request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRidesMethod(), responseObserver);
    }

    /**
     */
    public void addRideLeader(generated.ride request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnimplementedUnaryCall(getAddRideLeaderMethod(), responseObserver);
    }

    /**
     */
    public void addRideFollower(generated.ride request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnimplementedUnaryCall(getAddRideFollowerMethod(), responseObserver);
    }

    /**
     */
    public void reserveRideLeader(generated.reservation request,
        io.grpc.stub.StreamObserver<generated.ride> responseObserver) {
      asyncUnimplementedUnaryCall(getReserveRideLeaderMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getUpsertMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                generated.ride,
                generated.response>(
                  this, METHODID_UPSERT)))
          .addMethod(
            getGetRidesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                generated.emptyMessage,
                generated.response>(
                  this, METHODID_GET_RIDES)))
          .addMethod(
            getAddRideLeaderMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                generated.ride,
                generated.response>(
                  this, METHODID_ADD_RIDE_LEADER)))
          .addMethod(
            getAddRideFollowerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                generated.ride,
                generated.response>(
                  this, METHODID_ADD_RIDE_FOLLOWER)))
          .addMethod(
            getReserveRideLeaderMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                generated.reservation,
                generated.ride>(
                  this, METHODID_RESERVE_RIDE_LEADER)))
          .build();
    }
  }

  /**
   */
  public static final class sscStub extends io.grpc.stub.AbstractAsyncStub<sscStub> {
    private sscStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected sscStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new sscStub(channel, callOptions);
    }

    /**
     */
    public void upsert(generated.ride request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpsertMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRides(generated.emptyMessage request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetRidesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addRideLeader(generated.ride request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddRideLeaderMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addRideFollower(generated.ride request,
        io.grpc.stub.StreamObserver<generated.response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddRideFollowerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void reserveRideLeader(generated.reservation request,
        io.grpc.stub.StreamObserver<generated.ride> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getReserveRideLeaderMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class sscBlockingStub extends io.grpc.stub.AbstractBlockingStub<sscBlockingStub> {
    private sscBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected sscBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new sscBlockingStub(channel, callOptions);
    }

    /**
     */
    public generated.response upsert(generated.ride request) {
      return blockingUnaryCall(
          getChannel(), getUpsertMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.response getRides(generated.emptyMessage request) {
      return blockingUnaryCall(
          getChannel(), getGetRidesMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.response addRideLeader(generated.ride request) {
      return blockingUnaryCall(
          getChannel(), getAddRideLeaderMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.response addRideFollower(generated.ride request) {
      return blockingUnaryCall(
          getChannel(), getAddRideFollowerMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.ride reserveRideLeader(generated.reservation request) {
      return blockingUnaryCall(
          getChannel(), getReserveRideLeaderMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class sscFutureStub extends io.grpc.stub.AbstractFutureStub<sscFutureStub> {
    private sscFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected sscFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new sscFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.response> upsert(
        generated.ride request) {
      return futureUnaryCall(
          getChannel().newCall(getUpsertMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.response> getRides(
        generated.emptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getGetRidesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.response> addRideLeader(
        generated.ride request) {
      return futureUnaryCall(
          getChannel().newCall(getAddRideLeaderMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.response> addRideFollower(
        generated.ride request) {
      return futureUnaryCall(
          getChannel().newCall(getAddRideFollowerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<generated.ride> reserveRideLeader(
        generated.reservation request) {
      return futureUnaryCall(
          getChannel().newCall(getReserveRideLeaderMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UPSERT = 0;
  private static final int METHODID_GET_RIDES = 1;
  private static final int METHODID_ADD_RIDE_LEADER = 2;
  private static final int METHODID_ADD_RIDE_FOLLOWER = 3;
  private static final int METHODID_RESERVE_RIDE_LEADER = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final sscImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(sscImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPSERT:
          serviceImpl.upsert((generated.ride) request,
              (io.grpc.stub.StreamObserver<generated.response>) responseObserver);
          break;
        case METHODID_GET_RIDES:
          serviceImpl.getRides((generated.emptyMessage) request,
              (io.grpc.stub.StreamObserver<generated.response>) responseObserver);
          break;
        case METHODID_ADD_RIDE_LEADER:
          serviceImpl.addRideLeader((generated.ride) request,
              (io.grpc.stub.StreamObserver<generated.response>) responseObserver);
          break;
        case METHODID_ADD_RIDE_FOLLOWER:
          serviceImpl.addRideFollower((generated.ride) request,
              (io.grpc.stub.StreamObserver<generated.response>) responseObserver);
          break;
        case METHODID_RESERVE_RIDE_LEADER:
          serviceImpl.reserveRideLeader((generated.reservation) request,
              (io.grpc.stub.StreamObserver<generated.ride>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class sscBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    sscBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return generated.Scheme.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ssc");
    }
  }

  private static final class sscFileDescriptorSupplier
      extends sscBaseDescriptorSupplier {
    sscFileDescriptorSupplier() {}
  }

  private static final class sscMethodDescriptorSupplier
      extends sscBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    sscMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (sscGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new sscFileDescriptorSupplier())
              .addMethod(getUpsertMethod())
              .addMethod(getGetRidesMethod())
              .addMethod(getAddRideLeaderMethod())
              .addMethod(getAddRideFollowerMethod())
              .addMethod(getReserveRideLeaderMethod())
              .build();
        }
      }
    }
    return result;
  }
}
