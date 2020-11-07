package lol.up.pylon.gateway.client.event;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;
import com.salesforce.jprotoc.ProtocPlugin;

import java.util.ArrayList;
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
        return fileDesc.getMessageTypeList().stream()
                .filter(descriptorProto -> descriptorProto.getName().contains("Event")
                        && !descriptorProto.getName().contains("Builder")
                        && !descriptorProto.getName().contains("Scope")
                && !descriptorProto.getName().contains("EventResponse"))
                .flatMap(descriptorProto -> handleMessageType(fileDesc, descriptorProto));
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleMessageType(final DescriptorProtos.FileDescriptorProto fileDesc,
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
        /*
        @Override
        public Class<lol.up.pylon.gateway.client.entity.event.MessageCreateEvent> getInterfaceType() {
            return null;
        }
         */
    }
}
