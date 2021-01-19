package generated;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.35.0)",
    comments = "Source: scheme.proto")
public final class sscGrpc {

  private sscGrpc() {}

  public static final String SERVICE_NAME = "ssc.ssc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<generated.emptyMessage,
      generated.Response> getGetRidesAsyncMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRidesAsync",
      requestType = generated.emptyMessage.class,
      responseType = generated.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<generated.emptyMessage,
      generated.Response> getGetRidesAsyncMethod() {
    io.grpc.MethodDescriptor<generated.emptyMessage, generated.Response> getGetRidesAsyncMethod;
    if ((getGetRidesAsyncMethod = sscGrpc.getGetRidesAsyncMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getGetRidesAsyncMethod = sscGrpc.getGetRidesAsyncMethod) == null) {
          sscGrpc.getGetRidesAsyncMethod = getGetRidesAsyncMethod =
              io.grpc.MethodDescriptor.<generated.emptyMessage, generated.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRidesAsync"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.emptyMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.Response.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("getRidesAsync"))
              .build();
        }
      }
    }
    return getGetRidesAsyncMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.ride,
      generated.Response> getAddRideLeaderMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "addRideLeader",
      requestType = generated.ride.class,
      responseType = generated.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<generated.ride,
      generated.Response> getAddRideLeaderMethod() {
    io.grpc.MethodDescriptor<generated.ride, generated.Response> getAddRideLeaderMethod;
    if ((getAddRideLeaderMethod = sscGrpc.getAddRideLeaderMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getAddRideLeaderMethod = sscGrpc.getAddRideLeaderMethod) == null) {
          sscGrpc.getAddRideLeaderMethod = getAddRideLeaderMethod =
              io.grpc.MethodDescriptor.<generated.ride, generated.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "addRideLeader"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.ride.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.Response.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("addRideLeader"))
              .build();
        }
      }
    }
    return getAddRideLeaderMethod;
  }

  private static volatile io.grpc.MethodDescriptor<generated.reservation,
      generated.ride> getReserveRidesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "reserveRides",
      requestType = generated.reservation.class,
      responseType = generated.ride.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<generated.reservation,
      generated.ride> getReserveRidesMethod() {
    io.grpc.MethodDescriptor<generated.reservation, generated.ride> getReserveRidesMethod;
    if ((getReserveRidesMethod = sscGrpc.getReserveRidesMethod) == null) {
      synchronized (sscGrpc.class) {
        if ((getReserveRidesMethod = sscGrpc.getReserveRidesMethod) == null) {
          sscGrpc.getReserveRidesMethod = getReserveRidesMethod =
              io.grpc.MethodDescriptor.<generated.reservation, generated.ride>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "reserveRides"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.reservation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  generated.ride.getDefaultInstance()))
              .setSchemaDescriptor(new sscMethodDescriptorSupplier("reserveRides"))
              .build();
        }
      }
    }
    return getReserveRidesMethod;
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
    public void getRidesAsync(generated.emptyMessage request,
        io.grpc.stub.StreamObserver<generated.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRidesAsyncMethod(), responseObserver);
    }

    /**
     */
    public void addRideLeader(generated.ride request,
        io.grpc.stub.StreamObserver<generated.Response> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddRideLeaderMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<generated.reservation> reserveRides(
        io.grpc.stub.StreamObserver<generated.ride> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getReserveRidesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetRidesAsyncMethod(),
            io.grpc.stub.ServerCalls.asyncServerStreamingCall(
              new MethodHandlers<
                generated.emptyMessage,
                generated.Response>(
                  this, METHODID_GET_RIDES_ASYNC)))
          .addMethod(
            getAddRideLeaderMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                generated.ride,
                generated.Response>(
                  this, METHODID_ADD_RIDE_LEADER)))
          .addMethod(
            getReserveRidesMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                generated.reservation,
                generated.ride>(
                  this, METHODID_RESERVE_RIDES)))
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
    public void getRidesAsync(generated.emptyMessage request,
        io.grpc.stub.StreamObserver<generated.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetRidesAsyncMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addRideLeader(generated.ride request,
        io.grpc.stub.StreamObserver<generated.Response> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddRideLeaderMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<generated.reservation> reserveRides(
        io.grpc.stub.StreamObserver<generated.ride> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getReserveRidesMethod(), getCallOptions()), responseObserver);
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
    public java.util.Iterator<generated.Response> getRidesAsync(
        generated.emptyMessage request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetRidesAsyncMethod(), getCallOptions(), request);
    }

    /**
     */
    public generated.Response addRideLeader(generated.ride request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddRideLeaderMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<generated.Response> addRideLeader(
        generated.ride request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddRideLeaderMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_RIDES_ASYNC = 0;
  private static final int METHODID_ADD_RIDE_LEADER = 1;
  private static final int METHODID_RESERVE_RIDES = 2;

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
        case METHODID_GET_RIDES_ASYNC:
          serviceImpl.getRidesAsync((generated.emptyMessage) request,
              (io.grpc.stub.StreamObserver<generated.Response>) responseObserver);
          break;
        case METHODID_ADD_RIDE_LEADER:
          serviceImpl.addRideLeader((generated.ride) request,
              (io.grpc.stub.StreamObserver<generated.Response>) responseObserver);
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
        case METHODID_RESERVE_RIDES:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.reserveRides(
              (io.grpc.stub.StreamObserver<generated.ride>) responseObserver);
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
              .addMethod(getGetRidesAsyncMethod())
              .addMethod(getAddRideLeaderMethod())
              .addMethod(getReserveRidesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
