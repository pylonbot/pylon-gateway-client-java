package lol.up.pylon.gateway.client.event;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;
import com.salesforce.jprotoc.ProtocPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventGenerator extends Generator {

    public static void main(final String[] args) {
        if (args.length == 0) {
            ProtocPlugin.generate(new EventGenerator());
        } else {
            ProtocPlugin.debug(new EventGenerator(), args[0]);
        }
    }

    @Override
    public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(final PluginProtos.CodeGeneratorRequest request)
            throws GeneratorException {
        return request.getProtoFileList().stream()
                .flatMap(this::handleProtoFile)
                .collect(Collectors.toList());
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleProtoFile(final DescriptorProtos.FileDescriptorProto fileDesc) {
        return Stream.concat(
                fileDesc.getMessageTypeList().stream()
                        .filter(descriptorProto -> descriptorProto.getName().contains("Event")
                                && !descriptorProto.getName().contains("Builder")
                                && !descriptorProto.getName().contains("Scope")
                                && !descriptorProto.getName().contains("EventResponse"))
                        .flatMap(descriptorProto -> handleEventClasses(fileDesc, descriptorProto)),
                fileDesc.getMessageTypeList().stream()
                        .filter(descriptorProto -> fileDesc.getPackage().contains("pylon."))
                        .flatMap(descriptorProto -> handleWrappedTypes(fileDesc, descriptorProto))
        );
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleEventClasses(final DescriptorProtos.FileDescriptorProto fileDesc,
                                                                              final DescriptorProtos.DescriptorProto messageTypeDesc) {
        final String javaPackage;
        if (fileDesc.getOptions().hasJavaPackage()) {
            javaPackage = fileDesc.getOptions().getJavaPackage();
        } else {
            javaPackage = fileDesc.getPackage();
        }
        final String fileName = javaPackage.replace(".", "/") + "/" + messageTypeDesc.getName() + ".java";

        final String messageType = fileDesc.getPackage() + "." + messageTypeDesc.getName();
        final String interfaceClass = "lol.up.pylon.gateway.client.entity.event." + messageTypeDesc.getName();
        final List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();
        files.add(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(fileName)
                .setContent(interfaceClass + ", ")
                .setInsertionPoint("message_implements:" + messageType)
                .build());
        files.add(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(fileName)
                .setContent(
                        "@Override\n" +
                                "public Class<" + interfaceClass + "> getInterfaceType() {\n" +
                                "  return " + interfaceClass + ".class;\n" +
                                "}"
                )
                .setInsertionPoint("class_scope:" + messageType)
                .build());
        return files.stream();
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleWrappedTypes(final DescriptorProtos.FileDescriptorProto fileDesc,
                                                                              final DescriptorProtos.DescriptorProto messageTypeDesc) {
        return messageTypeDesc.getFieldList().stream()
                .filter(fieldDescriptorProto ->
                        fieldDescriptorProto.getTypeName().contains(".StringValue") ||
                                fieldDescriptorProto.getTypeName().contains(".BoolValue") ||
                                fieldDescriptorProto.getTypeName().contains("UInt32Value") ||
                                fieldDescriptorProto.getTypeName().contains("Int32Value") ||
                                fieldDescriptorProto.getTypeName().contains("Int64Value") ||
                                fieldDescriptorProto.getTypeName().contains("UInt64Value") ||
                                fieldDescriptorProto.getTypeName().contains("FloatValue") ||
                                fieldDescriptorProto.getTypeName().contains("DoubleValue") ||
                                fieldDescriptorProto.getTypeName().contains("SnowflakeValue"))
                .flatMap(fieldDescriptorProto -> handleWrappedType(fileDesc, messageTypeDesc, fieldDescriptorProto));
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleWrappedType(final DescriptorProtos.FileDescriptorProto fileDesc,
                                                                             final DescriptorProtos.DescriptorProto messageTypeDesc,
                                                                             final DescriptorProtos.FieldDescriptorProto fieldTypeDesc) {
        final String javaPackage;
        if (fileDesc.getOptions().hasJavaPackage()) {
            javaPackage = fileDesc.getOptions().getJavaPackage();
        } else {
            javaPackage = fileDesc.getPackage();
        }
        final String fileName = javaPackage.replace(".", "/") + "/" + messageTypeDesc.getName() + ".java";

        final String messageType = fileDesc.getPackage() + "." + messageTypeDesc.getName();

        final List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();
        final String fieldName = getUpperPascalCase(fieldTypeDesc.getName());
        final String shortType =
                fieldTypeDesc.getTypeName().substring(fieldTypeDesc.getTypeName().lastIndexOf(".") + 1);
        files.add(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(fileName)
                .setContent("" +
                        "public Builder set" + fieldName + "(@javax.annotation.Nullable " +
                        getPrimitiveType(shortType) + " value) {\n" +
                        " if(value == null) {\n" +
                        "    clear" + fieldName + "();\n" +
                        "  } else {\n" +
                        "    " + getSetterOf(fieldName, fieldTypeDesc.getTypeName(), shortType) + "\n" +
                        "  }\n" +
                        "  return this;\n" +
                        "}\n\n")
                .setInsertionPoint("builder_scope:" + messageType)
                .build());
        return files.stream();
    }

    public String getPrimitiveType(final String typeNameShort) {
        switch (typeNameShort) {
            case "StringValue":
                return "String";
            case "BoolValue":
                return "Boolean";
            case "UInt32Value":
            case "Int32Value":
                return "Integer";
            case "UInt64Value":
            case "Int64Value":
            case "SnowflakeValue":
                return "Long";
            case "DoubleValue":
                return "Double";
            case "FloatValue":
                return "Float";
            default:
                throw new RuntimeException("Wtf " + typeNameShort);
        }
    }

    public String getSetterOf(final String fieldName, final String typeName, final String typeNameShort) {
        if(typeNameShort.equals("SnowflakeValue")) {
            return "set" + fieldName + "(bot.pylon.proto.discord.v1.model." + typeNameShort + ".newBuilder().setValue(value).build());";
        } else {
            return "set" + fieldName + "(com" + typeName + ".of(value));";
        }
    }

    public String getUpperPascalCase(final String field) {
        final String[] values = field.split("_");
        return Arrays.stream(values)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining());
    }
}
