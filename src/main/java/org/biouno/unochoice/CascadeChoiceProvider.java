
package org.biouno.unochoice;

import hudson.Extension;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;


/**
 * <p>A choice parameter, that gets updated when another parameter changes. The simplest
 * use case for this, would be having a list of states, and when the user selected a
 * state it would trigger an update of the city fields.</p>
 *
 * <p>The state parameter would be a choice parameter, and the city parameter would be a
 * cascade choice parameter, that referenced the former.</p>
 *
 * <p>Its options are retrieved from the evaluation of a Groovy script.</p>
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class CascadeChoiceProvider extends ChoiceListProvider {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 4524790278642708107L;

    /**
     * Choice type.
     */
    private String choiceType;

    /**
     * Filter flag.
     */
    private Boolean filterable;

    /**
     * Filter length. Defines a minimum number of characters that must be entered before the filter
     * is activated.
     */
    private Integer filterLength;

    private String referencedParameters;

    private Script script;

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name 这3个统一提取放到 类 ActiveChoiceParameterDefinition 中了
     * @param description description 这3个统一提取放到 类 ActiveChoiceParameterDefinition 中了
     * @param randomName parameter random generated name (uuid) 这3个统一提取放到 类 ActiveChoiceParameterDefinition 中了
     * @param script script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param filterable filter flag
     * @param filterLength filter length
     */
    @DataBoundConstructor
    public CascadeChoiceProvider(Script script, String choiceType, String referencedParameters,
                                 Boolean filterable, Integer filterLength) {
        this.script = script;
        this.choiceType = StringUtils.defaultIfBlank(choiceType, ActiveChoiceParameterDefinition.PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
        this.filterLength = filterLength;
        this.referencedParameters=referencedParameters;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    public String getChoiceType() {
        return choiceType;
    }

    /**
     * Get the filter flag.
     * @return filter flag
     */
    public Boolean getFilterable() {
        return filterable;
    }

    /**
     * Get the filter length.
     * @return filter length
     */
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

    // --- descriptor

    @Extension
    public static final class DescriptImpl extends ChoiceListProviderDescriptor {

        @Override
        public String getDisplayName() {
            return "Active Choices Reactive Parameter";
        }

    }

}
