
package org.biouno.unochoice;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.List;

/**
 * <p>Provides a <b>dynamic reference parameter</b> for users. This is a not so elegant
 * solution, since we are using a ParameterDefinition extension point, but it
 * actually <b>doesn't provide any parameter value</b>.</p>
 *
 * <p>This kind of parameter is only for reference. An use case is when you have several
 * job parameters, but your input values may vary depending on previous executions. You
 * can get the previous executions by accessing from your Groovy code the jenkinsProject
 * variable.</p>
 *
 * <p>Its options are retrieved from the evaluation of a Groovy script.</p>
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class DynamicReferenceProvider extends ChoiceListProvider {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 8261526672604361397L;

    /**
     * Choice type.
     */
    private final String choiceType;

    private final Boolean omitValueField;

    private final Script script;
    private String referencedParameters;

    @DataBoundConstructor
    public DynamicReferenceProvider(Script script,
                                    String choiceType, String referencedParameters,
                                    Boolean omitValueField) {
        this.script=script;
        this.referencedParameters=referencedParameters;
        this.choiceType = choiceType;
        this.omitValueField = BooleanUtils.toBooleanDefaultIfNull(omitValueField, Boolean.FALSE);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
    public String getChoiceType() {
        return this.choiceType;
    }

    public Boolean getOmitValueField() {
        return omitValueField;
    }

    public Script getScript() {
        return script;
    }

    public String getReferencedParameters() {
        return referencedParameters;
    }

    @Override
    public List<String> getChoiceList() {
        return null;
    }

    @JavaScriptMethod
    public String getChoicesAsStringForUI() {

        return "";
    }

    // --- descriptor

    @Extension
    public static final class DescriptorImpl extends ChoiceListProviderDescriptor {


        @Override
        public String getDisplayName() {
            return "Active Choices Reference Parameter";
        }

    }

}
