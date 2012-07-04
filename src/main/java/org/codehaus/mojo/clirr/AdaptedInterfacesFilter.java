package org.codehaus.mojo.clirr;

import java.lang.annotation.Annotation;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.clirr.core.ApiDifference;

public class AdaptedInterfacesFilter implements ApiDifferenceFilter
{

    private Set<String> annotations;
    private JavaTypeRepository originalClasses;
    private MessageCodeFilter adaptedInterfaceFilter = ExternallyInvokedFilter.EXTERNALLY_INVOKED_FILTER;

    public AdaptedInterfacesFilter( Set<String> adapterAnnotations, JavaTypeRepository origClasses )
    {
        this.annotations = adapterAnnotations;
        this.originalClasses = origClasses;
    }

    public boolean shouldInclude( ApiDifference apiDiff )
    {
        try {
            if(apiDiff.getAffectedMethod() != null)
            {
                Class<?> clazz = originalClasses.get( apiDiff.getAffectedClass());
                for(Annotation annotation : clazz.getAnnotations())
                {
                    if(annotations.contains( annotation.annotationType().getName() ))
                    {
                        return adaptedInterfaceFilter.shouldInclude( apiDiff );
                    }
                }
                
                // No adaptor annotation
                return true;
            } else 
            {
                // We only care about method issues, don't filter out methods and fields
                return true;
            }
        } catch(NoSuchElementException e)
        {
            // Ignore things that did not exist previously (they were obviously not deprecated)
            return false;
        }
    }

}
