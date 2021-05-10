
package org.biouno.unochoice;

import hudson.Extension;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;



public class CascadeChoiceProvider extends ChoiceListProvider {


    private static final long serialVersionUID = 4524790278642708107L;


    private String choiceType;


    private Boolean filterable;

    private Integer filterLength;

    private String referencedParameters;
    private Script script;

    @DataBoundConstructor
    public CascadeChoiceProvider(Script script, String choiceType, String referencedParameters, Boolean filterable, Integer filterLength) {
        this.script = script;
        this.choiceType = choiceType;
        this.referencedParameters = referencedParameters;
        this.filterable = filterable;
        this.filterLength = filterLength;
    }


    public String getChoiceType() {
        return choiceType;
    }


    public Boolean getFilterable() {
        return filterable;
    }


    public Integer getFilterLength() {
        return filterLength == null ? (Integer) 1 : filterLength;
    }

    public String getReferencedParameters() {
        return referencedParameters;
    }

    public void setReferencedParameters(String referencedParameters) {
        this.referencedParameters = referencedParameters;
    }

    public Script getScript() {
        return script;
    }

    @Extension
    public static final class DescriptImpl extends ChoiceListProviderDescriptor {

        @Override
        public String getDisplayName() {
            return "Reactive Choices Parameter";
        }

    }

}
