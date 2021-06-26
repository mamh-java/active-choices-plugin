
package org.biouno.unochoice;

import hudson.Extension;
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
    private final String choiceType;

    /**
     * Filter flag.
     */
    private final Boolean filterable;

    /**
     * Filter length. Defines a minimum number of characters that must be entered before the filter
     * is activated.
     */
    private final Integer filterLength;

    private final Script script;


    @DataBoundConstructor
    public ActiveChoiceProvider(Script script, String choiceType, Boolean filterable, Integer filterLength) {
        this.script = script;
        this.choiceType = choiceType;
        this.filterable = filterable;
        this.filterLength = filterLength;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
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
            return "Active Choices Common Parameter";
        }

    }

}
