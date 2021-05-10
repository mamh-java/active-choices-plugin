
package org.biouno.unochoice;

import hudson.Extension;

import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;


/**
 * A parameter that renders its options as a choice (select) HTML component.
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class ActiveChoiceProvider extends ChoiceListProvider {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -4449319038169585222L;

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

    private Script script;

    /**
     * Constructor called from Jelly with parameters.
     * @param name name 这3个统一提取放到 类 ActiveChoiceParameterDefinition 中了
     * @param description description这3个统一提取放到 类 ActiveChoiceParameterDefinition 中了
     * @param randomName parameter random generated name这 3个统一提取放到 类 ActiveChoiceParameterDefinition 中了
     * @param script script
     * @param choiceType choice type
     * @param filterable filter flag
     * @param filterLength length when filter start filtering
     */
    @DataBoundConstructor
    public ActiveChoiceProvider(Script script, String choiceType, Boolean filterable, Integer filterLength) {
        this.script = script;
        this.choiceType = StringUtils.defaultIfBlank(choiceType, ActiveChoiceParameterDefinition.PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
        this.filterLength = filterLength;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    public String getChoiceType() {
        return this.choiceType;
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

    public Script getScript() {
        return script;
    }

    @Override
    public List<String> getChoiceList() {
        return null;
    }

    // --- descriptor

    @Extension
    public static final class DescriptImpl extends ChoiceListProviderDescriptor {

        @Override
        public String getDisplayName() {
            return "Active Choices Parameter";
        }

    }

}
