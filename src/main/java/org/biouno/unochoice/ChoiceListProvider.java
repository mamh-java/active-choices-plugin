
package org.biouno.unochoice;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import java.io.Serializable;


abstract public class ChoiceListProvider extends AbstractDescribableImpl<ChoiceListProvider> implements ExtensionPoint, Serializable {

    private static final long serialVersionUID = 8965389708210167871L;


    /**
     * Returns all the ChoiceListProvider subclass whose DescriptorImpl is annotated with Extension.
     *
     * @return DescriptorExtensionList of ChoiceListProvider subclasses.
     */
    static public DescriptorExtensionList<ChoiceListProvider, Descriptor<ChoiceListProvider>> all() {
        DescriptorExtensionList<ChoiceListProvider, Descriptor<ChoiceListProvider>> descriptorList = Jenkins.get().getDescriptorList(ChoiceListProvider.class);
        return descriptorList;
    }
}
