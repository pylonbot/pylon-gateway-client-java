package lol.up.pylon.gateway.client.util;

import lol.up.pylon.gateway.client.exception.GrpcException;
import lol.up.pylon.gateway.client.exception.GrpcGatewayApiException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;

import java.util.concurrent.ExecutionException;

public class ExceptionUtil {

    public static GrpcException asGrpcException(final Throwable throwable) throws RuntimeException {
        if (throwable instanceof GrpcRequestException) {
            return (GrpcRequestException) throwable;
        }
        if (throwable instanceof GrpcGatewayApiException) {
            return (GrpcGatewayApiException) throwable;
        }
        if(throwable instanceof ExecutionException) {
            if(throwable.getCause() instanceof GrpcRequestException) {
                return (GrpcException) throwable.getCause();
            }
            return new GrpcRequestException("An error occurred during gRPC: " + getLastMethodCaller(), throwable.getCause());
        }
        return new GrpcRequestException("An error occurred during gRPC: " + getLastMethodCaller(), throwable);
    }

    private static String getLastMethodCaller() {
        final StackTraceElement traceElement = new Exception().getStackTrace()[2];
        final String qualifiedClassName = traceElement.getClassName();
        if (qualifiedClassName.contains(".")) {
            return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1) + "." + traceElement.getMethodName();
        } else {
            return qualifiedClassName + "." + traceElement.getMethodName();
        }
    }

}
