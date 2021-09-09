package io.zimara.backend.api.service.parser.deployment;

import io.zimara.backend.api.service.parser.DeploymentParserService;
import io.zimara.backend.model.deployment.kamelet.KameletBinding;
import io.zimara.backend.model.deployment.kamelet.KameletBindingSpec;
import io.zimara.backend.model.deployment.kamelet.KameletBindingStep;
import io.zimara.backend.model.deployment.kamelet.KameletBindingStepRef;
import io.zimara.backend.model.step.Step;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * 🐱miniclass KameletBindingDeploymentParserService (DeploymentParserService)
 */
@ApplicationScoped
public class KameletBindingDeploymentParserService implements DeploymentParserService {

    @Override
    public String parse(String name, List<Step> steps) {

        KameletBindingSpec spec = new KameletBindingSpec();

        spec.setSource(createKameletBindingStep(steps.get(0)));

        for (int i = 1; i < steps.size() - 1; i++) {
            spec.getSteps().add(createKameletBindingStep(steps.get(i)));
        }

        spec.setSink(createKameletBindingStep(steps.get(steps.size() - 1)));

        KameletBinding binding = new KameletBinding(name, spec);

        Representer representer = new Representer();
        representer.getPropertyUtils().setAllowReadOnlyProperties(true);

        Yaml yaml = new Yaml(new Constructor(KameletBinding.class), representer);
        return yaml.dumpAsMap(binding);
    }

    private KameletBindingStep createKameletBindingStep(Step step) {
        KameletBindingStep kameletStep = new KameletBindingStep();

        KameletBindingStepRef ref = new KameletBindingStepRef();
        ref.setName(step.getName());
        kameletStep.setRef(ref);

        for (var p : step.getParameters()) {
            if (p.getValue() != null) {
                kameletStep.getProperties().put(p.getId(), p.getValue().toString());
            }
        }

        return kameletStep;
    }

    @Override
    public boolean appliesTo(List<Step> steps) {
        if (steps.size() < 2) {
            return false;
        }

        for (Step s : steps) {
            if (s.getSubType().equalsIgnoreCase("KAMELET")) {
                return true;
            }
        }

        return false;
    }
}