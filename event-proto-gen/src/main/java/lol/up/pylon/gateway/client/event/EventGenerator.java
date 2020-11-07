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
    public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(final PluginProtos.CodeGeneratorRequest request) throws GeneratorException {
        return request.getProtoFileList().stream()
                .flatMap(this::handleProtoFile)
                .collect(Collectors.toList());
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleProtoFile(final DescriptorProtos.FileDescriptorProto fileDesc) {
        return fileDesc.getMessageTypeList().stream()
                .filter(descriptorProto -> descriptorProto.getName().contains("Event")
                        && !descriptorProto.getName().contains("Builder")
                        && !descriptorProto.getName().contains("Scope"))
                .flatMap(descriptorProto -> handleMessageType(fileDesc, descriptorProto));
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> handleMessageType(final DescriptorProtos.FileDescriptorProto fileDesc, final DescriptorProtos.DescriptorProto messageTypeDesc) {
        final String javaPackage;
        if (fileDesc.getOptions().hasJavaPackage()) {
            javaPackage = fileDesc.getOptions().getJavaPackage();
        } else {
            javaPackage = fileDesc.getPackage();
        }
        final String fileName = javaPackage.replace(".", "/") + "/" + messageTypeDesc.getName() + ".java";

        final List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();
        files.add(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(fileName)
                .setContent("lol.up.pylon.gateway.client.entity.event." + messageTypeDesc.getName() + ", ")
                .setInsertionPoint("message_implements:" + fileDesc.getPackage() + "." + messageTypeDesc.getName())
                .build());
        return files.stream();
    }
}
