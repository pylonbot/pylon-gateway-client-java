package lol.up.pylon.gateway.client.util;

import lol.up.pylon.gateway.client.exception.GrpcGatewayApiException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;

public class ExceptionUtil {

    public static void rethrowGrpcException(final Throwable throwable) throws RuntimeException {
        if (throwable instanceof GrpcRequestException) {
            throw (GrpcRequestException) throwable;
        }
        if (throwable instanceof GrpcGatewayApiException) {
            throw (GrpcGatewayApiException) throwable;
        }
        throw new GrpcRequestException("An error occurred during gRPC: " + getLastMethodCaller(), throwable);
    }

    private static String getLastMethodCaller() {
        final StackTraceElement traceElement = new Exception().getStackTrace()[2];
        final String qualifiedClassName = traceElement.getClassName();
        if(qualifiedClassName.contains(".")) {
            return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1) + "." + traceElement.getMethodName();
        } else {
            return qualifiedClassName + "." + traceElement.getMethodName();
        }
    }

}
