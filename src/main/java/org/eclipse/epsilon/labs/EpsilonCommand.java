package org.eclipse.epsilon.labs;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.emfatic.core.EmfaticResourceFactory;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.execute.operations.contributors.AnyOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.ArrayOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.BasicEUnitOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.BooleanOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.DateOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.IntegerOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.IterableOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.ModelElementOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.NumberOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.ScalarOperationContributor;
import org.eclipse.epsilon.eol.execute.operations.contributors.StringOperationContributor;
import org.eclipse.epsilon.eol.parse.EolParser;
import org.eclipse.epsilon.eol.types.EolBag;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.eol.types.EolOrderedSet;
import org.eclipse.epsilon.eol.types.EolSequence;
import org.eclipse.epsilon.eol.types.EolSet;
import org.eclipse.epsilon.eol.types.concurrent.EolConcurrentBag;
import org.eclipse.epsilon.eol.types.concurrent.EolConcurrentSet;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.parse.EvlParser;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.core.annotation.ReflectionConfig;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.annotation.TypeHint.AccessType;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/*
 * Ecore types that are used reflectively.
 */
@TypeHint(value = {
        EParameter[].class,
        EStringToStringMapEntryImpl[].class,
        ETypeParameter[].class
})
/* Epsilon parsers */
@ReflectionConfig(type = EolParser.class, accessType = {
        AccessType.ALL_DECLARED_FIELDS,
        AccessType.ALL_DECLARED_METHODS,
        AccessType.ALL_DECLARED_CONSTRUCTORS
})
@ReflectionConfig(type = EvlParser.class, accessType = {
        AccessType.ALL_DECLARED_FIELDS,
        AccessType.ALL_DECLARED_METHODS,
        AccessType.ALL_DECLARED_CONSTRUCTORS
})
/* EMF models */
@ReflectionConfig(type = EmfModel.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EolModelElementType.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EObject.class, accessType = AccessType.ALL_PUBLIC_METHODS)
/* Operation contributors */
@ReflectionConfig(type = AnyOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = ArrayOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = BasicEUnitOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = BooleanOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = DateOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = IntegerOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = IterableOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = ModelElementOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = NumberOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = ScalarOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = StringOperationContributor.class, accessType = AccessType.ALL_PUBLIC_METHODS)
/* EOL collections */
@ReflectionConfig(type = EolBag.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EolConcurrentBag.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EolConcurrentSet.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EolOrderedSet.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EolSequence.class, accessType = AccessType.ALL_PUBLIC_METHODS)
@ReflectionConfig(type = EolSet.class, accessType = AccessType.ALL_PUBLIC_METHODS)
/* Picocli command */
@Command(name = "epsilon", description = "Runs an Epsilon Object Language / Epsilon Validation Language script.", mixinStandardHelpOptions = true)
public class EpsilonCommand implements Runnable {

    @Parameters(index = "0", description = "Path to the EOL/EVL script")
    private File scriptPath;

    @ArgGroup(exclusive = false, multiplicity = "0..*")
    List<ModelOptions> models;

    static class ModelOptions {
        @Option(names = { "-n",
                "--model-name" }, required = true, defaultValue = "Model", description = "Name of the model (defaults to 'Model')")
        String name;
        @Option(names = { "-f",
                "--model-file" }, required = true, description = "Path to the model file (in .xmi or .flexmi format)")
        File modelFile;
        @Option(names = { "-m",
                "--metamodel-file" }, description = "Path to a metamodel file (in .ecore or .emf format)")
        List<File> metamodelFiles;
        @Option(names = { "-r", "--read-on-load" }, description = "If used, will read the model from disk upon load (default is not to read)")
        boolean readOnLoad;
        @Option(names = { "-s",
                "--store-on-disposal" }, description = "If used, will store the model to disk upon disposal (default is not to store)")
        boolean storeOnDisposal;
    }

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(EpsilonCommand.class, args);
    }

    public void run() {
        EolModule module = null;

        try {
            // Register the Flexmi and Emfatic parsers with EMF
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("flexmi", new FlexmiResourceFactory());
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("emf", new EmfaticResourceFactory());

            // Parse the EOL program
            if ("evl".equals(scriptPath.getName().toLowerCase())) {
                module = new EvlModule();
            } else {
                module = new EolModule();
            }
            URI fileURI = scriptPath.toURI();
            module.parse(fileURI);
            if (!module.getParseProblems().isEmpty()) {
                for (ParseProblem problem : module.getParseProblems()) {
                    System.err.println("Parsing problem: " + problem);
                }
                System.exit(1);
            }

            if (models != null) {
                for (ModelOptions model : models) {
                    EmfModel m = new EmfModel();
                    m.setName(model.name);
                    m.setModelFile(model.modelFile.getPath());
                    m.setMetamodelFiles(model.metamodelFiles.stream().map(f -> f.getPath()).toList());
                    m.setReadOnLoad(model.readOnLoad);
                    m.setStoredOnDisposal(model.storeOnDisposal);
                    m.load();

                    module.getContext().getModelRepository().addModel(m);
                }
            }

            // Execute the EOL program
            module.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (module != null) {
                // Dispose of the model
                module.getContext().getModelRepository().dispose();
            }
        }
    }
}
