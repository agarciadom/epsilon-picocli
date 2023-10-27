package org.eclipse.epsilon.labs;

import java.io.File;
import java.net.URI;

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
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.core.annotation.ReflectionConfig;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.annotation.TypeHint.AccessType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/*
 * Note: a good part of this could be perhaps automated with a GraalVM Feature:
 *
 * https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/#configuration-with-features
 */
@TypeHint(value = {
    EParameter[].class,
    EStringToStringMapEntryImpl[].class,
    ETypeParameter[].class
})
@ReflectionConfig(
    type = EolParser.class,
    accessType = {
        AccessType.ALL_DECLARED_FIELDS,
        AccessType.ALL_DECLARED_METHODS,
        AccessType.ALL_DECLARED_CONSTRUCTORS
    }
)
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
@Command(name = "epsilon", description = "Runs an Epsilon script", mixinStandardHelpOptions = true)
public class EpsilonCommand implements Runnable {

    @Option(names = { "-v", "--verbose" }, description = "...")
    boolean verbose;

    @Parameters(index = "0")
    private File eolPath;

    @Parameters(index = "1")
    private File metamodelPath;

    @Parameters(index = "2")
    private File modelPath;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(EpsilonCommand.class, args);
    }

    public void run() {
        try {
            // Register the Flexmi and Emfatic parsers with EMF
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("flexmi", new FlexmiResourceFactory());
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("emf", new EmfaticResourceFactory());

            // Parse the EOL program
            EolModule module = new EolModule();
            URI fileURI = eolPath.toURI();
            module.parse(fileURI);
            if (!module.getParseProblems().isEmpty()) {
                for (ParseProblem problem : module.getParseProblems()) {
                    System.err.println("Parsing problem: " + problem);
                }
                System.exit(1);
            }

            // Load the model from model.flexmi using metamodel.emf as its metamodel
            EmfModel model = new EmfModel();
            model.setName("M");
            model.setModelFile(modelPath.getPath());
            model.setMetamodelFile(metamodelPath.getPath());
            model.setReadOnLoad(true);
            model.setStoredOnDisposal(false);
            model.load();

            // Make the model available to the program
            module.getContext().getModelRepository().addModel(model);

            // Execute the EOL program
            module.execute();

            // Dispose of the model
            module.getContext().getModelRepository().dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
