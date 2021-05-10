
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
public class ReferenceChoiceProvider extends ChoiceListProvider {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 8261526672604361397L;

    /**
     * Choice type.
     */
    private String choiceType;

    private Boolean omitValueField;

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
     * @param omitValueField used in the UI to decide whether to include a hidden empty &lt;input name=value&gt;.
     * <code>false</code> by default.
     */
    @DataBoundConstructor
    public ReferenceChoiceProvider(Script script, String choiceType, String referencedParameters,
                                   Boolean omitValueField) {
        this.script=script;
        this.referencedParameters=referencedParameters;
        this.choiceType = StringUtils.defaultIfBlank(choiceType, ActiveChoiceParameterDefinition.PARAMETER_TYPE_SINGLE_SELECT);
        this.omitValueField = BooleanUtils.toBooleanDefaultIfNull(omitValueField, Boolean.FALSE);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
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
            return "Reference Choices Parameter";
        }

    }

}
