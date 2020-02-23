
package org.biouno.unochoice;

import hudson.Extension;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

import static org.biouno.unochoice.AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT;


public class CascadeChoiceProvider extends ChoiceListProvider {


    private static final long serialVersionUID = 4524790278642708107L;


    private final String choiceType;


    private final Boolean filterable;

    private final Integer filterLength;

    private String referencedParameters;
    private final Script script;

    @DataBoundConstructor
    public CascadeChoiceProvider(Script script,
                                 String choiceType, String referencedParameters,
                                 Boolean filterable, Integer filterLength) {
        this.script=script;
        this.choiceType = choiceType;
        this.referencedParameters=referencedParameters;
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

    @Override
    public List<String> getChoiceList() {
        return null;
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
            return "Active Choices Reactive Parameter";
        }

    }

}
